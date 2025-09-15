// src/main/java/com/factory_dynamics/erp/erp_server/cost/CostController.java
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
@Tag(name = "Cost", description = "원가 조회 API")
public class CostController {

    private final CostService svc;

    public CostController(CostService svc) {
        this.svc = svc;
    }

    @Operation(summary = "생산계획 기준 원가 계산")
    @GetMapping("/by-plan/{planId}")
    public ResponseEntity<CostSnapshotResponse> costByPlan(
            @PathVariable String planId,
            @RequestParam(defaultValue = "0.100000") BigDecimal laborRate,
            @RequestParam(defaultValue = "0.100000") BigDecimal overheadRate
    ) {
        return ResponseEntity.ok(svc.calculateByPlan(planId, laborRate, overheadRate));
    }

    @Operation(summary = "제품 기준 원가 계산")
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<CostSnapshotResponse> costByProduct(
            @PathVariable String productId,
            @RequestParam BigDecimal qty,
            @RequestParam(defaultValue = "0.100000") BigDecimal laborRate,
            @RequestParam(defaultValue = "0.100000") BigDecimal overheadRate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
    ) {
        return ResponseEntity.ok(svc.calculateByProduct(productId, qty, laborRate, overheadRate, baseDate));
    }
}