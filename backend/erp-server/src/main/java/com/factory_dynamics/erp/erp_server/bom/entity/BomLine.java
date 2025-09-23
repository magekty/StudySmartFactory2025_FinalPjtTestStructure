// src/main/java/com/factory_dynamics/erp/erp_server/bom/BomLine.java
package com.factory_dynamics.erp.erp_server.bom.entity;

import com.factory_dynamics.erp.erp_server.common.AuditableEntity;
import com.factory_dynamics.erp.erp_server.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "tb_bom_line",
        indexes = {
                @Index(name = "idx_bom_line_bom", columnList = "bom_id"),
                @Index(name = "idx_bom_line_parent", columnList = "parent_line_id"),
                @Index(name = "idx_bom_line_component", columnList = "component_product_id"),
                @Index(name = "idx_bom_line_is_deleted", columnList = "is_deleted")
        }
        // 기존 uq_bom_parent_component는 DB에서 드롭하고, active_key 포함 유니크 인덱스로 교체함
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BomLine extends AuditableEntity {

    @Id
    @Column(name = "bom_line_id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bom_line_header"))
    private BomHeader bom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_line_id", foreignKey = @ForeignKey(name = "fk_bom_line_parent"))
    private BomLine parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bom_line_component_product"))
    private Product component;

    @Column(name = "qty", precision = 18, scale = 6, nullable = false)
    private BigDecimal qty;

    @Column(name = "scrap_rate", precision = 9, scale = 6, nullable = false)
    private BigDecimal scrapRate;

    @Column(name = "note", length = 255)
    private String note;

    @Builder.Default
    @Column(name = "active_key", nullable = false, length = 1)
    private String activeKey = "A"; // 활성은 'A', 소프트삭제 시 'Z'

    // 편의 메서드: 소프트삭제/복구 시 active_key 동기화
    public void markDeleted(String actor) {
        setDeleted(true);
        setDeletedAt(LocalDateTime.now(ZoneOffset.UTC));
        setModifiedAt(getDeletedAt());
        setModifiedBy(actor);
        this.activeKey = "Z";
    }

    public void reviveAsActive(String actor) {
        setDeleted(false);
        setDeletedAt(null);
        setModifiedAt(LocalDateTime.now(ZoneOffset.UTC));
        setModifiedBy(actor);
        this.activeKey = "A";
    }
}