package com.globalmed.mes.mes_api.performance.repository;

import com.globalmed.mes.mes_api.performance.domain.ProductionPerformanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PerformanceRepo extends JpaRepository<ProductionPerformanceEntity, Long>,
        JpaSpecificationExecutor<ProductionPerformanceEntity> {
    @EntityGraph(attributePaths = {"workOrder", "item", "process", "equipment"})
    Page<ProductionPerformanceEntity> findAll(Specification<ProductionPerformanceEntity> spec, Pageable pageable);
    interface PerfAgg {
        BigDecimal getProduced(); // ← get 접두어
        BigDecimal getGood();     // ← get 접두어
    }

    @Query("""
    select COALESCE(sum(pp.producedQty), 0) as produced,
           COALESCE(sum(pp.producedQty - pp.defectQty), 0) as good
      from ProductionPerformanceEntity pp
      join pp.equipment eq
     where eq.equipmentId = :eqp
       and pp.startTime >= :fromTs
       and pp.startTime < :toTs
    """)
    PerfAgg aggregateForDay(@Param("eqp") String equipmentId,
                            @Param("fromTs") LocalDateTime fromTs,
                            @Param("toTs") LocalDateTime toTs);
    Optional<ProductionPerformanceEntity> findByRequestId(String requestId);




    @Query("select pp from ProductionPerformanceEntity pp " +
            "where pp.startTime >= :fromTs and pp.startTime < :toTs ORDER BY pp.startTime")
    List<ProductionPerformanceEntity> findPerformancesForDay(@Param("fromTs") LocalDateTime fromTs,
                                                             @Param("toTs") LocalDateTime toTs);

    /**
     * 워크 오더 ID, 설비, 공정, 품목 ID로 모든 성과 데이터를 조회
     */
    @Query("select pp from ProductionPerformanceEntity pp " +
            "join pp.workOrder wo " +
            "join pp.equipment eq " +
            "join pp.process pr " +
            "join pp.item it " +
            "where wo.workOrderId = :workOrderId " +
            "and eq.equipmentId = :equipmentId " +
            "and pr.processId = :processId " +
            "and it.itemId = :itemId " +
            "order by pp.startTime")
    List<ProductionPerformanceEntity> findPerformancesByWorkOrder(
            @Param("workOrderId") String workOrderId,
            @Param("equipmentId") String equipmentId,
            @Param("processId") String processId,
            @Param("itemId") String itemId
    );
}