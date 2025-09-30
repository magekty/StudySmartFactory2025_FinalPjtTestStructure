package com.globalmed.mes.mes_api.cmms.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "tb_cmms_work_order",
    uniqueConstraints =@UniqueConstraint(name="uq_cmms_wo_request_id", columnNames = "request_id")
)
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CmmsWorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cmms_wo_id")
    private Long id;

    @Column(name = "equipment_id", length=36,nullable = false)
    private String equipmentId;

    @Column(name = "title", length=200, nullable=false)
    private String title;

    @Column(name="priority_code_id", nullable=false)
    private Long priorityCodeId;

    @Column(name = "status_code_id", nullable = false)
    private Long statusCodeId;

    @Column(name = "assignee_user_id", length=36)
    private String assigneeUserId;

    @Column(name="request_id", length=64)
    private String requestId;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "actual_minutes")
    private Integer actualMinutes;

    @Column(name = "parts_cost", precision = 12, scale = 2)
    private BigDecimal partsCost;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted; // 기본값 = false;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_by", length=50, nullable = false)
    private String createdBy;

    @Column(name = "modified_by", length=50)
    private String modifiedBy;

    @Column(name = "modified_at", insertable=false, updatable=false)
    private OffsetDateTime modifiedAt;

}
