package com.globalmed.mes.mes_api.cmms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "tb_cmms_pm_plan")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CmmsPmPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private long id;

    @Column(name = "equipment_id", length=36, nullable=false)
    private String equipmentId;

    @Column(name = "task_name", length = 100, nullable = false)
    private String taskName;

    @Column(name = "cycle_type_code_id", nullable = false)
    private Long cycleTypeCodeId;

    @Column(name = "cycle_value", nullable = false)
    private Integer cycleValue;

    @Column(name = "last_done_at")
    private OffsetDateTime lastDoneAt;

    @Column(name = "next_due_at", nullable = false)
    private OffsetDateTime nextDueAt;

    @Column(name="status", length=20, nullable = false)
    private String status; // 기본값  = "ACTIVE";

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted; // 기본값  = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_by", length=50,nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @Column(name = "estimated_take_time")
    private Integer estimatedTakeTime;
}
