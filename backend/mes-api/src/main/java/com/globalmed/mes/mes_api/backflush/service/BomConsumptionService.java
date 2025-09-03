// src/main/java/com/globalmed/mes/mes_api/backflush/service/BomConsumptionService.java
package com.globalmed.mes.mes_api.backflush.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import com.globalmed.mes.mes_api.workorder.repository.WorkOrderRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BomConsumptionService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper om;
    private final WorkOrderRepo woRepo;
    private final OutboxAppender outbox;

    @Transactional
    public void enqueueBackflushAndCost(String workOrderId, BigDecimal goodQty, String uom, OffsetDateTime asOfUtc, String perfKey) {
        if (goodQty == null || goodQty.signum() <= 0) return;

        WorkOrderEntity wo = woRepo.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("WO_NOT_FOUND"));
        String itemId = wo.getItemId();

        String bomId = jdbc.query("""
      SELECT h.bom_id FROM tb_bom_header h
      WHERE h.item_id=? AND h.is_deleted=0
        AND h.eff_from <= ? AND (h.eff_to IS NULL OR h.eff_to > ?)
      ORDER BY h.eff_from DESC
      LIMIT 1
    """, rs -> rs.next() ? rs.getString(1) : null, itemId, java.sql.Timestamp.from(asOfUtc.toInstant()), java.sql.Timestamp.from(asOfUtc.toInstant()));

        if (bomId == null) return;

        var lines = jdbc.query("""
      SELECT component_id, qty, uom, scrap_rate
      FROM tb_bom_line
      WHERE bom_id=? AND is_deleted=0
    """, (rs, i) -> {
            String compId = rs.getString("component_id");
            BigDecimal baseQty = rs.getBigDecimal("qty");
            BigDecimal scrap = rs.getBigDecimal("scrap_rate");
            String luom = rs.getString("uom");
            BigDecimal factor = BigDecimal.ONE.add(scrap == null ? BigDecimal.ZERO : scrap);
            BigDecimal need = baseQty.multiply(factor).multiply(goodQty).setScale(6, RoundingMode.HALF_UP);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("componentId", compId);
            m.put("qty", need);
            m.put("uom", luom);
            return m;
        }, bomId);

        Map<String, Object> bf = new LinkedHashMap<>();
        bf.put("workOrderId", workOrderId);
        bf.put("lines", lines);
        String bfJson;
        try { bfJson = om.writeValueAsString(bf); } catch(Exception e){ throw new RuntimeException(e); }

        String bfKey = "BF:" + perfKey;
        outbox.append("BACKFLUSH", bfJson, bfKey);

        Map<String, Object> cost = new LinkedHashMap<>();
        cost.put("workOrderId", workOrderId);
        cost.put("itemId", itemId);
        cost.put("totalQty", goodQty);
        cost.put("uom", uom == null || uom.isBlank() ? "EA" : uom);
        cost.put("unitCost", 0);
        cost.put("currency", "KRW");
        String costJson;
        try { costJson = om.writeValueAsString(cost); } catch(Exception e){ throw new RuntimeException(e); }

        String costKey = "COST:" + perfKey;
        outbox.append("COST_POST", costJson, costKey);
    }
}