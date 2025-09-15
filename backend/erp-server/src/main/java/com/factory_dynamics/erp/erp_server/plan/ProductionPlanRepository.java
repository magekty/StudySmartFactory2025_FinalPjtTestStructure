package com.factory_dynamics.erp.erp_server.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, String> {
    Optional<ProductionPlan> findByPlanCodeAndDeletedFalse(String planCode);
}