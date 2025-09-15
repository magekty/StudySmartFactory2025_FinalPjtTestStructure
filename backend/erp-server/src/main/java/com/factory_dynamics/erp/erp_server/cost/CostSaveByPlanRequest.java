package com.factory_dynamics.erp.erp_server.cost;

import java.math.BigDecimal;

public record CostSaveByPlanRequest(
        String planId,
        BigDecimal laborRate,
        BigDecimal overheadRate,
        String note
) {}