package com.globalmed.mes.mes_api.cmms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "tb_cmms_work_order_log")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CmmsWorkOrderLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "cmms_wo_id", nullable = false)
    private Long cmmsWoId;

    @Column(name ="from_status_code_id")
    private Long fromStatusCodeId;

    @Column(name ="to_status_code_id", nullable = false)
    private Long toStatusCodeId;

    @Column(name ="changed_by", length=36, nullable = false)
    private String changedByUserId;

    @Column(name = "changed_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime changedAt;

    @Column(name = "note", length = 255)
    private String note;
}
