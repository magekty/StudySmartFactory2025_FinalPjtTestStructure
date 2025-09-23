package com.factory_dynamics.erp.erp_server.cost;

import com.factory_dynamics.erp.erp_server.cost.entity.CostSnapshotDetail;

import java.math.BigDecimal;

public record CostSnapshotDetailResponse(
        Long snapshotDetailId,
        String componentProductId,
        String componentCode,
        String componentName,
        Integer level,
        BigDecimal baseQty,
        BigDecimal scrapRate,
        BigDecimal explodedQty,
        BigDecimal unitCost,
        BigDecimal materialCost
) {
    public static CostSnapshotDetailResponse of(CostSnapshotDetail d) {
        return new CostSnapshotDetailResponse(
                d.getId(),
                d.getComponent() == null ? null : d.getComponent().getId(),
                d.getComponent() == null ? null : d.getComponent().getProductCode(),
                d.getComponent() == null ? null : d.getComponent().getName(),
                d.getLevel(),
                d.getBaseQty(),
                d.getScrapRate(),
                d.getExplodedQty(),
                d.getUnitCost(),
                d.getMaterialCost()
        );
    }
}