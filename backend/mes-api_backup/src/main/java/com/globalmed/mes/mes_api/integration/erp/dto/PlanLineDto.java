// MES: src/main/java/com/globalmed/mes/mes_api/integration/erp/dto/PlanLineDto.java
package com.globalmed.mes.mes_api.integration.erp.dto;

import java.time.OffsetDateTime;

public record PlanLineDto(
        String planId,
        Integer planLineNo,
        String itemId,
        Double qty,
        OffsetDateTime dueDateUtc,
        Integer priority,
        String unit,
        boolean isDeleted,
        OffsetDateTime updatedAt
) {}