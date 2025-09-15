package com.factory_dynamics.erp.erp_server.cost;

import com.factory_dynamics.erp.erp_server.bom.BomHeader;
import com.factory_dynamics.erp.erp_server.bom.BomHeaderRepository;
import com.factory_dynamics.erp.erp_server.bom.BomLine;
import com.factory_dynamics.erp.erp_server.bom.BomLineRepository;
import com.factory_dynamics.erp.erp_server.common.BizException;
import com.factory_dynamics.erp.erp_server.common.Uuids;
import com.factory_dynamics.erp.erp_server.material.MaterialCost;
import com.factory_dynamics.erp.erp_server.material.MaterialCostRepository;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlan;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlanRepository;
import com.factory_dynamics.erp.erp_server.product.Product;
import com.factory_dynamics.erp.erp_server.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional
public class CostService {

    private final BomHeaderRepository bomHeaderRepo;
    private final BomLineRepository bomLineRepo;
    private final MaterialCostRepository costRepo;
    private final ProductionPlanRepository planRepo;
    private final ProductRepository productRepo;
    private final CostSnapshotRepository snapshotRepo;
    private final CostSnapshotDetailRepository detailRepo;

    public CostService(BomHeaderRepository bomHeaderRepo,
                       BomLineRepository bomLineRepo,
                       MaterialCostRepository costRepo,
                       ProductionPlanRepository planRepo,
                       ProductRepository productRepo,
                       CostSnapshotRepository snapshotRepo,
                       CostSnapshotDetailRepository detailRepo) {
        this.bomHeaderRepo = bomHeaderRepo;
        this.bomLineRepo = bomLineRepo;
        this.costRepo = costRepo;
        this.planRepo = planRepo;
        this.productRepo = productRepo;
        this.snapshotRepo = snapshotRepo;
        this.detailRepo = detailRepo;
    }

    public CostSnapshotResponse calculateByPlan(String planId, BigDecimal laborRate, BigDecimal overheadRate) {
        ProductionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "생산계획을 찾을 수 없음: " + planId));
        CostSnapshot snapshot = calculateInternal(plan.getProduct(), plan.getQty(), plan, laborRate, overheadRate, LocalDate.now(ZoneOffset.UTC));
        var details = detailRepo.findAllWithComponentBySnapshotId(snapshot.getId());
        var detailDtos = details.stream().map(CostSnapshotDetailResponse::of).toList();
        return CostSnapshotResponse.of(snapshot, detailDtos);
    }

    public CostSnapshotResponse calculateByProduct(String productId, BigDecimal qty, BigDecimal laborRate, BigDecimal overheadRate, LocalDate baseDate) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "제품 없음: " + productId));
        CostSnapshot snapshot = calculateInternal(product, qty, null, laborRate, overheadRate, baseDate == null ? LocalDate.now(ZoneOffset.UTC) : baseDate);
        var details = detailRepo.findAllWithComponentBySnapshotId(snapshot.getId());
        var detailDtos = details.stream().map(CostSnapshotDetailResponse::of).toList();
        return CostSnapshotResponse.of(snapshot, detailDtos);
    }

    private CostSnapshot calculateInternal(Product product, BigDecimal planQty, ProductionPlan plan,
                                           BigDecimal laborRate, BigDecimal overheadRate, LocalDate baseDate) {

        BomHeader bom = bomHeaderRepo.findEffectiveActiveBom(product.getId(), baseDate)
                .orElseThrow(() -> new BizException(HttpStatus.BAD_REQUEST, "유효한 활성 BOM이 없음"));

        List<BomLine> lines = bomLineRepo.findByBom_IdAndDeletedFalse(bom.getId());
        Map<String, List<BomLine>> children = new HashMap<>();
        for (BomLine l : lines) {
            String parentKey = l.getParent() == null ? "ROOT" : l.getParent().getId();
            children.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(l);
        }

        List<ComponentRow> exploded = new ArrayList<>();
        dfsExplode("ROOT", children, BigDecimal.ONE, 0, new HashSet<>(), exploded, baseDate);

        BigDecimal Q = scale6(planQty == null ? BigDecimal.ONE : planQty);

        BigDecimal totalMaterial = BigDecimal.ZERO;
        for (ComponentRow row : exploded) {
            BigDecimal explodedQty = scale6(row.baseQty.multiply(Q));
            BigDecimal matCost = scale6(explodedQty.multiply(row.unitCost));
            row.explodedQty = explodedQty;
            row.materialCost = matCost;
            totalMaterial = totalMaterial.add(matCost);
        }
        totalMaterial = scale6(totalMaterial);

        BigDecimal labor = scale6(totalMaterial.multiply(laborRate));
        BigDecimal overhead = scale6(totalMaterial.multiply(overheadRate));
        BigDecimal total = scale6(totalMaterial.add(labor).add(overhead));

        CostSnapshot snapshot = CostSnapshot.builder()
                .id(Uuids.newId())
                .plan(plan)
                .product(product)
                .qty(Q)
                .totalMaterial(totalMaterial)
                .labor(labor)
                .overhead(overhead)
                .totalCost(total)
                .laborRate(scale6(laborRate))
                .overheadRate(scale6(overheadRate))
                .method("RATE")
                .calculatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        snapshot.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        snapshot.setModifiedAt(snapshot.getCreatedAt());
        snapshotRepo.save(snapshot);

        List<CostSnapshotDetail> details = new ArrayList<>();
        for (ComponentRow r : exploded) {
            CostSnapshotDetail d = CostSnapshotDetail.builder()
                    .snapshot(snapshot)
                    .component(r.component)
                    .level(r.level)
                    .baseQty(scale6(r.baseQty))
                    .scrapRate(scale6(r.scrapRate))
                    .explodedQty(scale6(r.explodedQty))
                    .unitCost(scale6(r.unitCost))
                    .materialCost(scale6(r.materialCost))
                    .build();
            details.add(d);
        }
        detailRepo.saveAll(details);

        return snapshot;
    }

    private void dfsExplode(String parentKey,
                            Map<String, List<BomLine>> children,
                            BigDecimal multiplier,
                            int level,
                            Set<String> path,
                            List<ComponentRow> out,
                            LocalDate baseDate) {

        List<BomLine> childs = children.getOrDefault(parentKey, List.of());
        for (BomLine line : childs) {
            String currentId = line.getId();
            if (path.contains(currentId)) {
                throw new BizException(HttpStatus.CONFLICT, "BOM 사이클 감지: " + currentId);
            }
            path.add(currentId);

            BigDecimal baseQty = scale6(multiplier.multiply(line.getQty().multiply(BigDecimal.ONE.add(line.getScrapRate()))));

            MaterialCost cost = costRepo.findEffectiveCost(line.getComponent().getId(), baseDate)
                    .orElseThrow(() -> new BizException(HttpStatus.BAD_REQUEST, "표준단가 없음: " + line.getComponent().getId()));

            ComponentRow row = new ComponentRow();
            row.level = level;
            row.component = line.getComponent();
            row.baseQty = baseQty;
            row.scrapRate = line.getScrapRate();
            row.unitCost = cost.getStdCost();
            out.add(row);

            dfsExplode(currentId, children, baseQty, level + 1, path, out, baseDate);
            path.remove(currentId);
        }
    }

    private static class ComponentRow {
        int level;
        Product component;
        BigDecimal baseQty;
        BigDecimal scrapRate;
        BigDecimal unitCost;
        BigDecimal explodedQty;
        BigDecimal materialCost;
    }

    private static BigDecimal scale6(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(6, java.math.RoundingMode.HALF_UP);
    }
}