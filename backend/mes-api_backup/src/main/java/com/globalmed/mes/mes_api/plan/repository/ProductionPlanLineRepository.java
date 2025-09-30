package com.globalmed.mes.mes_api.plan.repository;


import com.globalmed.mes.mes_api.plan.domain.ProductionPlanLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductionPlanLineRepository extends JpaRepository<ProductionPlanLineEntity, Long> {
    Optional<ProductionPlanLineEntity> findByPlanIdAndPlanLineNo(String planId, Integer planLineNo);
}