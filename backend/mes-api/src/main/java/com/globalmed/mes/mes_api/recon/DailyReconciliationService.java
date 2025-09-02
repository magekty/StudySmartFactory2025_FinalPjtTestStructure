// src/main/java/com/globalmed/mes/mes_api/recon/DailyReconciliationService.java
// PERF 지표 로그 테이블 기반(이미 적용), 안전 가드(테이블 없을 때 0 처리) 추가
package com.globalmed.mes.mes_api.recon;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
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
        ZonedDateTime kstStart = kstDate.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ZonedDateTime kstEnd = kstStart.plusDays(1);
        var fromUtc = Timestamp.from(kstStart.toInstant());
        var toUtc = Timestamp.from(kstEnd.toInstant());

        long woCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tb_work_order WHERE created_at BETWEEN ? AND ?",
                Long.class, fromUtc, toUtc);

        double goodQty = safeSum("GoodQty", fromUtc, toUtc);
        double defectQty = safeSum("DefectQty", fromUtc, toUtc);

        long bfShadowed = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mes_outbox WHERE event_type='BACKFLUSH' AND status='SHADOWED' AND created_at BETWEEN ? AND ?",
                Long.class, fromUtc, toUtc);
        long bfSent = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mes_outbox WHERE event_type='BACKFLUSH' AND status='SENT' AND created_at BETWEEN ? AND ?",
                Long.class, fromUtc, toUtc);

        upsert(kstDate, "WO", "COUNT", woCount);
        upsert(kstDate, "PERF", "GOOD_QTY", goodQty);
        upsert(kstDate, "PERF", "DEFECT_QTY", defectQty);
        upsert(kstDate, "BACKFLUSH", "SHADOWED", bfShadowed);
        upsert(kstDate, "BACKFLUSH", "SENT", bfSent);

        Map<String, Map<String, String>> res = new HashMap<>();
        res.computeIfAbsent("WO", k -> new HashMap<>()).put("COUNT", String.valueOf(woCount));
        res.computeIfAbsent("PERF", k -> new HashMap<>()).put("GOOD_QTY", String.valueOf(goodQty));
        res.get("PERF").put("DEFECT_QTY", String.valueOf(defectQty));
        res.computeIfAbsent("BACKFLUSH", k -> new HashMap<>()).put("SHADOWED", String.valueOf(bfShadowed));
        res.get("BACKFLUSH").put("SENT", String.valueOf(bfSent));
        return res;
    }

    private double safeSum(String type, Timestamp fromUtc, Timestamp toUtc) {
        try {
            Double v = jdbc.queryForObject("""
        SELECT COALESCE(SUM(value_qty),0) FROM tb_production_log
        WHERE event_type=? AND event_timestamp BETWEEN ? AND ?
      """, Double.class, type, fromUtc, toUtc);
            return v == null ? 0d : v;
        } catch (DataAccessException e) {
            return 0d;
        }
    }

    private void upsert(LocalDate day, String kind, String metric, Number val) {
        jdbc.update("""
      INSERT INTO tb_daily_reconciliation (date_kst, kind, metric, value)
      VALUES (?, ?, ?, ?)
      ON DUPLICATE KEY UPDATE value=VALUES(value), created_at=UTC_TIMESTAMP()
    """, day, kind, metric, val);
    }
}