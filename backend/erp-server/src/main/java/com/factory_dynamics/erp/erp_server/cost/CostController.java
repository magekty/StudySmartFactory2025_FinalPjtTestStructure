package com.factory_dynamics.erp.erp_server.cost;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/costs")
@Tag(name = "Cost", description = "원가 계산/스냅샷 API (미리보기/저장 분리)")
public class CostController {

    private final CostService svc;

    public CostController(CostService svc) {
        this.svc = svc;
    }

    @Operation(summary = "제품 기준 원가 계산 (미리보기, 저장 안 함)")
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<CostSnapshotResponse> previewByProduct(
            @PathVariable String productId,
            @RequestParam BigDecimal qty,
            @RequestParam(defaultValue = "0.100000") BigDecimal laborRate,
            @RequestParam(defaultValue = "0.100000") BigDecimal overheadRate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
    ) {
        return ResponseEntity.ok(svc.previewByProduct(productId, qty, laborRate, overheadRate, baseDate));
    }

    @Operation(summary = "생산계획 기준 원가 계산 (미리보기, 저장 안 함)")
    @GetMapping("/by-plan/{planId}")
    public ResponseEntity<CostSnapshotResponse> previewByPlan(
            @PathVariable String planId,
            @RequestParam(defaultValue = "0.100000") BigDecimal laborRate,
            @RequestParam(defaultValue = "0.100000") BigDecimal overheadRate
    ) {
        return ResponseEntity.ok(svc.previewByPlan(planId, laborRate, overheadRate));
    }

    @Operation(summary = "제품 기준 원가 스냅샷 저장")
    @PostMapping("/snapshots/by-product")
    public ResponseEntity<CostSnapshotResponse> saveByProduct(@RequestBody CostSaveByProductRequest req) {
        return ResponseEntity.ok(svc.saveByProduct(req));
    }

    @Operation(summary = "생산계획 기준 원가 스냅샷 저장")
    @PostMapping("/snapshots/by-plan")
    public ResponseEntity<CostSnapshotResponse> saveByPlan(@RequestBody CostSaveByPlanRequest req) {
        return ResponseEntity.ok(svc.saveByPlan(req));
    }
}