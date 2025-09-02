// src/main/java/com/globalmed/mes/mes_api/plan/domain/ProductionPlan.java
package com.globalmed.mes.mes_api.plan.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_production_plan")
public class ProductionPlan {

    @Id
    @Column(name = "plan_id", length = 36, nullable = false)
    private String planId;

    @Column(name = "plan_number", length = 50, nullable = false)
    private String planNumber;

    @Column(name = "item_id", length = 36, nullable = false)
    private String itemId;

    @Column(name = "target_qty", precision = 10, scale = 4, nullable = false)
    private BigDecimal targetQty;

    @Column(name = "start_date", nullable = false)
    private java.sql.Date startDate;

    @Column(name = "end_date", nullable = false)
    private java.sql.Date endDate;

    @Column(name = "status", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String status; // 'P','R','C'

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    public static ProductionPlan seed(String planId, String itemId, java.sql.Date dayUtc) {
        ProductionPlan p = new ProductionPlan();
        p.planId = planId;
        p.planNumber = planId;
        p.itemId = itemId;
        p.targetQty = new BigDecimal("0.0000");
        p.startDate = dayUtc;
        p.endDate = dayUtc;
        p.status = "P";
        p.isDeleted = false;
        p.createdBy = "sync";
        p.createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC);
        return p;
    }
}