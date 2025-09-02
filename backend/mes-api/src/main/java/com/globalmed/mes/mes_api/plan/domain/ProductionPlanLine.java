package com.globalmed.mes.mes_api.plan.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Setter
@Getter
@Table(name = "tb_production_plan_line",
        uniqueConstraints = @UniqueConstraint(name="uk_plan_line",
                columnNames = {"plan_id","plan_line_no"}))
public class ProductionPlanLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_line_id")
    private Long planLineId;

    @Column(name = "plan_id", nullable = false, length = 36)
    private String planId;

    @Column(name = "plan_line_no", nullable = false)
    private Integer planLineNo;

    @Column(name = "item_id", nullable = false, length = 36)
    private String itemId;

    @Column(name = "qty", nullable = false, precision = 18, scale = 6)
    private BigDecimal qty;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "due_date", nullable = false)
    private OffsetDateTime dueDate;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy = "sync";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    // getters/setters ...
    // (롬복 사용 시 @Getter @Setter 추가)
}