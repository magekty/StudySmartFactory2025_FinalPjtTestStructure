package com.factory_dynamics.erp.erp_server.product;

import com.factory_dynamics.erp.erp_server.common.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_product",
        indexes = {
                @Index(name = "idx_tb_product_type", columnList = "type"),
                @Index(name = "idx_tb_product_is_deleted", columnList = "is_deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tb_product_code", columnNames = {"product_code"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product extends AuditableEntity {

    @Id
    @Column(name = "product_id", length = 36, nullable = false)
    private String id;

    @Column(name = "product_code", length = 50, nullable = false)
    private String productCode;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "type", length = 10, nullable = false)
    private String type;

    @Column(name = "unit", length = 20, nullable = false)
    private String unit;

    @Column(name = "description", length = 255)
    private String description;

    // 추가: 상태(활성/비활성) 매핑 - JPQL에서는 p.deleted로 사용
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;
}