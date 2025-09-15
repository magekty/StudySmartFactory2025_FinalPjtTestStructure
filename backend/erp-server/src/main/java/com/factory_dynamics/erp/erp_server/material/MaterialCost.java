package com.factory_dynamics.erp.erp_server.material;

import com.factory_dynamics.erp.erp_server.common.AuditableEntity;
import com.factory_dynamics.erp.erp_server.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_material_cost",
        indexes = {
                @Index(name = "idx_cost_product_valid", columnList = "product_id,effective_to"),
                @Index(name = "idx_cost_is_deleted", columnList = "is_deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_cost_product_from", columnNames = {"product_id","effective_from"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaterialCost extends AuditableEntity {

    @Id
    @Column(name = "cost_id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cost_product"))
    private Product product;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "std_cost", precision = 18, scale = 6, nullable = false)
    private BigDecimal stdCost;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}