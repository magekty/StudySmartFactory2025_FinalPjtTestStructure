package com.globalmed.mes.mes_api.workorder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record WorkOrderDetailDto(
        String workOrderId, String workOrderNumber,
        String itemId, String processId, String equipmentId,
        BigDecimal orderQty, BigDecimal producedQty,
        String status,
        OffsetDateTime startTs,      // UTC
        OffsetDateTime createdAt,    // UTC
        OffsetDateTime modifiedAt    // UTC
        ) {}