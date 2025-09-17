package com.factory_dynamics.erp.erp_server.product;

public record ProductListItem(
        String productId,
        String productCode,
        String name,
        String description,
        String unit,
        boolean active
) {
    public static ProductListItem from(Product p) {
        return new ProductListItem(
                p.getId(),     // 엔티티 필드명에 맞게 수정
                p.getProductCode(),
                p.getName(),
                p.getDescription(),
                p.getUnit(),
                !p.isDeleted()
        );
    }
}