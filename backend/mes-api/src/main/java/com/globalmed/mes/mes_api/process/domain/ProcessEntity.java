package com.globalmed.mes.mes_api.process.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_process",
        uniqueConstraints = @UniqueConstraint(name = "uk_process_name", columnNames = {"process_name"}))
@Getter
@Setter
public class ProcessEntity {
    @Id
    @Column(name = "process_id", length = 36, nullable = false)
    private String processId;

    @Column(name = "process_name", length = 255, nullable = false)
    private String processName;

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
    @PrePersist
    void prePersist() {
        if (createdBy == null || createdBy.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            createdBy = (auth != null && auth.isAuthenticated())
                    ? String.valueOf(auth.getPrincipal())
                    : "system";
        }
    }

    @PreUpdate
    void preUpdate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        modifiedBy = (auth != null && auth.isAuthenticated())
                ? String.valueOf(auth.getPrincipal())
                : "system";
    }
}

