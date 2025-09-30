package com.globalmed.mes.mes_api.equipstatus.domain;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.process.domain.ProcessEntity;
import com.globalmed.mes.mes_api.workorder.domain.WorkcenterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_equipment",
        uniqueConstraints = @UniqueConstraint(name = "uk_equipment_name", columnNames = {"equipment_name"}))
@Getter
@Setter
public class EquipmentEntity {

    @Id
    @Column(name = "equipment_id", length = 36, nullable = false)
    private String equipmentId;

    @Column(name = "equipment_name", length = 255, nullable = false)
    private String equipmentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workcenter_id", nullable = false)
    private WorkcenterEntity workcenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessEntity process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code_id", nullable = false)
    private CodeEntity statusCode;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    @Column(name = "modified_at", insertable = false, updatable = false)
    private LocalDateTime modifiedAt;

}