package com.globalmed.mes.mes_api.kpi.dto;

import com.globalmed.mes.mes_api.kpi.domain.KpiDataEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record KpiDataListDto(
        Long kpiId,
        LocalDate kpiDate,
        String equipmentId,
        String processId,
        String itemId,
        BigDecimal actualOee,
        BigDecimal actualProductivity,
        BigDecimal actualYield,
        BigDecimal actualDefectRate,
        Long aggregationType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String workOrderId,
        String createdBy,
        LocalDateTime createdAt,
        String modifiedBy,
        LocalDateTime modifiedAt,
        String batchGroupKey,
        Long calcStatusCodeId,
        LocalDateTime calcAt,
        boolean deleted,
        OffsetDateTime deletedAt
) {
    public static KpiDataListDto fromEntity(KpiDataEntity entity) {
        return new KpiDataListDto(
                entity.getKpiId(),
                entity.getKpiDate(),
                entity.getEquipmentId(),
                entity.getProcessId(),
                entity.getItemId(),
                entity.getActualOee(),
                entity.getActualProductivity(),
                entity.getActualYield(),
                entity.getActualDefectRate(),
                entity.getAggregationTypeId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getWorkOrderId(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getModifiedBy(),
                entity.getModifiedAt(),
                entity.getBatchGroupKey(),
                entity.getCalcStatusCodeId(),
                entity.getCalcAt(),
                entity.isDeleted(),
                entity.getDeletedAt()
        );
    }
}