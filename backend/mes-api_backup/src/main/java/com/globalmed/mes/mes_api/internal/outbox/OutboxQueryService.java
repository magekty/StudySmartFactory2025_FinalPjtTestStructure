// src/main/java/com/globalmed/mes/mes_api/internal/outbox/OutboxQueryService.java
package com.globalmed.mes.mes_api.internal.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxQueryService {

    private final JdbcTemplate jdbc;

    public record Summary(String status, long count) {}

    public List<Summary> summary() {
        return jdbc.query("""
      SELECT status, COUNT(*) AS cnt
      FROM mes_outbox
      GROUP BY status
    """, (rs, i) -> new Summary(rs.getString("status"), rs.getLong("cnt")));
    }

    public record Row(
            long outboxId,
            String eventType,
            String status,
            String idempotencyKey,
            String workOrderId,
            String lastError,
            Instant createdAt
    ) {}

    public List<Row> list(String status, String eventType, Integer sinceMinutes, Integer limit, String searchWo) {
        StringBuilder sql = new StringBuilder("""
      SELECT outbox_id, event_type, status, idempotency_key,
             JSON_UNQUOTE(JSON_EXTRACT(payload_json,'$.workOrderId')) AS work_order_id,
             last_error, created_at
      FROM mes_outbox
      WHERE 1=1
    """);
        List<Object> args = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            args.add(status);
        }
        if (eventType != null && !eventType.isBlank()) {
            sql.append(" AND event_type = ?");
            args.add(eventType);
        }

        int minutes = (sinceMinutes == null || sinceMinutes <= 0) ? 1440 : sinceMinutes;
        sql.append(" AND created_at >= ?");
        args.add(Timestamp.from(Instant.now().minusSeconds(minutes * 60L)));

        if (searchWo != null && !searchWo.isBlank()) {
            sql.append(" AND JSON_UNQUOTE(JSON_EXTRACT(payload_json,'$.workOrderId')) = ?");
            args.add(searchWo.trim());
        }

        int lim = (limit == null || limit <= 0 || limit > 500) ? 100 : limit;
        sql.append(" ORDER BY outbox_id DESC LIMIT ").append(lim);

        // 최신 방식: varargs 오버로드 사용 (deprecated 시그니처 회피)
        return jdbc.query(
                sql.toString(),
                (rs, i) -> new Row(
                        rs.getLong("outbox_id"),
                        rs.getString("event_type"),
                        rs.getString("status"),
                        rs.getString("idempotency_key"),
                        rs.getString("work_order_id"),
                        rs.getString("last_error"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                args.toArray()
        );
    }
}