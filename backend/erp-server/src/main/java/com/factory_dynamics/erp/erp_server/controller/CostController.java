package com.factory_dynamics.erp.erp_server.controller;

import com.factory_dynamics.erp.erp_server.service.CostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cost")
public class CostController {

    private final CostService costService;

    @GetMapping("/{planId}")
    public ResponseEntity<BigDecimal> calculateCostForPlan(
            @PathVariable Integer planId,
            @RequestParam BigDecimal laborCostRate,
            @RequestParam BigDecimal manufacturingOverheadRate) {
        try {
            BigDecimal totalCost = costService.calculateCostForPlan(planId, laborCostRate, manufacturingOverheadRate);
            return new ResponseEntity<>(totalCost, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}