package com.globalmed.mes.mes_api.performance.dto;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;
import com.globalmed.mes.mes_api.item.ItemEntity;
import com.globalmed.mes.mes_api.performance.domain.ProductionPerformanceEntity;
import com.globalmed.mes.mes_api.process.domain.ProcessEntity;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PerformanceListDto(
        // WorkOrder 정보
        String workOrderId,
        String workOrderPlanId,
        String workOrderNumber,
        BigDecimal workOrderorderQty,
        BigDecimal workOrderproducedQty,
        LocalDateTime workOrderstartTs,
        LocalDateTime workOrderendTs,
        Long workOrderCodeId,
        String workOrderCode,
        String workOrderCodeName,
        String workOrderCreatedBy,
        LocalDateTime workOrderCreatedAt,
        LocalDateTime workOrderModifiedAt,

        // Item 정보
        String itemId,
        String itemCode,
        String itemName,
        String itemType,
        String itemUnit,
        String itemDescription,
        boolean itemIsDeleted,
        LocalDateTime itemDeletedAt,
        String itemCreatedBy,
        LocalDateTime itemCreatedAt,
        String itemModifiedBy,
        LocalDateTime itemModifiedAt,

        // Process 정보
        String processId,
        String processName,
        String processDescription,
        boolean processIsDeleted,
        LocalDateTime processDeletedAt,
        String processCreatedBy,
        LocalDateTime processCreatedAt,
        String processModifiedBy,
        LocalDateTime processModifiedAt,

        // Equipment 정보
        String equipmentId,
        String equipmentName,
        Long equipCodeId,
        String equipCode,
        String equipCodeName,
        boolean equipmentIsDeleted,
        LocalDateTime equipmentDeletedAt,
        String equipmentCreatedBy,
        LocalDateTime equipmentCreatedAt,
        String equipmentModifiedBy,
        LocalDateTime equipmentModifiedAt,

        // Performance 정보
        BigDecimal perfProducedQty,
        BigDecimal perfDefectQty,
        LocalDateTime perfstartTime,
        LocalDateTime perfendTime,
        String perfworkerId,
        String performanceCreatedBy,
        Long performanceId,
        String perfrequestId
) {
    public static PerformanceListDto fromEntity(ProductionPerformanceEntity e) {
        WorkOrderEntity wo = e.getWorkOrder();
        ItemEntity item = e.getItem();
        ProcessEntity process = e.getProcess();
        EquipmentEntity equip = e.getEquipment();
        CodeEntity woCode = wo.getStatusCode();
        CodeEntity equipCode = equip.getStatusCode();

        return new PerformanceListDto(
                wo.getWorkOrderId(),
                wo.getPlanId(),
                wo.getWorkOrderNumber(),
                wo.getOrderQty(),
                wo.getProducedQty(),
                wo.getStartTs(),
                wo.getEndTs(),
                woCode.getCodeId(),
                woCode.getCode(),
                woCode.getName(),
                wo.getCreatedBy(),
                wo.getCreatedAt(),
                wo.getModifiedAt(),

                // Item
                item.getItemId(),
                item.getItemCode(),
                item.getItemName(),
                item.getItemType(),
                item.getUnit(),
                item.getDescription(),
                item.isDeleted(),
                item.getDeletedAt(),
                item.getCreatedBy(),
                item.getCreatedAt(),
                item.getModifiedBy(),
                item.getModifiedAt(),

                // Process
                process.getProcessId(),
                process.getProcessName(),
                process.getDescription(),
                process.isDeleted(),
                process.getDeletedAt(),
                process.getCreatedBy(),
                process.getCreatedAt(),
                process.getModifiedBy(),
                process.getModifiedAt(),

                // Equipment
                equip.getEquipmentId(),
                equip.getEquipmentName(),
                equipCode.getCodeId(),
                equipCode.getCode(),
                equipCode.getName(),
                equip.isDeleted(),
                equip.getDeletedAt(),
                equip.getCreatedBy(),
                equip.getCreatedAt(),
                equip.getModifiedBy(),
                equip.getModifiedAt(),

                // Performance
                e.getProducedQty(),
                e.getDefectQty(),
                e.getStartTime(),
                e.getEndTime(),
                e.getWorkerId(),
                e.getCreatedBy(),
                e.getPerformanceId(),
                e.getRequestId()
        );
    }
}