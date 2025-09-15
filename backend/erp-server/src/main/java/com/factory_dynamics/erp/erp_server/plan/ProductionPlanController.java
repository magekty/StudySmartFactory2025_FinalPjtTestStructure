package com.factory_dynamics.erp.erp_server.plan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/plans")
@Tag(name = "Plan", description = "생산 계획 API")
public class ProductionPlanController {

    private final ProductionPlanService svc;

    public ProductionPlanController(ProductionPlanService svc) {
        this.svc = svc;
    }

    @Operation(summary = "생산 계획 생성")
    @PostMapping
    public ResponseEntity<ProductionPlan> create(
            @RequestParam @NotBlank String planCode,
            @RequestParam @NotBlank String productId,
            @RequestParam @NotNull BigDecimal qty,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam @NotBlank String status,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(svc.create(planCode, productId, qty, startDate, endDate, status, note, "system"));
    }
}