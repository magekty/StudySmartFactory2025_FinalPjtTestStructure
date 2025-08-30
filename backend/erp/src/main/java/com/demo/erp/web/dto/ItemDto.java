package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ItemDto(
        @NotBlank String itemId, @NotBlank String uom, @NotNull OffsetDateTime updatedAt, boolean isDeleted) {}