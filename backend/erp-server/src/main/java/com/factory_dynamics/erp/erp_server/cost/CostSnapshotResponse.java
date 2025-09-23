package com.factory_dynamics.erp.erp_server.cost;

import com.factory_dynamics.erp.erp_server.cost.entity.CostSnapshot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CostSnapshotResponse(
        String snapshotId,
        String planId,
        String productId,
        BigDecimal qty,
        BigDecimal totalMaterial,
        BigDecimal labor,
        BigDecimal overhead,
        BigDecimal totalCost,
        BigDecimal laborRate,
        BigDecimal overheadRate,
        String method,
        LocalDateTime calculatedAt,
        String note,
        List<CostSnapshotDetailResponse> details
) {
    public static CostSnapshotResponse of(CostSnapshot s, List<CostSnapshotDetailResponse> details) {
        return new CostSnapshotResponse(
                s.getId(),
                s.getPlan() == null ? null : s.getPlan().getId(),
                s.getProduct() == null ? null : s.getProduct().getId(),
                s.getQty(),
                s.getTotalMaterial(),
                s.getLabor(),
                s.getOverhead(),
                s.getTotalCost(),
                s.getLaborRate(),
                s.getOverheadRate(),
                s.getMethod(),
                s.getCalculatedAt(),
                s.getNote(),
                details
        );
    }
}