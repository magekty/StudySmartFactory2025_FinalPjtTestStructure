package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record BackflushLineDto(@NotBlank String componentId, @PositiveOrZero Double qty, @NotBlank String uom) {
}
