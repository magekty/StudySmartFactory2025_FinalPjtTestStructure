package com.globalmed.mes.mes_api.plan.repository;


import com.globalmed.mes.mes_api.plan.domain.ProductionPlanLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductionPlanLineRepository extends JpaRepository<ProductionPlanLine, Long> {
    Optional<ProductionPlanLine> findByPlanIdAndPlanLineNo(String planId, Integer planLineNo);
}