package com.factory_dynamics.erp.erp_server.bom;

import com.factory_dynamics.erp.erp_server.common.AuditableEntity;
import com.factory_dynamics.erp.erp_server.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_bom_line",
        indexes = {
                @Index(name = "idx_bom_line_bom", columnList = "bom_id"),
                @Index(name = "idx_bom_line_parent", columnList = "parent_line_id"),
                @Index(name = "idx_bom_line_component", columnList = "component_product_id"),
                @Index(name = "idx_bom_line_is_deleted", columnList = "is_deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_bom_parent_component", columnNames = {"bom_id","parent_line_id","component_product_id"})
        })
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
}