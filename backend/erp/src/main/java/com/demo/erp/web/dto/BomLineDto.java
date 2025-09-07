// src/main/java/com/demo/erp/web/dto/BomLineDto.java
package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record BomLineDto(
        @NotNull Integer lineNo,
        @NotBlank String componentId,
        @NotNull Double qty,
        @NotBlank String uom,
        @NotNull Double scrapRate,
        @NotNull OffsetDateTime updatedAt,
        boolean isDeleted
) {}