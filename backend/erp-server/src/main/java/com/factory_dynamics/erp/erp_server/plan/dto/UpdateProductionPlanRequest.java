package com.factory_dynamics.erp.erp_server.plan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductionPlanRequest(
        BigDecimal qty,
        LocalDate startDate,
        LocalDate endDate,
        String note,
        long version
) {}