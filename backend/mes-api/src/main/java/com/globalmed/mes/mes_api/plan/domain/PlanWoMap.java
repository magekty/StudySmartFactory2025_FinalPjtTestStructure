// src/main/java/com/globalmed/mes/mes_api/plan/domain/PlanWoMap.java
package com.globalmed.mes.mes_api.plan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "tb_plan_wo_map",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_plan_wo_triplet", columnNames = {"plan_id","plan_line_no","work_order_id"})
        },
        indexes = {
                @Index(name = "idx_map_plan_line", columnList = "plan_id,plan_line_no"),
                @Index(name = "idx_map_wo", columnList = "work_order_id"),
                @Index(name = "idx_map_issued_at", columnList = "issued_at")
        })
public class PlanWoMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Long mapId;

    @Column(name = "plan_id", length = 36, nullable = false)
    private String planId;

    @Column(name = "plan_line_no", nullable = false)
    private Integer planLineNo;

    @Column(name = "work_order_id", length = 36, nullable = false)
    private String workOrderId;

    @Column(name = "issue_qty", precision = 18, scale = 6, nullable = false)
    private BigDecimal issueQty;

    @Column(name = "issue_uom", length = 20, nullable = false)
    private String issueUom = "EA";

    @Column(name = "issued_by", length = 50, nullable = false)
    private String issuedBy = "system";

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "status", length = 20)
    private String status = "ISSUED";
}