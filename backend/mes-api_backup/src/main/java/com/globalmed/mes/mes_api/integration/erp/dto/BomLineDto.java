// src/main/java/com/globalmed/mes/mes_api/integration/erp/dto/BomLineDto.java
package com.globalmed.mes.mes_api.integration.erp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BomLineDto(
        @JsonAlias({"line_no", "lineNo"}) Integer lineNo,
        @JsonAlias({"component_id", "componentId", "component_item_id"}) String componentId,
        @JsonAlias({"qty", "qty_per"}) BigDecimal qty,
        @JsonAlias({"uom"}) String uom,
        @JsonAlias({"scrap_rate"}) BigDecimal scrapRate,
        @JsonAlias({"is_deleted"}) Boolean isDeleted,
        @JsonAlias({"updated_at", "updatedAt"}) OffsetDateTime updatedAt
) {}