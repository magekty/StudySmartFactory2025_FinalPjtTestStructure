package com.factory_dynamics.erp.erp_server.material;

import com.factory_dynamics.erp.erp_server.common.BizException;
import com.factory_dynamics.erp.erp_server.common.Uuids;
import com.factory_dynamics.erp.erp_server.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Transactional
public class MaterialCostService {
    private final MaterialCostRepository repo;
    private final ProductRepository productRepo;

    public MaterialCostService(MaterialCostRepository repo, ProductRepository productRepo) {
        this.repo = repo;
        this.productRepo = productRepo;
    }

    public MaterialCost upsert(MaterialCostUpsertRequest req, String actor) {
        var product = productRepo.findById(req.productId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "제품 없음"));
        var cost = MaterialCost.builder()
                .id(Uuids.newId())
                .product(product)
                .currency("KRW")
                .stdCost(req.stdCost())
                .effectiveFrom(req.effectiveFrom())
                .effectiveTo(req.effectiveTo())
                .build();
        var now = LocalDateTime.now(ZoneOffset.UTC);
        cost.setCreatedAt(now);
        cost.setModifiedAt(now);
        cost.setCreatedBy(actor);
        cost.setModifiedBy(actor);
        return repo.save(cost);
    }
}