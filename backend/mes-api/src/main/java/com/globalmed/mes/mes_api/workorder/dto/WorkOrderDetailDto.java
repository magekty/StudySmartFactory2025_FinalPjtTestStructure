package com.globalmed.mes.mes_api.workorder.dto;

import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record WorkOrderDetailDto(
        String workOrderId,
        String workOrderNumber,
        BigDecimal orderQty,
        BigDecimal producedQty,
        String statusCode,
        // 상태 코드 이름 추가
        OffsetDateTime startTs,
        OffsetDateTime createdAt,
        OffsetDateTime modifiedAt,
        // Item 관련 정보 추가
        String itemName,
        String itemId,
        String itemType,
        String unit,
        String itemDescription,
        // Process 관련 정보 추가
        String processId,
        String processName,
        String processDescription,
        // Equipment 관련 정보 추가
        String equipmentId,
        String equipmentName,
        String workcenterName  // UTC
        ) {public static WorkOrderDetailDto fromEntity(WorkOrderEntity entity) {
        // 엔티티에서 필요한 모든 정보를 가져와 DTO에 매핑
        String itemName = (entity.getItemId() != null) ? entity.getItemId().getItemName() : null;
        String itemId = (entity.getItemId() != null) ? entity.getItemId().getItemCode() : null;
        String itemType = (entity.getItemId() != null) ? entity.getItemId().getItemType() : null;
        String unit = (entity.getItemId() != null) ? entity.getItemId().getUnit() : null;
        String itemDescription = (entity.getItemId() != null) ? entity.getItemId().getDescription() : null;
        String processId = (entity.getProcessId() != null) ? entity.getProcessId().getProcessId() : null;
        String processName = (entity.getProcessId() != null) ? entity.getProcessId().getProcessName() : null;
        String processDescription = (entity.getProcessId() != null) ? entity.getProcessId().getDescription() : null;
        String equipmentId = (entity.getEquipmentId() != null) ? entity.getEquipmentId().getEquipmentId() : null;
        String equipmentName = (entity.getEquipmentId() != null) ? entity.getEquipmentId().getEquipmentName() : null;
        String workcenterName = (entity.getEquipmentId() != null && entity.getEquipmentId().getWorkcenter() != null)
                ? entity.getEquipmentId().getWorkcenter().getWorkcenterName() : null;
        String statusCode = (entity.getStatusCode() != null) ? entity.getStatusCode().getCode() : null;

        return new WorkOrderDetailDto(
                entity.getWorkOrderId(),
                entity.getWorkOrderNumber(),
                entity.getOrderQty(),
                entity.getProducedQty(),
                statusCode,
                entity.getStartTs() != null ? com.globalmed.mes.mes_api.common.DateTimeMapper.attachKst(entity.getStartTs()) : null,
                entity.getCreatedAt() != null ? com.globalmed.mes.mes_api.common.DateTimeMapper.attachKst(entity.getCreatedAt()) : null,
                entity.getModifiedAt() != null ? com.globalmed.mes.mes_api.common.DateTimeMapper.attachKst(entity.getModifiedAt()) : null,
                itemName, itemId, itemType, unit, itemDescription,
                processId, processName,processDescription,
                equipmentId, equipmentName, workcenterName
        );
        }
}