package com.globalmed.mes.mes_api.item;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_item_code", columnNames = {"item_code"}))
@Getter
@Setter
public class ItemEntity {
    @Id
    @Column(name = "item_id", length = 36, nullable = false)
    private String itemId;

    @Column(name = "item_code", length = 50, nullable = false)
    private String itemCode;

    @Column(name = "item_name", length = 255, nullable = false)
    private String itemName;

    @Column(name = "item_type", columnDefinition = "char(1)", nullable = false)
    private String itemType;

    @Column(name = "unit", length = 10, nullable = false)
    private String unit;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    @Column(name = "modified_at", insertable = false, updatable = false)
    private LocalDateTime modifiedAt;

}