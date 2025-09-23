// plan/controller/ProductionPlanController.java
package com.factory_dynamics.erp.erp_server.plan.controller;

import com.factory_dynamics.erp.erp_server.plan.ProductionPlanService;
import com.factory_dynamics.erp.erp_server.plan.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/plans")
@Tag(name = "Plan", description = "생산 계획 API")
public class ProductionPlanController {

    private final ProductionPlanService svc;

    public ProductionPlanController(ProductionPlanService svc) {
        this.svc = svc;
    }

    @Operation(summary = "목록 조회")
    @GetMapping
    public ResponseEntity<Page<ProductionPlanSummaryDto>> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        var sortObj = Sort.by(sort.split(",")[1].equalsIgnoreCase("desc") ? Sort.Order.desc(sort.split(",")[0]) : Sort.Order.asc(sort.split(",")[0]));
        return ResponseEntity.ok(svc.list(q, status, from, to, page, size, sortObj));
    }

    @Operation(summary = "상세 조회")
    @GetMapping("/{planId}")
    public ResponseEntity<ProductionPlanDetailDto> get(@PathVariable String planId) {
        return ResponseEntity.ok(svc.get(planId));
    }

    @Operation(summary = "생성")
    @PostMapping
    public ResponseEntity<ProductionPlanDetailDto> create(@RequestBody CreateProductionPlanRequest req) {
        return ResponseEntity.ok(svc.create(req, "system"));
    }

    @Operation(summary = "수정")
    @PutMapping("/{planId}")
    public ResponseEntity<ProductionPlanDetailDto> update(@PathVariable String planId, @RequestBody UpdateProductionPlanRequest req) {
        return ResponseEntity.ok(svc.update(planId, req, "system"));
    }

    @Operation(summary = "상태 전이")
    @PatchMapping("/{planId}/status")
    public ResponseEntity<ProductionPlanDetailDto> changeStatus(@PathVariable String planId, @RequestBody ChangeStatusRequest req) {
        return ResponseEntity.ok(svc.changeStatus(planId, req));
    }

    @Operation(summary = "삭제(논리)")
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> delete(@PathVariable String planId) {
        svc.softDelete(planId, "system");
        return ResponseEntity.noContent().build();
    }
}