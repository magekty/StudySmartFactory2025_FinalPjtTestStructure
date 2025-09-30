package com.globalmed.mes.mes_api.kpi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_kpi_data",
        uniqueConstraints = {
                // 실시간 KPI: aggregation_type -> aggregation_type_id
                @UniqueConstraint(name = "uk_kpi_realtime", columnNames = {"kpi_date", "work_order_id", "equipment_id", "process_id", "item_id", "aggregation_type_id"}),
                // 배치 KPI: aggregation_type -> aggregation_type_id, batch_check -> batch_group_key
                @UniqueConstraint(name = "uk_kpi_daily", columnNames = {"kpi_date",  "equipment_id", "process_id", "item_id", "batch_group_key", "aggregation_type_id"})
        })
@Getter
@Setter
public class KpiDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kpi_id")
    private Long kpiId;

    @Column(name = "kpi_date", nullable = false)
    private LocalDate kpiDate;

    @Column(name = "equipment_id", length = 36, nullable = false)
    private String equipmentId;

    @Column(name = "process_id", length = 36, nullable = false)
    private String processId;

    @Column(name = "item_id", length = 36, nullable = false)
    private String itemId;

    // 실시간 KPI용
    @Column(name = "work_order_id", length = 36)
    private String workOrderId;

    @Column(name = "actual_oee", precision = 5, scale = 2, nullable = false)
    private BigDecimal actualOee = BigDecimal.ZERO;

    @Column(name = "actual_productivity", precision = 10, scale = 4, nullable = false)
    private BigDecimal actualProductivity = BigDecimal.ZERO;

    @Column(name = "actual_yield", precision = 5, scale = 2, nullable = false)
    private BigDecimal actualYield = BigDecimal.ZERO;

    @Column(name = "actual_defect_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal actualDefectRate = BigDecimal.ZERO;

    @Column(name="is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_by", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;


    // 새로운 컬럼
    @Column(name = "aggregation_type_id", nullable = false)
    private Long aggregationTypeId;

    @Column(name = "batch_group_key", length = 20)
    private String batchGroupKey;

    @Column(name = "calc_status_code_id", nullable = false)
    private Long calcStatusCodeId;

    @Column(name = "calc_at", nullable = false)
    private LocalDateTime calcAt;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @PrePersist
    void prePersist() {
        if (createdBy == null || createdBy.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            createdBy = (auth != null && auth.isAuthenticated())
                    ? String.valueOf(auth.getPrincipal())
                    : "system";
        }
        if (startTime == null) startTime = LocalDateTime.now();
        if (endTime == null) endTime = LocalDateTime.now();
        if (calcAt == null) calcAt = LocalDateTime.now();
    }
}
