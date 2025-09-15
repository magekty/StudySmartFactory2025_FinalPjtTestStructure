package com.factory_dynamics.erp.erp_server.cost;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CostSaveByProductRequest(
        String productId,
        BigDecimal qty,
        BigDecimal laborRate,
        BigDecimal overheadRate,
        LocalDate baseDate,
        String note
) {}