package com.globalmed.mes.mes_api.kpi.downtime.repository;

import com.globalmed.mes.mes_api.kpi.downtime.domain.PlannedDowntimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface PlannedDowntimeRepo extends JpaRepository<PlannedDowntimeEntity, Long> {

    @Query("SELECT p FROM PlannedDowntimeEntity p WHERE p.equipmentId = :equipmentId " +
            "AND p.isDeleted = false " +
            "AND p.startTime <= :endTs AND p.endTime >= :startTs")
    List<PlannedDowntimeEntity> findByEquipmentIdAndOverlappingDateRange(
            @Param("equipmentId") String equipmentId,
            @Param("startTs") OffsetDateTime startTs,
            @Param("endTs") OffsetDateTime endTs
    );
}
