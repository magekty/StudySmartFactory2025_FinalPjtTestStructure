package com.factory_dynamics.erp.erp_server.plan.controller;

import com.factory_dynamics.erp.erp_server.common.BizException;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlan;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * 생산계획 상태 전이 전용 API (DTO 응답 버전).
 * 엔티티 직렬화로 인한 LAZY 프록시 초기화 문제(no session)를 피하기 위해
 * 항상 DTO로 매핑해 반환한다.
 */
@RestController
@RequestMapping("/api/plans")
@Tag(name = "Plan-Status", description = "생산계획 상태 전이 API")
public class ProductionPlanStatusController {

    private final ProductionPlanRepository planRepo;

    public ProductionPlanStatusController(ProductionPlanRepository planRepo) {
        this.planRepo = planRepo;
    }

    // 허용 상태
    private static final Set<String> ALLOWED = Set.of(
            "DRAFT", "CONFIRMED", "PENDING", "IN_PRODUCTION", "COMPLETED", "CANCELED"
    );

    // 응답 DTO: 프록시 접근 없이 안전한 필드만
    public record ProductionPlanStatusResponse(
            String planId,
            String planCode,
            String productId,   // 연관 엔티티 전체 대신 ID만 노출
            String status,
            String note,
            LocalDateTime modifiedAt,
            String modifiedBy
    ) {
        public static ProductionPlanStatusResponse from(ProductionPlan p) {
            return new ProductionPlanStatusResponse(
                    p.getId(),
                    p.getPlanCode(),
                    // product는 LAZY일 수 있으므로 getId만 안전하게 접근
                    p.getProduct() != null ? p.getProduct().getId() : null,
                    p.getStatus(),
                    p.getNote(),
                    p.getModifiedAt(),
                    p.getModifiedBy()
            );
        }
    }

    @Operation(summary = "생산계획 상태 전이", description = "현재 상태에서 nextStatus로 전이한다. 전이 규칙 위반 시 400 반환.")
    @PatchMapping("/{planId}/status")
    @Transactional
    public ResponseEntity<ProductionPlanStatusResponse> changeStatus(
            @PathVariable String planId,
            @RequestParam @NotBlank String nextStatus,
            @RequestParam(required = false, defaultValue = "system") String actor
    ) {
        String to = nextStatus.trim().toUpperCase();
        if (!ALLOWED.contains(to)) {
            throw new BizException(HttpStatus.BAD_REQUEST, "알 수 없는 상태: " + nextStatus);
        }

        ProductionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "계획 없음: " + planId));

        String from = plan.getStatus() == null ? "DRAFT" : plan.getStatus().toUpperCase();

        // 동일 상태면 no-op
        if (from.equals(to)) {
            plan.setModifiedBy(actor);
            plan.setModifiedAt(LocalDateTime.now(ZoneOffset.UTC));
            return ResponseEntity.ok(ProductionPlanStatusResponse.from(plan));
        }

        // 완료 이후 전이 금지
        if ("COMPLETED".equals(from) && !"COMPLETED".equals(to)) {
            throw new BizException(HttpStatus.BAD_REQUEST, "완료 상태 이후 전이 불가");
        }

        // 전이 규칙 검증
        if (!canTransit(from, to)) {
            throw new BizException(HttpStatus.BAD_REQUEST, "전이 불가: " + from + " -> " + to);
        }

        // 전이 수행
        plan.setStatus(to);
        plan.setModifiedBy(actor);
        plan.setModifiedAt(LocalDateTime.now(ZoneOffset.UTC));
        // plan 저장은 @Transactional로 커밋 시점에 flush

        // 응답은 DTO로 반환하여 프록시 초기화 회피
        return ResponseEntity.ok(ProductionPlanStatusResponse.from(plan));
    }

    // 상태 전이 규칙
    private boolean canTransit(String from, String to) {
        return switch (from) {
            case "DRAFT" -> to.equals("CONFIRMED") || to.equals("CANCELED");
            case "CONFIRMED" -> to.equals("PENDING") || to.equals("IN_PRODUCTION") || to.equals("CANCELED");
            case "PENDING" -> to.equals("CONFIRMED") || to.equals("IN_PRODUCTION") || to.equals("CANCELED");
            case "IN_PRODUCTION" -> to.equals("COMPLETED") || to.equals("CANCELED");
            case "COMPLETED" -> to.equals("COMPLETED"); // 유지만 허용
            case "CANCELED" -> to.equals("CANCELED");   // 취소 후 고정(정책에 따라 변경 가능)
            default -> false;
        };
    }
}