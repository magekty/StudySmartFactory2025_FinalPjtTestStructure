package com.factory_dynamics.erp.erp_server.controller;

import com.factory_dynamics.erp.erp_server.domain.ProductionPlan;
import com.factory_dynamics.erp.erp_server.service.ProductionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plans")
public class ProductionPlanController {

    private final ProductionPlanService productionPlanService;

    @PostMapping
    public ResponseEntity<ProductionPlan> createProductionPlan(@RequestBody ProductionPlan productionPlan) {
        try {
            ProductionPlan createdPlan = productionPlanService.createProductionPlan(productionPlan);
            return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ProductionPlan> getProductionPlanById(@PathVariable Integer planId) {
        Optional<ProductionPlan> plan = productionPlanService.getProductionPlanById(planId);
        return plan.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ProductionPlan>> getAllProductionPlans() {
        List<ProductionPlan> plans = productionPlanService.getAllProductionPlans();
        if (plans.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(plans, HttpStatus.OK);
    }
}