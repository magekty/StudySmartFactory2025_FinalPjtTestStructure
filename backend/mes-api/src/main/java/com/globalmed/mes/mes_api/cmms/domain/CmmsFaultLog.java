package com.globalmed.mes.mes_api.cmms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;


@Entity
@Table(name="tb_cmms_fault_log")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CmmsFaultLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="fault_id")
    private Long id;

    @Column(name="equipment_id", length=36, nullable = false)
    private String equipmentId;

    @Column(name="loss_cat_code_id", nullable = false)
    private Long lossCategoryCodeId;

    @Column(name="symptom", length=255, nullable=false)
    private String symptom;

    @Column(name="action", length=255)
    private String action;

    @Column(name="occurred_at", nullable=false)
    private OffsetDateTime occurredAt;

    @Column(name="resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name="cmms_wo_id")
    private Long cmmsWoId;  // ← 스칼라 FK (선택 연결)

    @Column(name="is_deleted", nullable = false)
    private boolean deleted; // 기본값  = false;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name="created_by", length=50, nullable = false)
    private String createdBy;

    @Column(name="created_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name="modified_by", length=50)
    private String modifiedBy;

    @Column(name="modified_at", insertable = false, updatable=false)
    private OffsetDateTime modifiedAt;
}
