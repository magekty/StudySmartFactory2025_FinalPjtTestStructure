package com.factory_dynamics.erp.erp_server.service;

import com.factory_dynamics.erp.erp_server.domain.BomHeader;
import com.factory_dynamics.erp.erp_server.domain.Product;
import com.factory_dynamics.erp.erp_server.domain.ProductionPlan;
import com.factory_dynamics.erp.erp_server.repository.BomHeaderRepository;
import com.factory_dynamics.erp.erp_server.repository.ProductRepository;
import com.factory_dynamics.erp.erp_server.repository.ProductionPlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductionPlanService {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductRepository productRepository;
    private final BomHeaderRepository bomHeaderRepository;

    @Transactional
    public ProductionPlan createProductionPlan(ProductionPlan productionPlan) {
        // 제품 존재 여부 확인
        if (productionPlan.getProduct() == null || productionPlan.getProduct().getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required for Production Plan.");
        }
        Optional<Product> product = productRepository.findById(productionPlan.getProduct().getProductId());
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productionPlan.getProduct().getProductId());
        }

        // 핵심 비즈니스 규칙: BOM 없이는 생산 계획 생성 불가
        List<BomHeader> bomHeaders = bomHeaderRepository.findByProduct_ProductId(productionPlan.getProduct().getProductId());
        if (bomHeaders.isEmpty()) {
            throw new IllegalArgumentException("A BOM must exist for the product to create a production plan.");
        }

        productionPlan.setProduct(product.get());
        return productionPlanRepository.save(productionPlan);
    }

    public Optional<ProductionPlan> getProductionPlanById(Integer planId) {
        return productionPlanRepository.findById(planId);
    }

    public List<ProductionPlan> getAllProductionPlans() {
        return productionPlanRepository.findAll();
    }
}