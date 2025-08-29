package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.OffsetDateTime;

public record PlanUpsertDto(@NotBlank String planId, @NotNull Integer planLineNo, @NotBlank String itemId,
                            @PositiveOrZero Double qty, @NotNull OffsetDateTime dueDateUtc, Integer priority) {
}
