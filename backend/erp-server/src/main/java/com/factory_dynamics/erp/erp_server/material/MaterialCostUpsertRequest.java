package com.factory_dynamics.erp.erp_server.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MaterialCostUpsertRequest(
        @NotBlank String productId,
        @NotNull BigDecimal stdCost,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo
) {}