package com.globalmed.mes.mes_api.workorder.dto;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.code.dto.CodeDto;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkOrderPerfoDto(
        String workOrderId,
        String planId,
        String workOrderNumber,
        BigDecimal orderQty,
        BigDecimal producedQty,
        LocalDateTime startTs,
        LocalDateTime endTs,
        CodeDto statusCode,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static WorkOrderPerfoDto fromEntity(WorkOrderEntity wo){
        return new WorkOrderPerfoDto(
                wo.getWorkOrderId(),
                wo.getPlanId(),
                wo.getWorkOrderNumber(),
                wo.getOrderQty(),
                wo.getProducedQty(),
                wo.getStartTs(),
                wo.getEndTs(),
                CodeDto.fromEntity(wo.getStatusCode()),
                wo.getCreatedBy(),
                wo.getCreatedAt(),
                wo.getModifiedAt()
        );
    }
}
