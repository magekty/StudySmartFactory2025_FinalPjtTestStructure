package com.globalmed.mes.mes_api.employee.shift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_shift_calendar")
@Getter
@Setter
public class ShiftCalendarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Long calendarId;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private ShiftEntity shift;

    @Column(name = "equipment_id", length = 36)
    private String equipmentId;

    @Column(name = "workcenter_id", length = 36)
    private String workcenterId;

    @Column(name = "start_ts", columnDefinition = "TIMESTAMP", nullable = false)
    private OffsetDateTime startTs;

    @Column(name = "end_ts", columnDefinition = "TIMESTAMP", nullable = false)
    private OffsetDateTime endTs;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;
    @PrePersist
    void prePersist() {
        if (createdBy == null || createdBy.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            createdBy = (auth != null && auth.isAuthenticated())
                    ? auth.getName()
                    : "system";
        }
        if (startTs == null) throw new IllegalStateException("startTs must be set before persist");
        if (endTs == null) throw new IllegalStateException("endTs must be set before persist");
    }

    @PreUpdate
    void preUpdate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        modifiedBy = (auth != null && auth.isAuthenticated())
                ? auth.getName()
                : "system";
    }
}
