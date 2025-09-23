package com.factory_dynamics.erp.erp_server.plan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductionPlanRequest(
        String planCode,
        String productId,
        BigDecimal qty,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String note
) {}