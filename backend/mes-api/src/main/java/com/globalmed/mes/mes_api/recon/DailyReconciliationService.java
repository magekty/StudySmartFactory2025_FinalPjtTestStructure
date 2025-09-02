// src/main/java/com/globalmed/mes/mes_api/recon/DailyReconciliationService.java
package com.globalmed.mes.mes_api.recon;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DailyReconciliationService {

    private final JdbcTemplate jdbc;

    public Map<String, Map<String, String>> runForKst(LocalDate kstDate) {
        // KST 하루 → UTC 윈도
        ZonedDateTime kstStart = kstDate.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ZonedDateTime kstEnd = kstStart.plusDays(1);
        Instant fromUtc = kstStart.toInstant();
        Instant toUtc = kstEnd.toInstant();

        // 지표 계산
        long woCount = jdbc.queryForObject("""
        SELECT COUNT(*) FROM tb_work_order
        WHERE created_at BETWEEN ? AND ?
      """, Long.class, Timestamp.from(fromUtc), Timestamp.from(toUtc));

        // 생산 실적: tb_production_log에서 이벤트 기반(없으면 0)
        Double goodQty = jdbc.queryForObject("""
        SELECT COALESCE(SUM(value_qty),0) FROM tb_production_log
        WHERE event_type='GoodQty' AND event_timestamp BETWEEN ? AND ?
      """, Double.class, Timestamp.from(fromUtc), Timestamp.from(toUtc));
        Double defectQty = jdbc.queryForObject("""
        SELECT COALESCE(SUM(value_qty),0) FROM tb_production_log
        WHERE event_type='DefectQty' AND event_timestamp BETWEEN ? AND ?
      """, Double.class, Timestamp.from(fromUtc), Timestamp.from(toUtc));

        // Backflush: mes_outbox 기준(Shadow/Sent 카운트)
        long bfShadowed = jdbc.queryForObject("""
        SELECT COUNT(*) FROM mes_outbox
        WHERE event_type='BACKFLUSH' AND status='SHADOWED'
          AND created_at BETWEEN ? AND ?
      """, Long.class, Timestamp.from(fromUtc), Timestamp.from(toUtc));
        long bfSent = jdbc.queryForObject("""
        SELECT COUNT(*) FROM mes_outbox
        WHERE event_type='BACKFLUSH' AND status='SENT'
          AND created_at BETWEEN ? AND ?
      """, Long.class, Timestamp.from(fromUtc), Timestamp.from(toUtc));

        // 결과 upsert
        LocalDate day = kstDate;
        upsert(day, "WO", "COUNT", woCount);
        upsert(day, "PERF", "GOOD_QTY", goodQty);
        upsert(day, "PERF", "DEFECT_QTY", defectQty);
        upsert(day, "BACKFLUSH", "SHADOWED", bfShadowed);
        upsert(day, "BACKFLUSH", "SENT", bfSent);

        Map<String, Map<String, String>> summary = new HashMap<>();
        summary.computeIfAbsent("WO", k -> new HashMap<>()).put("COUNT", String.valueOf(woCount));
        summary.computeIfAbsent("PERF", k -> new HashMap<>()).put("GOOD_QTY", String.valueOf(goodQty));
        summary.get("PERF").put("DEFECT_QTY", String.valueOf(defectQty));
        summary.computeIfAbsent("BACKFLUSH", k -> new HashMap<>()).put("SHADOWED", String.valueOf(bfShadowed));
        summary.get("BACKFLUSH").put("SENT", String.valueOf(bfSent));
        return summary;
    }

    private void upsert(LocalDate dateKst, String kind, String metric, Number val) {
        jdbc.update("""
      INSERT INTO tb_daily_reconciliation (date_kst, kind, metric, value)
      VALUES (?, ?, ?, ?)
      ON DUPLICATE KEY UPDATE value=VALUES(value), created_at=UTC_TIMESTAMP()
    """, dateKst, kind, metric, val);
    }
}