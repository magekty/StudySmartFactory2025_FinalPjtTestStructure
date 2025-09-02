// src/main/java/com/globalmed/mes/mes_api/plan/repository/ProductionPlanRepository.java
package com.globalmed.mes.mes_api.plan.repository;

import com.globalmed.mes.mes_api.plan.domain.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, String> {}