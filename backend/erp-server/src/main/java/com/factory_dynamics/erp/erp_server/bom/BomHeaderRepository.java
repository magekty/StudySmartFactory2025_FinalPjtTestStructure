package com.factory_dynamics.erp.erp_server.bom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface BomHeaderRepository extends JpaRepository<BomHeader, String> {

    @Query("""
        select b from BomHeader b
        where b.product.id = :productId
          and b.deleted = false
          and b.active = true
          and b.effectiveFrom <= :baseDate
          and (b.effectiveTo is null or b.effectiveTo >= :baseDate)
        order by b.effectiveFrom desc
        """)
    Optional<BomHeader> findEffectiveActiveBom(String productId, LocalDate baseDate);
}