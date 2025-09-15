package com.factory_dynamics.erp.erp_server.bom;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BomLineCreateRequest(
        @NotBlank String bomId,
        String parentLineId,
        @NotBlank String componentProductId,
        @NotNull(message = "수량은 필수입니다")
        @DecimalMin(value = "0.000001", message = "수량은 0보다 커야 합니다")
        BigDecimal qty,
        @NotNull BigDecimal scrapRate,
        String note
) {}