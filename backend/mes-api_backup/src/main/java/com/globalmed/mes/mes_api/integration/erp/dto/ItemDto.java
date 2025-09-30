package com.globalmed.mes.mes_api.integration.erp.dto;

import java.time.OffsetDateTime;

public record ItemDto(
        String itemId,
        String itemCode,
        String unit,       // 캐노니컬 단위
        String itemType,   // 'R' | 'P' | 'F'
        OffsetDateTime updatedAt,
        boolean isDeleted
) {}