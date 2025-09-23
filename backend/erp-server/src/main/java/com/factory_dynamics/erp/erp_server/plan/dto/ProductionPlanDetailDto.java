package com.factory_dynamics.erp.erp_server.plan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductionPlanDetailDto(
        String planId,
        String planCode,
        String productId,
        String productCode,
        String productName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal qty,
        String status,
        String note,
        long version
) {}