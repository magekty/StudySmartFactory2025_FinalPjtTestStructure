package com.factory_dynamics.erp.erp_server.product;

public record ProductResponse(
        String id,
        String productCode,
        String name,
        String type,
        String unit,
        String description
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getProductCode(), p.getName(), p.getType(), p.getUnit(), p.getDescription()
        );
    }
}