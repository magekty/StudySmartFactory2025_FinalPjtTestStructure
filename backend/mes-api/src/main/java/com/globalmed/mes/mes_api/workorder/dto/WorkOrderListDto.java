package com.globalmed.mes.mes_api.workorder.dto;

import java.math.BigDecimal;

public record WorkOrderListDto(
        String workOrderId,
        String workOrderNumber,
        String itemId,
        String processId,
        String equipmentId,
        BigDecimal orderQty,
        BigDecimal producedQty,
        String status     // CodeEntity.code
) {}