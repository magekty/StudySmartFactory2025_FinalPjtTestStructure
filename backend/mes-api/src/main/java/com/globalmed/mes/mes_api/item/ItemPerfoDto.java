package com.globalmed.mes.mes_api.item;

import java.time.LocalDateTime;

public record ItemPerfoDto(
        String itemId,
        String itemCode,
        String itemName,
        String itemType,
        String unit,
        String description,
        boolean isDeleted,
        LocalDateTime deletedAt,
        String createdBy,
        LocalDateTime createdAt,
        String modifiedBy,
        LocalDateTime modifiedAt
) {
    public static ItemPerfoDto fromEntity(ItemEntity e) {
        if (e == null) return null;
        return new ItemPerfoDto(
                e.getItemId(),
                e.getItemCode(),
                e.getItemName(),
                e.getItemType(),
                e.getUnit(),
                e.getDescription(),
                e.isDeleted(),
                e.getDeletedAt(),
                e.getCreatedBy(),
                e.getCreatedAt(),
                e.getModifiedBy(),
                e.getModifiedAt()
        );
    }
}
