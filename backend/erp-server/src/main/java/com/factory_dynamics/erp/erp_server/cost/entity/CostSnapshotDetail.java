package com.factory_dynamics.erp.erp_server.cost.entity;

import com.factory_dynamics.erp.erp_server.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_cost_snapshot_detail",
        indexes = {
                @Index(name = "idx_cost_detail_snapshot", columnList = "snapshot_id"),
                @Index(name = "idx_cost_detail_component", columnList = "component_product_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CostSnapshotDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cost_detail_snapshot"))
    private CostSnapshot snapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cost_detail_component_product"))
    private Product component;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "base_qty", precision = 18, scale = 6, nullable = false)
    private BigDecimal baseQty;

    @Column(name = "scrap_rate", precision = 9, scale = 6, nullable = false)
    private BigDecimal scrapRate;

    @Column(name = "exploded_qty", precision = 18, scale = 6, nullable = false)
    private BigDecimal explodedQty;

    @Column(name = "unit_cost", precision = 18, scale = 6, nullable = false)
    private BigDecimal unitCost;

    @Column(name = "material_cost", precision = 18, scale = 6, nullable = false)
    private BigDecimal materialCost;
}