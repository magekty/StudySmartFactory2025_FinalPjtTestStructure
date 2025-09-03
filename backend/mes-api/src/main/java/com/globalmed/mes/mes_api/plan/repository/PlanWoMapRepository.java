// src/main/java/com/globalmed/mes/mes_api/plan/repository/PlanWoMapRepository.java
package com.globalmed.mes.mes_api.plan.repository;

import com.globalmed.mes.mes_api.plan.domain.PlanWoMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface PlanWoMapRepository extends JpaRepository<PlanWoMap, Long> {

    @Query("select coalesce(sum(m.issueQty),0) from PlanWoMap m where m.planId=:planId and m.planLineNo=:lineNo and (m.status is null or m.status='ISSUED')")
    BigDecimal sumIssuedQty(String planId, Integer lineNo);

    boolean existsByPlanIdAndPlanLineNo(String planId, Integer lineNo);
}