package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;

public record WorkOrderStatusDto(@Pattern(regexp = "P|R|C") String status, @NotNull OffsetDateTime changedAt) {
}
