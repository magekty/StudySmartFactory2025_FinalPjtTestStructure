package com.factory_dynamics.erp.erp_server.repository;

import com.factory_dynamics.erp.erp_server.domain.BomLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BomLineRepository extends JpaRepository<BomLine, Integer> {

    // 특정 BOM 헤더에 속한 모든 BOM 라인을 조회
    List<BomLine> findByBomHeader_BomId(Integer bomId);

    // 재귀적으로 BOM 라인 구조를 조회하는 JPQL 쿼리
    @Query("SELECT b FROM BomLine b WHERE b.bomHeader.bomId = :bomId ORDER BY b.parentLine.lineId NULLS FIRST, b.lineId")
    List<BomLine> findHierarchicalBomLines(@Param("bomId") Integer bomId);

    // 부모 라인 ID를 기준으로 하위 라인을 조회
    List<BomLine> findByParentLine_LineId(Integer parentLineId);
}