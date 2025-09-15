package com.factory_dynamics.erp.erp_server.bom;

import java.math.BigDecimal;

public record BomLineResponse(
        String bomLineId,
        String bomId,
        String parentLineId,
        String componentProductId,
        String componentCode,
        String componentName,
        BigDecimal qty,
        BigDecimal scrapRate,
        String note
) {
    public static BomLineResponse from(BomLine l) {
        return new BomLineResponse(
                l.getId(),
                l.getBom().getId(),
                l.getParent() == null ? null : l.getParent().getId(),
                l.getComponent().getId(),
                l.getComponent().getProductCode(),
                l.getComponent().getName(),
                l.getQty(),
                l.getScrapRate(),
                l.getNote()
        );
    }
}