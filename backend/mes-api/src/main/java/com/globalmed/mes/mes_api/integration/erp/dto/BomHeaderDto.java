package com.globalmed.mes.mes_api.integration.erp.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record BomHeaderDto(String bomId, String itemId, String revision,
                           OffsetDateTime effFrom, OffsetDateTime effTo,
                           OffsetDateTime updatedAt, boolean isDeleted,
                           List<BomLineDto> lines) {}