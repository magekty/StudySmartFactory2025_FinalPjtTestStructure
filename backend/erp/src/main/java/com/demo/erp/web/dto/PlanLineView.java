package com.demo.erp.web.dto;

import java.time.OffsetDateTime;

public record PlanLineView(
        String planId,
        Integer planLineNo,
        String itemId,
        Double qty,
        OffsetDateTime dueDateUtc,
        Integer priority,
        String unit,
        boolean isDeleted,
        OffsetDateTime updatedAt
) {}