package com.factory_dynamics.erp.erp_server.service;

import com.factory_dynamics.erp.erp_server.domain.BomHeader;
import com.factory_dynamics.erp.erp_server.domain.BomLine;
import com.factory_dynamics.erp.erp_server.domain.Material;
import com.factory_dynamics.erp.erp_server.domain.Product;
import com.factory_dynamics.erp.erp_server.domain.ProductionPlan;
import com.factory_dynamics.erp.erp_server.repository.BomHeaderRepository;
import com.factory_dynamics.erp.erp_server.repository.BomLineRepository;
import com.factory_dynamics.erp.erp_server.repository.MaterialRepository;
import com.factory_dynamics.erp.erp_server.repository.ProductRepository;
import com.factory_dynamics.erp.erp_server.repository.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CostService {

    private final ProductionPlanRepository productionPlanRepository;
    private final BomHeaderRepository bomHeaderRepository;
    private final BomLineRepository bomLineRepository;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;

    public BigDecimal calculateCostForPlan(Integer planId, BigDecimal laborCostRate, BigDecimal manufacturingOverheadRate) {
        Optional<ProductionPlan> planOptional = productionPlanRepository.findById(planId);
        if (planOptional.isEmpty()) {
            throw new IllegalArgumentException("Production Plan not found with ID: " + planId);
        }
        ProductionPlan plan = planOptional.get();

        // 제품에 대한 최신 BOM 헤더 조회
        List<BomHeader> bomHeaders = bomHeaderRepository.findByProduct_ProductId(plan.getProduct().getProductId());
        if (bomHeaders.isEmpty()) {
            throw new IllegalArgumentException("No BOM found for product: " + plan.getProduct().getName());
        }
        // 여기서는 가장 최신 버전의 BOM을 사용한다고 가정
        BomHeader latestBomHeader = bomHeaders.stream()
                .max((b1, b2) -> b1.getVersion().compareTo(b2.getVersion()))
                .get();

        // BOM 라인을 재귀적으로 조회하여 원자재 비용 합산
        List<BomLine> bomLines = bomLineRepository.findHierarchicalBomLines(latestBomHeader.getBomId());

        BigDecimal totalMaterialCost = BigDecimal.ZERO;

        for (BomLine line : bomLines) {
            if ("material".equals(line.getItemType())) {
                Optional<Material> materialOptional = materialRepository.findById(line.getItemId());
                if (materialOptional.isPresent()) {
                    Material material = materialOptional.get();
                    // 소모량 * 단가
                    BigDecimal lineCost = line.getQuantity().multiply(material.getCost());
                    totalMaterialCost = totalMaterialCost.add(lineCost);
                }
            }
        }

        // 최종 원가 계산
        BigDecimal totalCost = totalMaterialCost
                .add(laborCostRate) // 프론트엔드에서 전달받은 인건비율
                .add(manufacturingOverheadRate) // 프론트엔드에서 전달받은 제조경비율
                .multiply(plan.getQuantity()); // 계획 수량 곱하기

        // 소수점 2자리에서 반올림
        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }
}