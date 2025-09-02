// src/main/java/com/globalmed/mes/mes_api/workorder/domain/WorkOrderEntity.java
package com.globalmed.mes.mes_api.workorder.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

    @Column(name = "item_id", length = 36, nullable = false)
    private String itemId;

    @Column(name = "process_id", length = 36, nullable = false)
    private String processId;

    @Column(name = "equipment_id", length = 36, nullable = false)
    private String equipmentId;

    @Column(name = "order_qty", nullable = false, precision = 10, scale = 4)
    private BigDecimal orderQty;

    @Column(name = "produced_qty", nullable = false, precision = 10, scale = 4)
    private BigDecimal producedQty = BigDecimal.ZERO;

    @Column(name = "start_ts")
    private LocalDateTime startTs;

    @Column(name = "end_ts")
    private LocalDateTime endTs;

    // FK(CodeEntity) 제거 → 코드값 직접 보관(P/R/C)
    @Column(name = "status_code", length = 1, nullable = false)
    private String statusCode;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    // DB DEFAULT/ON UPDATE 사용 → 읽기 전용
    @Column(name = "created_at", columnDefinition = "datetime", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", columnDefinition = "datetime", insertable = false, updatable = false)
    private LocalDateTime modifiedAt;

    @PrePersist
    void prePersist() {
        if (producedQty == null) producedQty = BigDecimal.ZERO;
        if (statusCode == null || statusCode.isBlank()) statusCode = "P";
        else statusCode = statusCode.trim().toUpperCase();

        if (createdBy == null || createdBy.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            createdBy = (auth != null && auth.isAuthenticated())
                    ? String.valueOf(auth.getPrincipal())
                    : "system";
        }
    }
}