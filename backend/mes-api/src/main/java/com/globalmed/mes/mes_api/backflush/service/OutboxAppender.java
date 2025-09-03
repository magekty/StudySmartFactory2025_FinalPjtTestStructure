// src/main/java/com/globalmed/mes/mes_api/backflush/service/OutboxAppender.java
package com.globalmed.mes.mes_api.backflush.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxAppender {

    private final JdbcTemplate jdbc;

    public void append(String eventType, String payloadJson, String idempotencyKey) {
        jdbc.update("""
      INSERT INTO mes_outbox (event_type, payload_json, idempotency_key, status, retry_count, next_retry_at, created_at)
      VALUES (?, ?, ?, 'PENDING', 0, NULL, UTC_TIMESTAMP())
      ON DUPLICATE KEY UPDATE payload_json=VALUES(payload_json), status='PENDING', retry_count=0, next_retry_at=NULL
    """, eventType, payloadJson, idempotencyKey);
    }
}