package com.factory_dynamics.erp.erp_server.bom.entity;

import com.factory_dynamics.erp.erp_server.common.AuditableEntity;
import com.factory_dynamics.erp.erp_server.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_bom_header",
        indexes = {
                @Index(name = "idx_bom_product_active", columnList = "product_id,is_active"),
                @Index(name = "idx_bom_effective", columnList = "effective_from,effective_to"),
                @Index(name = "idx_bom_is_deleted", columnList = "is_deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_bom_product_rev", columnNames = {"product_id","revision"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BomHeader extends AuditableEntity {

    @Id
    @Column(name = "bom_id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bom_header_product"))
    private Product product;

    @Column(name = "revision", length = 20, nullable = false)
    private String revision;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "note", length = 255)
    private String note;
}