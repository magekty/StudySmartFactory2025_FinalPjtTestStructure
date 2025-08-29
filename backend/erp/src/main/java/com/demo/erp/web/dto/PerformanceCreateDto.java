package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.OffsetDateTime;

public record PerformanceCreateDto(
        @NotBlank String workOrderId, @NotBlank String itemId, @NotBlank String processId,
        @NotBlank String equipmentId, @PositiveOrZero Double goodQty, @PositiveOrZero Double defectQty,
        @NotNull OffsetDateTime startTime, @NotNull OffsetDateTime endTime) {}