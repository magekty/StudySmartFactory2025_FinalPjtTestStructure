package com.globalmed.mes.mes_api.integration.erp.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity @Table(name = "mes_outbox")
@Getter
@Setter
public class OutboxEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxId;
    private String eventType;              // WO_CREATE / WO_STATUS / PERF_CREATE / BACKFLUSH / COST_POST
    @Lob private String payloadJson;       // 그대로 전송
    private String idempotencyKey;         // 없으면 워커가 생성
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    private int retryCount;
    private OffsetDateTime nextRetryAt;
    private OffsetDateTime createdAt;
    private String lastError;

}