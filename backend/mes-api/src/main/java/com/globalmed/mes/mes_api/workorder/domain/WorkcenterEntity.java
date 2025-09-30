package com.globalmed.mes.mes_api.workorder.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_workcenter",
        uniqueConstraints = @UniqueConstraint(name = "uk_workcenter_name", columnNames = {"workcenter_name"}))
@Getter
@Setter
public class WorkcenterEntity {

    @Id
    @Column(name = "workcenter_id", length = 36, nullable = false)
    private String workcenterId;

    @Column(name = "workcenter_name", length = 255, nullable = false)
    private String workcenterName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id", nullable = false)
    private WorkshopEntity workshop;

    @Column(name = "description", length = 255)
    private String description;

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
