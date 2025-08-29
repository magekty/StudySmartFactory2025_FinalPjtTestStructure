package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BackflushDto(@NotBlank String workOrderId, @NotNull java.util.List<BackflushLineDto> lines) {
}
