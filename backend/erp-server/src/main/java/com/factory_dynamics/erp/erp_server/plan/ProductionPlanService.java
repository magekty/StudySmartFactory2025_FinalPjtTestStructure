// plan/ProductionPlanService.java
package com.factory_dynamics.erp.erp_server.plan;

import com.factory_dynamics.erp.erp_server.common.BizException;
import com.factory_dynamics.erp.erp_server.plan.dto.*;
import com.factory_dynamics.erp.erp_server.product.Product;
import com.factory_dynamics.erp.erp_server.product.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@Transactional(readOnly = true)
public class ProductionPlanService {

    private final ProductionPlanRepository repo;
    private final ProductRepository productRepo;

    public ProductionPlanService(ProductionPlanRepository repo, ProductRepository productRepo) {
        this.repo = repo;
        this.productRepo = productRepo;
    }

    public Page<ProductionPlanSummaryDto> list(String q, String status, java.time.LocalDate from, java.time.LocalDate to,
                                               int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var result = repo.search(q == null ? "" : q.trim(),
                status == null ? "ALL" : status.trim(),
                from, to, pageable);
        return result.map(this::toSummary);
    }

    public ProductionPlanDetailDto get(String planId) {
        var p = mustGet(planId);
        return toDetail(p);
    }

    @Transactional
    public ProductionPlanDetailDto create(CreateProductionPlanRequest req, String actor) {
        // 중복 planCode 체크
        repo.findByPlanCodeAndDeletedFalse(req.planCode())
                .ifPresent(x -> { throw new BizException(HttpStatus.CONFLICT, "중복 계획코드: " + req.planCode()); });

        // 제품 존재 검증(선택적이지만 권장)
        productRepo.findById(req.productId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "제품 없음: " + req.productId()));

        var now = OffsetDateTime.now(ZoneOffset.UTC);

        // Product 참조: 기본 생성자 + ID만 세팅
        var prodRef = new Product();
        prodRef.setId(req.productId());

        var p = ProductionPlan.builder()
                .id(java.util.UUID.randomUUID().toString())
                .planCode(req.planCode().trim())
                .product(prodRef)
                .qty(req.qty())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .status(req.status() == null ? "DRAFT" : req.status().trim().toUpperCase())
                .note(req.note())
                .build();

        p.setCreatedBy(actor);
        p.setCreatedAt(now.toLocalDateTime());
        p.setModifiedBy(actor);
        p.setModifiedAt(now.toLocalDateTime());

        validateDateRange(p.getStartDate(), p.getEndDate());

        repo.save(p);
        return toDetail(p);
    }

    @Transactional
    public ProductionPlanDetailDto update(String planId, UpdateProductionPlanRequest req, String actor) {
        var p = mustGet(planId);
        checkVersion(p.getVersion(), req.version());

        if (req.qty() != null) p.setQty(req.qty());
        if (req.startDate() != null) p.setStartDate(req.startDate());
        if (req.endDate() != null) p.setEndDate(req.endDate());
        p.setNote(req.note());

        if (p.getStartDate() != null && p.getEndDate() != null) {
            validateDateRange(p.getStartDate(), p.getEndDate());
        }

        touchModified(p, actor);
        return toDetail(p);
    }

    @Transactional
    public ProductionPlanDetailDto changeStatus(String planId, ChangeStatusRequest req) {
        var p = mustGet(planId);
        checkVersion(p.getVersion(), req.version());

        var from = p.getStatus() == null ? "DRAFT" : p.getStatus().toUpperCase();
        var to = req.nextStatus().trim().toUpperCase();

        if (!isAllowed(to)) {
            throw new BizException(HttpStatus.BAD_REQUEST, "알 수 없는 상태: " + to);
        }
        if (from.equals(to)) {
            touchModified(p, req.actor());
            return toDetail(p);
        }
        if ("COMPLETED".equals(from) && !"COMPLETED".equals(to)) {
            throw new BizException(HttpStatus.BAD_REQUEST, "완료 이후 전이 불가");
        }
        if (!canTransit(from, to)) {
            throw new BizException(HttpStatus.BAD_REQUEST, "전이 불가: " + from + " -> " + to);
        }

        p.setStatus(to);
        touchModified(p, req.actor());
        return toDetail(p);
    }

    @Transactional
    public void softDelete(String planId, String actor) {
        var p = mustGet(planId);
        if (p.isDeleted()) return;
        p.setDeleted(true);
        p.setDeletedAt(java.time.LocalDateTime.now());
        touchModified(p, actor);
    }

    // 헬퍼
    private ProductionPlan mustGet(String planId) {
        var p = repo.findById(planId).orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "계획 없음: " + planId));
        if (p.isDeleted()) throw new BizException(HttpStatus.GONE, "삭제된 계획: " + planId);
        return p;
    }
    private void checkVersion(long entityVersion, long requestVersion) {
        if (entityVersion != requestVersion) {
            throw new BizException(HttpStatus.CONFLICT, "버전 불일치");
        }
    }
    private void validateDateRange(java.time.LocalDate s, java.time.LocalDate e) {
        if (s.isAfter(e)) throw new BizException(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 늦음");
    }
    private void touchModified(ProductionPlan p, String actor) {
        var now = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        p.setModifiedBy(actor == null ? "system" : actor);
        p.setModifiedAt(now);
    }

    private boolean isAllowed(String to) {
        return switch (to) {
            case "DRAFT", "CONFIRMED", "PENDING", "IN_PRODUCTION", "COMPLETED", "CANCELED" -> true;
            default -> false;
        };
    }
    private boolean canTransit(String from, String to) {
        return switch (from) {
            case "DRAFT" -> to.equals("CONFIRMED") || to.equals("CANCELED");
            case "CONFIRMED" -> to.equals("PENDING") || to.equals("CANCELED");
            case "PENDING" -> to.equals("IN_PRODUCTION") || to.equals("CANCELED");
            case "IN_PRODUCTION" -> to.equals("COMPLETED") || to.equals("CANCELED");
            case "COMPLETED" -> to.equals("COMPLETED"); // 변경 불가
            case "CANCELED" -> false; // 취소 후 재전이 금지(정책에 따라 완화 가능)
            default -> false;
        };
    }

    private ProductionPlanSummaryDto toSummary(ProductionPlan p) {
        return new ProductionPlanSummaryDto(
                p.getId(),
                p.getPlanCode(),
                p.getProduct().getId(),
                p.getProduct().getProductCode(),
                p.getProduct().getName(),
                p.getStartDate(),
                p.getEndDate(),
                p.getQty(),
                p.getStatus(),
                p.getCreatedAt() == null ? null : p.getCreatedAt().atOffset(ZoneOffset.UTC)
        );
    }

    private ProductionPlanDetailDto toDetail(ProductionPlan p) {
        return new ProductionPlanDetailDto(
                p.getId(),
                p.getPlanCode(),
                p.getProduct().getId(),
                p.getProduct().getProductCode(),
                p.getProduct().getName(),
                p.getStartDate(),
                p.getEndDate(),
                p.getQty(),
                p.getStatus(),
                p.getNote(),
                p.getVersion()
        );
    }
}