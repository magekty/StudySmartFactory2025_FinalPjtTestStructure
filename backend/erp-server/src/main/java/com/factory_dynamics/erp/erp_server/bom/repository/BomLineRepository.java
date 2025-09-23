package com.factory_dynamics.erp.erp_server.bom.repository;

import com.factory_dynamics.erp.erp_server.bom.entity.BomLine;
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
      select l from BomLine l
      where l.parent.id = :parentLineId and l.deleted = false
    """)
    List<BomLine> findChildren(String parentLineId);

    @Query("""
      select l from BomLine l
      where l.bom.id = :bomId
        and ((:parentId is null and l.parent is null) or (:parentId is not null and l.parent.id = :parentId))
        and l.component.id = :componentId
    """)
    List<BomLine> findAllByBusinessKey(String bomId, String parentId, String componentId);

    @Query("""
      select l from BomLine l
      join fetch l.component c
      where l.bom.id = :bomId and l.deleted = false
    """)
    List<BomLine> findLinesWithComponent(String bomId);
}