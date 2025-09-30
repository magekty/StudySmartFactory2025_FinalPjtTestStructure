package com.globalmed.mes.mes_api.workorder.domain;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.process.domain.ProcessEntity;
import com.globalmed.mes.mes_api.item.ItemEntity;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_work_order",
        uniqueConstraints = @UniqueConstraint(name = "uk_wo_number", columnNames = {"work_order_number"}))
@Getter @Setter
public class WorkOrderEntity {

    @Id
    @Column(name = "work_order_id", length = 36, nullable = false)
    private String workOrderId;

    @Column(name = "plan_id", length = 36)
    private String planId;

    @Column(name = "work_order_number", length = 50, nullable = false)
    private String workOrderNumber;

    // 기존의 itemId 컬럼을 제거하고, ItemEntity와의 관계로 대체
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity itemId;

    // 기존의 processId 컬럼을 제거하고, ProcessEntity와의 관계로 대체
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessEntity processId;

    // 기존의 equipmentId 컬럼을 제거하고, EquipmentEntity와의 관계로 대체
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipmentId;

    @Column(name = "order_qty", nullable = false, precision = 10, scale = 4)
    private BigDecimal orderQty;

    @Column(name = "produced_qty", nullable = false, precision = 10, scale = 4)
    private BigDecimal producedQty = BigDecimal.ZERO;

    @Column(name = "start_ts")
    private LocalDateTime startTs;

    @Column(name = "end_ts")
    private LocalDateTime endTs;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_code_id", nullable = false)
    private CodeEntity statusCode; // wo_status: P/R/C

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    // DB에서 DEFAULT CURRENT_TIMESTAMP / ON UPDATE 사용 → 읽기 전용 매핑
    @Column(name = "created_at", columnDefinition = "datetime", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", columnDefinition = "datetime", insertable = false, updatable = false)
    private LocalDateTime modifiedAt;

    @PrePersist
    void prePersist() {
        if (producedQty == null) producedQty = BigDecimal.ZERO;
        if (createdBy == null || createdBy.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            createdBy = (auth != null && auth.isAuthenticated())
                    ? String.valueOf(auth.getPrincipal())
                    : "system";
        }
    }
}