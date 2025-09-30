// src/main/java/com/globalmed/mes/mes_api/integration/erp/dto/BomHeaderDto.java
package com.globalmed.mes.mes_api.integration.erp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.OffsetDateTime;
import java.util.List;

public record BomHeaderDto(
        @JsonAlias({"item_id", "itemId"}) String itemId,
        @JsonAlias({"revision"}) String revision,
        @JsonAlias({"alt_code", "altCode"}) String altCode,
        @JsonAlias({"description"}) String description,
        @JsonAlias({"eff_from", "effective_from_utc", "effectiveFromUtc"}) OffsetDateTime effectiveFromUtc,
        @JsonAlias({"eff_to", "effective_to_utc", "effectiveToUtc"}) OffsetDateTime effectiveToUtc,
        @JsonAlias({"is_deleted"}) Boolean isDeleted,
        @JsonAlias({"updated_at", "updatedAt"}) OffsetDateTime updatedAt,
        @JsonAlias({"lines"}) List<BomLineDto> lines
) {}