package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ItemDto(
        String itemId,
        String itemCode,
        String unit,
        String itemType,      // 'R' | 'P' | 'F'
        OffsetDateTime updatedAt,
        boolean isDeleted
) {}