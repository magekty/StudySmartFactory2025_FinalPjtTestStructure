package com.globalmed.mes.mes_api.workorder.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_workshop",
        uniqueConstraints = @UniqueConstraint(name = "uk_workshop_name", columnNames = {"workshop_name"}))
@Getter
@Setter
public class WorkshopEntity {

    @Id
    @Column(name = "workshop_id", length = 36, nullable = false)
    private String workshopId;

    @Column(name = "workshop_name", length = 255, nullable = false)
    private String workshopName;

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