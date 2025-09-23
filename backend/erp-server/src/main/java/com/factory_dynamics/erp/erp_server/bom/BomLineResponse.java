// src/main/java/com/factory_dynamics/erp/erp_server/bom/BomLineResponse.java
package com.factory_dynamics.erp.erp_server.bom;

import com.factory_dynamics.erp.erp_server.bom.entity.BomLine;

import java.math.BigDecimal;

public record BomLineResponse(
        String BomLineId,
        String BomId,
        String ParentLineId,
        String ComponentProductId,
        String ComponentCode,
        String ComponentName,
        BigDecimal Qty,
        BigDecimal ScrapRate,
        String Note
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