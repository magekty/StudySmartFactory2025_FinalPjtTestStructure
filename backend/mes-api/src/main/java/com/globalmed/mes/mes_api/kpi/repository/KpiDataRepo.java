package com.globalmed.mes.mes_api.kpi.repository;

import com.globalmed.mes.mes_api.kpi.domain.KpiDataEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface KpiDataRepo extends JpaRepository<KpiDataEntity, Long> {

    // 일일 배치 KPI 조회를 위해 추가된 메서드
    @Query("SELECT k FROM KpiDataEntity k WHERE k.kpiDate = :kpiDate " +
            "AND k.equipmentId = :equipmentId AND k.processId = :processId " +
            "AND k.itemId = :itemId AND k.aggregationTypeId = :aggregationTypeId " +
            "AND k.batchGroupKey = :batchGroupKey AND k.deleted = false")
    Optional<KpiDataEntity> findDailyBatchKpi(
            @Param("kpiDate") LocalDate kpiDate,
            @Param("equipmentId") String equipmentId,
            @Param("processId") String processId,
            @Param("itemId") String itemId,
            @Param("aggregationTypeId") Long aggregationTypeId,
            @Param("batchGroupKey") String batchGroupKey
    );

    // 실시간 KPI를 위해 추가된 메서드
    @Query("SELECT k FROM KpiDataEntity k WHERE k.workOrderId = :workOrderId " +
            "AND k.equipmentId = :equipmentId AND k.processId = :processId " +
            "AND k.itemId = :itemId AND k.aggregationTypeId = :aggregationTypeId AND k.deleted = false")
    Optional<KpiDataEntity> findRealtimeKpiByWorkOrderId(
            @Param("workOrderId") String workOrderId,
            @Param("equipmentId") String equipmentId,
            @Param("processId") String processId,
            @Param("itemId") String itemId,
            @Param("aggregationTypeId") Long aggregationTypeId
    );
    // 컨트롤러에서 날짜 필터를 사용하지 않을 때 호출되는 메서드
    @Query("SELECT k FROM KpiDataEntity k WHERE " +
            "(:equipmentId IS NULL OR k.equipmentId = :equipmentId) AND " +
            "(:processId IS NULL OR k.processId = :processId) AND " +
            "(:itemId IS NULL OR k.itemId = :itemId) AND " +
            "(:aggregationTypeId IS NULL OR k.aggregationTypeId = :aggregationTypeId) AND k.deleted = false")
    Page<KpiDataEntity> findByFilters(
            @Param("equipmentId") String equipmentId,
            @Param("processId") String processId,
            @Param("itemId") String itemId,
            @Param("aggregationTypeId") Long aggregationTypeId,
            Pageable pageable
    );

    // 컨트롤러에서 날짜 필터를 사용할 때 호출되는 메서드
    @Query("SELECT k FROM KpiDataEntity k WHERE " +
            "k.kpiDate = :kpiDate AND " +
            "(:equipmentId IS NULL OR k.equipmentId = :equipmentId) AND " +
            "(:processId IS NULL OR k.processId = :processId) AND " +
            "(:itemId IS NULL OR k.itemId = :itemId) AND " +
            "(:aggregationTypeId IS NULL OR k.aggregationTypeId = :aggregationTypeId) AND k.deleted = false")
    Page<KpiDataEntity> findByKpiDateAndFilters(
            @Param("kpiDate") LocalDate kpiDate,
            @Param("equipmentId") String equipmentId,
            @Param("processId") String processId,
            @Param("itemId") String itemId,
            @Param("aggregationTypeId") Long aggregationTypeId,
            Pageable pageable
    );
}
