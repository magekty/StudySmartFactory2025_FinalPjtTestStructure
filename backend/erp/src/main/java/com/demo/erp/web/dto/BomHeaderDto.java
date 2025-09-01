package com.demo.erp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record BomHeaderDto(
        @NotBlank String bomId, @NotBlank String itemId, @NotBlank String revision,
        @NotNull OffsetDateTime effFrom, OffsetDateTime effTo,
        @NotNull OffsetDateTime updatedAt, boolean isDeleted,
        @NotNull List<BomLineDto> lines) {}