// src/main/java/com/globalmed/mes/mes_api/log/ProdLogService.java
package com.globalmed.mes.mes_api.log;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProdLogService {

    private final JdbcTemplate jdbc;

    @Value("${prodlog.enabled:true}")
    private boolean enabled;

    private void insert(String eventId, String source, String type, OffsetDateTime tsUtc,
                        String woId, String planId, Integer planLineNo, String itemId,
                        String eqpId, String processId, Double qty, String uom,
                        String statusCode, String valueText) {
        if (!enabled) return;
        jdbc.update("""
      INSERT IGNORE INTO tb_production_log
        (event_id, source, event_type, event_timestamp, work_order_id, plan_id, plan_line_no,
         item_id, equipment_id, process_id, value_qty, uom, status_code, value_text)
      VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
      """,
                eventId, source, type,
                Timestamp.from(tsUtc.toInstant()),
                woId, planId, planLineNo, itemId, eqpId, processId, qty, uom, statusCode, valueText
        );
    }

    public void goodQty(String woId, String itemId, String processId, String eqpId,
                        Double qty, String uom, OffsetDateTime endUtc, String eventKey) {
        String id = eventKey != null ? eventKey : "PERF-GOOD:" + UUID.randomUUID();
        insert(id, "MES", "GoodQty", toUtc(endUtc), woId, null, null, itemId, eqpId, processId, qty, uom, null, null);
    }

    public void defectQty(String woId, String itemId, String processId, String eqpId,
                          Double qty, String uom, OffsetDateTime endUtc, String eventKey) {
        String id = eventKey != null ? eventKey : "PERF-DEF:" + UUID.randomUUID();
        insert(id, "MES", "DefectQty", toUtc(endUtc), woId, null, null, itemId, eqpId, processId, qty, uom, null, null);
    }

    public void workOrderStatus(String woId, String status, OffsetDateTime changedUtc, String eventKey) {
        String id = eventKey != null ? eventKey : "WO-STS:" + woId + ":" + status + ":" + nowKey();
        insert(id, "MES", "WorkOrderStatus", toUtc(changedUtc), woId, null, null, null, null, null, null, null, status, null);
    }

    public void equipmentStatus(String eqpId, String status, OffsetDateTime atUtc, String eventKey) {
        String id = eventKey != null ? eventKey : "EQP-STS:" + eqpId + ":" + status + ":" + nowKey();
        insert(id, "MES", "EquipmentStatus", toUtc(atUtc), null, null, null, null, eqpId, null, null, null, status, null);
    }

    public void backflush(String woId, String itemId, Double totalQty, String uom,
                          String outcome, OffsetDateTime atUtc, String eventKey, String meta) {
        String id = eventKey != null ? eventKey : "BF:" + woId + ":" + outcome + ":" + nowKey();
        insert(id, "MES", "Backflush", toUtc(atUtc), woId, null, null, itemId, null, null, totalQty, uom, outcome, meta);
    }

    private static OffsetDateTime toUtc(OffsetDateTime odt) {
        return odt != null ? odt.withOffsetSameInstant(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC);
    }
    private static String nowKey() { return String.valueOf(System.currentTimeMillis()); }
}