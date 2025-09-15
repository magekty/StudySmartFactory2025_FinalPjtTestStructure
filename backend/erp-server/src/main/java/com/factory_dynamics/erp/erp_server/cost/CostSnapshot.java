package com.factory_dynamics.erp.erp_server.cost;

import com.factory_dynamics.erp.erp_server.common.AuditableEntity;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlan;
import com.factory_dynamics.erp.erp_server.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cost_snapshot",
        indexes = {
                @Index(name = "idx_cost_snapshot_plan", columnList = "plan_id"),
                @Index(name = "idx_cost_snapshot_product", columnList = "product_id"),
                @Index(name = "idx_cost_snapshot_calc_at", columnList = "calculated_at"),
                @Index(name = "idx_cost_snapshot_is_deleted", columnList = "is_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CostSnapshot extends AuditableEntity {

    @Id
    @Column(name = "snapshot_id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", foreignKey = @ForeignKey(name = "fk_cost_snapshot_plan"))
    private ProductionPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cost_snapshot_product"))
    private Product product;

    @Column(name = "qty", precision = 18, scale = 6, nullable = false)
    private BigDecimal qty;

    @Column(name = "total_material", precision = 18, scale = 6, nullable = false)
    private BigDecimal totalMaterial;

    @Column(name = "labor", precision = 18, scale = 6, nullable = false)
    private BigDecimal labor;

    @Column(name = "overhead", precision = 18, scale = 6, nullable = false)
    private BigDecimal overhead;

    @Column(name = "total_cost", precision = 18, scale = 6, nullable = false)
    private BigDecimal totalCost;

    @Column(name = "labor_rate", precision = 9, scale = 6, nullable = false)
    private BigDecimal laborRate;

    @Column(name = "overhead_rate", precision = 9, scale = 6, nullable = false)
    private BigDecimal overheadRate;

    @Column(name = "method", length = 20, nullable = false)
    private String method;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "note", length = 255)
    private String note;
}