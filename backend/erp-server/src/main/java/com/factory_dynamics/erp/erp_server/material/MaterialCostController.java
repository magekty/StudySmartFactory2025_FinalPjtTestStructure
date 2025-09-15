package com.factory_dynamics.erp.erp_server.material;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/material-costs")
@Tag(name = "MaterialCost", description = "표준단가 관리 API")
public class MaterialCostController {

    private final MaterialCostService svc;
    private final MaterialCostRepository repo;

    public MaterialCostController(MaterialCostService svc, MaterialCostRepository repo) {
        this.svc = svc;
        this.repo = repo;
    }

    @Operation(summary = "표준단가 등록")
    @PostMapping
    public ResponseEntity<MaterialCost> upsert(@Valid @RequestBody MaterialCostUpsertRequest req) {
        return ResponseEntity.ok(svc.upsert(req, "system"));
    }

    @Operation(summary = "기준일 표준단가 조회")
    @GetMapping("/effective")
    public ResponseEntity<MaterialCost> effective(@RequestParam String productId,
                                                  @RequestParam(required = false) LocalDate baseDate) {
        var date = baseDate == null ? LocalDate.now() : baseDate;
        return ResponseEntity.of(repo.findEffectiveCost(productId, date));
    }
}