// plan/ProductionPlanRepository.java
package com.factory_dynamics.erp.erp_server.plan;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, String> {

    Optional<ProductionPlan> findByPlanCodeAndDeletedFalse(String planCode);

    @Query("""
        select p from ProductionPlan p
        where p.deleted = false
          and (:q = '' or lower(p.planCode) like lower(concat('%', :q, '%'))
               or lower(p.product.productCode) like lower(concat('%', :q, '%'))
               or lower(p.product.name) like lower(concat('%', :q, '%')))
        and (:status = 'ALL' or upper(p.status) = upper(:status))
        and (:from is null or p.startDate >= :from)
        and (:to is null or p.endDate <= :to)
        """)
    Page<ProductionPlan> search(
            @Param("q") String q,
            @Param("status") String status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );
}