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

@Service public class CostService {


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

    // 미리보기 — 저장 안 함
    public CostSnapshotResponse previewByPlan(String planId, BigDecimal laborRate, BigDecimal overheadRate) {
        ProductionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "생산계획을 찾을 수 없음: " + planId));

        CalcResult result = calcOnly(plan.getProduct(), plan.getQty(), plan.getId(), laborRate, overheadRate, LocalDate.now(ZoneOffset.UTC));
        return result.toResponse();
    }

    // 미리보기 — 저장 안 함
    public CostSnapshotResponse previewByProduct(String productId, BigDecimal qty, BigDecimal laborRate, BigDecimal overheadRate, LocalDate baseDate) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "제품 없음: " + productId));

        CalcResult result = calcOnly(product, qty, null, laborRate, overheadRate,
                baseDate == null ? LocalDate.now(ZoneOffset.UTC) : baseDate);
        return result.toResponse();
    }

    // 저장 — by-product
    @Transactional
    public CostSnapshotResponse saveByProduct(CostSaveByProductRequest req) {
        Product product = productRepo.findById(req.productId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "제품 없음: " + req.productId()));

        CalcResult result = calcOnly(product, req.qty(), null, req.laborRate(), req.overheadRate(),
                req.baseDate() == null ? LocalDate.now(ZoneOffset.UTC) : req.baseDate());

        SavedResult saved = saveSnapshot(result, req.note());
        return saved.toResponse();
    }

    // 저장 — by-plan
    @Transactional
    public CostSnapshotResponse saveByPlan(CostSaveByPlanRequest req) {
        ProductionPlan plan = planRepo.findById(req.planId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "생산계획 없음: " + req.planId()));

        CalcResult result = calcOnly(plan.getProduct(), plan.getQty(), plan.getId(), req.laborRate(), req.overheadRate(), LocalDate.now(ZoneOffset.UTC));
        SavedResult saved = saveSnapshot(result, req.note());
        return saved.toResponse();
    }

    // 계산만 수행 — 엔티티 직접 노출 금지(필요 문자열/ID를 계산 시점에 복제)
    private CalcResult calcOnly(Product product, BigDecimal planQty, String planId,
                                BigDecimal laborRate, BigDecimal overheadRate, LocalDate baseDate) {

        BomHeader bom = bomHeaderRepo.findEffectiveActiveBom(product.getId(), baseDate)
                .orElseThrow(() -> new BizException(HttpStatus.BAD_REQUEST, "유효한 활성 BOM이 없음"));

        // fetch join으로 component 즉시 로딩(보수적 안전장치)
        List<BomLine> lines = bomLineRepo.findLinesWithComponent(bom.getId());

        Map<String, List<BomLine>> children = new HashMap<>();
        for (BomLine l : lines) {
            String parentKey = (l.getParent() == null) ? "ROOT" : l.getParent().getId();
            children.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(l);
        }

        List<ComponentRow> exploded = new ArrayList<>();
        dfsExplode("ROOT", children, BigDecimal.ONE, 0, new HashSet<>(), exploded, baseDate);

        BigDecimal Q = scale6((planQty == null) ? BigDecimal.ONE : planQty);

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

        return new CalcResult(
                planId,
                product.getId(),
                Q, totalMaterial, labor, overhead, total,
                scale6(laborRate), scale6(overheadRate),
                "RATE",
                LocalDateTime.now(ZoneOffset.UTC),
                exploded
        );
    }

    // 저장 — 연관은 반드시 영속 참조 사용
    private SavedResult saveSnapshot(CalcResult calc, String note) {
        Product productRef = productRepo.getReferenceById(calc.productId());
        ProductionPlan planRef = null;
        if (calc.planId() != null) {
            planRef = planRepo.findById(calc.planId())
                    .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "생산계획 없음: " + calc.planId()));
            productRef = planRef.getProduct();
        }

        CostSnapshot snapshot = CostSnapshot.builder()
                .id(Uuids.newId())
                .plan(planRef)
                .product(productRef)
                .qty(calc.qty())
                .totalMaterial(calc.totalMaterial())
                .labor(calc.labor())
                .overhead(calc.overhead())
                .totalCost(calc.totalCost())
                .laborRate(calc.laborRate())
                .overheadRate(calc.overheadRate())
                .method(calc.method())
                .calculatedAt(calc.calculatedAt())
                .note(note)
                .build();

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        snapshot.setCreatedAt(now);
        snapshot.setModifiedAt(now);

        snapshot = snapshotRepo.save(snapshot);

        List<CostSnapshotDetail> details = new ArrayList<>();
        for (ComponentRow r : calc.rows()) {
            Product compRef = productRepo.getReferenceById(r.componentId); // 영속 참조
            CostSnapshotDetail d = CostSnapshotDetail.builder()
                    .snapshot(snapshot)
                    .component(compRef)
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

        var savedDetails = detailRepo.findAllWithComponentBySnapshotId(snapshot.getId());
        var detailDtos = savedDetails.stream().map(CostSnapshotDetailResponse::of).toList();
        return new SavedResult(snapshot, detailDtos);
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

            // 세션 안에서 필요한 값만 복제(엔티티 직접 노출 금지)
            Product comp = line.getComponent();
            String compId = comp.getId();
            String compCode = comp.getProductCode();
            String compName = comp.getName();

            MaterialCost cost = costRepo.findEffectiveCost(compId, baseDate)
                    .orElseThrow(() -> new BizException(HttpStatus.BAD_REQUEST, "표준단가 없음: " + compId));

            ComponentRow row = new ComponentRow();
            row.level = level;
            row.componentId = compId;
            row.componentCode = compCode;
            row.componentName = compName;
            row.baseQty = baseQty;
            row.scrapRate = line.getScrapRate();
            row.unitCost = cost.getStdCost();
            out.add(row);

            dfsExplode(currentId, children, baseQty, level + 1, path, out, baseDate);
            path.remove(currentId);
        }
    }

    // 내부 계산 결과(엔티티 아님)
    private record CalcResult(
            String planId,
            String productId,
            BigDecimal qty,
            BigDecimal totalMaterial,
            BigDecimal labor,
            BigDecimal overhead,
            BigDecimal totalCost,
            BigDecimal laborRate,
            BigDecimal overheadRate,
            String method,
            LocalDateTime calculatedAt,
            List<ComponentRow> rows
    ) {
        CostSnapshotResponse toResponse() {
            var detailDtos = rows.stream().map(r -> new CostSnapshotDetailResponse(
                    null,               // snapshotDetailId (미리보기)
                    r.componentId,
                    r.componentCode,
                    r.componentName,
                    r.level,
                    r.baseQty,
                    r.scrapRate,
                    r.explodedQty,
                    r.unitCost,
                    r.materialCost
            )).toList();

            return new CostSnapshotResponse(
                    null,               // snapshotId (미리보기)
                    planId,
                    productId,
                    qty,
                    totalMaterial,
                    labor,
                    overhead,
                    totalCost,
                    laborRate,
                    overheadRate,
                    method,
                    calculatedAt,
                    null,
                    detailDtos
            );
        }
    }

    private record SavedResult(CostSnapshot snapshot, List<CostSnapshotDetailResponse> details) {
        CostSnapshotResponse toResponse() {
            return CostSnapshotResponse.of(snapshot, details);
        }
    }

    private static class ComponentRow {
        int level;
        String componentId;
        String componentCode;
        String componentName;
        BigDecimal baseQty;
        BigDecimal scrapRate;
        BigDecimal unitCost;
        BigDecimal explodedQty;
        BigDecimal materialCost;
    }

    private static BigDecimal scale6(BigDecimal v) {
        return (v == null) ? BigDecimal.ZERO : v.setScale(6, java.math.RoundingMode.HALF_UP);
    }
}