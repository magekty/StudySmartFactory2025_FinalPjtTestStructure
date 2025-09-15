package com.factory_dynamics.erp.erp_server.material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface MaterialCostRepository extends JpaRepository<MaterialCost, String> {

    @Query("""
        select c from MaterialCost c
        where c.product.id = :productId
          and c.deleted = false
          and c.effectiveFrom <= :baseDate
          and (c.effectiveTo is null or c.effectiveTo >= :baseDate)
        order by c.effectiveFrom desc
        """)
    Optional<MaterialCost> findEffectiveCost(String productId, LocalDate baseDate);
}