// src/main/java/com/globalmed/mes/mes_api/sync/SyncAuditLogger.java
package com.globalmed.mes.mes_api.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SyncAuditLogger {

    private final JdbcTemplate jdbc;

    public long start(String kind, Instant fromUtc, Instant toUtc) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO tb_sync_audit (source, kind, from_utc, to_utc, started_at) VALUES (?,?,?,?,UTC_TIMESTAMP())",
                    new String[]{"audit_id"}
            );
            ps.setString(1, "ERP");
            ps.setString(2, kind);
            ps.setTimestamp(3, fromUtc != null ? Timestamp.from(fromUtc) : null);
            ps.setTimestamp(4, toUtc   != null ? Timestamp.from(toUtc)   : null);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void success(long auditId, int fetched, int upserts, int deletes, String msg) {
        jdbc.update("""
      UPDATE tb_sync_audit
         SET fetched_cnt=?, upsert_cnt=?, delete_cnt=?, ok=1, message=?, finished_at=UTC_TIMESTAMP()
       WHERE audit_id=?
    """, fetched, upserts, deletes, truncate(msg, 250), auditId);
    }

    public void fail(long auditId, String errMsg) {
        jdbc.update("""
      UPDATE tb_sync_audit
         SET ok=0, message=?, finished_at=UTC_TIMESTAMP()
       WHERE audit_id=?
    """, truncate(errMsg, 250), auditId);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}