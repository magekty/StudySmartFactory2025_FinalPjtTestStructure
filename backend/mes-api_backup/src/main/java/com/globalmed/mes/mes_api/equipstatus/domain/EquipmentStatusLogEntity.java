package com.globalmed.mes.mes_api.equipstatus.domain;


import com.globalmed.mes.mes_api.code.CodeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_equipment_status_log")
@Getter @Setter
public class EquipmentStatusLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "equipment_id", length = 36, nullable = false)
    private String equipmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_code_id", nullable = false)
    private CodeEntity statusCode; // tb_code(eqp_status: RUN/IDLE/DOWN)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_code_id")
    private CodeEntity reasonCode; // 선택

    @Column(name = "work_order_id", length = 36)
    private String workOrderId; // 선택

    @Column(name = "shift_id")
    private Long shiftId; // 선택

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // UTC

    @Column(name = "end_time")
    private LocalDateTime endTime;   // UTC

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @PrePersist
    void prePersist() {
        if (this.createdBy == null || this.createdBy.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            this.createdBy = (auth != null && auth.isAuthenticated())
                    ? String.valueOf(auth.getPrincipal())
                    : "system";
        }
    }
}