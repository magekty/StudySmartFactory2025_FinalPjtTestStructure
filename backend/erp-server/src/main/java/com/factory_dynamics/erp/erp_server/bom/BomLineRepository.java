package com.factory_dynamics.erp.erp_server.bom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BomLineRepository extends JpaRepository<BomLine, String> {
    List<BomLine> findByBom_IdAndDeletedFalse(String bomId);

    @Query("""
        select l from BomLine l
        join fetch l.bom b
        left join fetch l.parent p
        join fetch l.component c
        where b.id = :bomId
          and l.deleted = false
        """)
    List<BomLine> findWithComponentByBomId(String bomId);

    @Query(""" 
            select l from BomLine l join fetch l.component c
             where l.bom.id = :bomId and l.deleted = false
            """)
    List findLinesWithComponent(String bomId);
}