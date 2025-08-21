package com.globalmed.mes.mes_api.performance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_production_performance")
@Getter @Setter
public class ProductionPerformanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id")
    private Long performanceId;

    @Column(name = "work_order_id", length = 36, nullable = false)
    private String workOrderId;

    @Column(name = "item_id", length = 36, nullable = false)
    private String itemId;

    @Column(name = "process_id", length = 36, nullable = false)
    private String processId;

    @Column(name = "equipment_id", length = 36, nullable = false)
    private String equipmentId;

    @Column(name = "produced_qty", nullable = false, precision = 10, scale = 4)
    private BigDecimal producedQty;

    @Column(name = "defect_qty", nullable = false, precision = 10, scale = 4)
    private BigDecimal defectQty = BigDecimal.ZERO;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // UTC

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;   // UTC

    @Column(name = "worker_id", length = 50)
    private String workerId; // 선택

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "request_id", length = 64, unique = true)
    private String requestId;

    @PrePersist
    void prePersist() {
        if (defectQty == null) defectQty = BigDecimal.ZERO;
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (createdBy == null || createdBy.isBlank()) {
            createdBy = (a != null && a.isAuthenticated()) ? String.valueOf(a.getPrincipal()) : "system";
        }
    }
}