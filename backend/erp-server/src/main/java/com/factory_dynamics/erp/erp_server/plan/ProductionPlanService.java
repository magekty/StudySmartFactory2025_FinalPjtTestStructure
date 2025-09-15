package com.factory_dynamics.erp.erp_server.plan;

import com.factory_dynamics.erp.erp_server.bom.BomHeaderRepository;
import com.factory_dynamics.erp.erp_server.common.BizException;
import com.factory_dynamics.erp.erp_server.common.Uuids;
import com.factory_dynamics.erp.erp_server.product.Product;
import com.factory_dynamics.erp.erp_server.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Transactional
public class ProductionPlanService {

    private final ProductionPlanRepository repo;
    private final ProductRepository productRepo;
    private final BomHeaderRepository bomHeaderRepo;

    public ProductionPlanService(ProductionPlanRepository repo, ProductRepository productRepo, BomHeaderRepository bomHeaderRepo) {
        this.repo = repo;
        this.productRepo = productRepo;
        this.bomHeaderRepo = bomHeaderRepo;
    }

    public ProductionPlan create(String planCode, String productId, BigDecimal qty, LocalDate start, LocalDate end, String status, String note, String actor) {
        if (start.isAfter(end)) {
            throw new BizException(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 늦을 수 없음");
        }
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "제품 없음: " + productId));

        boolean existsBom = bomHeaderRepo.findEffectiveActiveBom(productId, start).isPresent();
        if (!existsBom) {
            throw new BizException(HttpStatus.BAD_REQUEST, "유효한 활성 BOM이 없어 계획을 생성할 수 없음");
        }

        ProductionPlan plan = ProductionPlan.builder()
                .id(Uuids.newId())
                .planCode(planCode)
                .product(product)
                .qty(qty)
                .startDate(start)
                .endDate(end)
                .status(status)
                .note(note)
                .build();

        var now = LocalDateTime.now(ZoneOffset.UTC);
        plan.setCreatedAt(now);
        plan.setModifiedAt(now);
        plan.setCreatedBy(actor);
        plan.setModifiedBy(actor);
        return repo.save(plan);
    }
}