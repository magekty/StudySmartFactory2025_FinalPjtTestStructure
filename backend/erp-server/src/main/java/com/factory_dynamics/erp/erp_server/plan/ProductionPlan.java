package com.factory_dynamics.erp.erp_server.plan;

import com.factory_dynamics.erp.erp_server.common.AuditableEntity;
import com.factory_dynamics.erp.erp_server.plan.dto.ProductionPlanDetailDto;
import com.factory_dynamics.erp.erp_server.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_production_plan",
        indexes = {
                @Index(name = "idx_plan_product", columnList = "product_id"),
                @Index(name = "idx_plan_status", columnList = "status"),
                @Index(name = "idx_plan_dates", columnList = "start_date,end_date"),
                @Index(name = "idx_plan_is_deleted", columnList = "is_deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_plan_code", columnNames = {"plan_code"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductionPlan extends AuditableEntity {

    @Id
    @Column(name = "plan_id", length = 36, nullable = false)
    private String id;

    @Column(name = "plan_code", length = 50, nullable = false)
    private String planCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_plan_product"))
    private Product product;

    @Column(name = "qty", precision = 18, scale = 6, nullable = false)
    private BigDecimal qty;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "note", length = 255)
    private String note;

    public ProductionPlanDetailDto toDetailDto() {
        return new ProductionPlanDetailDto(
                this.id,
                this.planCode,
                this.product.getId(),
                this.product.getProductCode(), // Product 엔티티에 getCode()가 있다고 가정
                this.product.getName(), // Product 엔티티에 getName()이 있다고 가정
                this.startDate,
                this.endDate,
                this.qty,
                this.status,
                this.note,
                this.getVersion() // AuditableEntity에서 version 필드에 접근 가능하다고 가정
        );
    }
}