package com.globalmed.mes.mes_api.integration.erp.sync;

import com.globalmed.mes.mes_api.integration.erp.ErpIncrementalClient;
import com.globalmed.mes.mes_api.integration.erp.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncrementalSyncService {

    private final ErpIncrementalClient client;
    private final JdbcTemplate jdbc;

    public void syncItems() {
        OffsetDateTime cursor = getCursor("erp_items");
        List<ItemDto> list = client.items(cursor);
        OffsetDateTime maxTs = cursor;

        for (ItemDto it : list) {
            if (it.isDeleted()) {
                jdbc.update("UPDATE tb_item SET is_deleted=1, deleted_at=UTC_TIMESTAMP() WHERE item_id=?", it.itemId());
            } else {
                String itemCode = (it.itemCode()==null || it.itemCode().isBlank()) ? it.itemId() : it.itemCode();
                String itemType = (it.itemType()==null || it.itemType().isBlank()) ? "F" : it.itemType();
                String unit     = (it.unit()==null     || it.unit().isBlank())     ? "EA" : it.unit();

                jdbc.update("""
          INSERT INTO tb_item (item_id, item_code, item_name, unit, item_type, is_deleted, created_by, created_at)
          VALUES (?, ?, ?, ?, ?, 0, 'sync', UTC_TIMESTAMP())
          ON DUPLICATE KEY UPDATE
            item_code = VALUES(item_code),
            item_name = VALUES(item_name),
            unit      = VALUES(unit),
            item_type = VALUES(item_type),
            is_deleted= 0,
            deleted_at= NULL,
            modified_by='sync',
            modified_at=UTC_TIMESTAMP()
        """,
                        it.itemId(), itemCode, it.itemId(), unit, itemType
                );
            }
            if (it.updatedAt()!=null && it.updatedAt().isAfter(maxTs)) maxTs = it.updatedAt();
        }
        if (!list.isEmpty()) setCursor("erp_items", maxTs);
    }

    public void syncBoms() {
        OffsetDateTime cursor = getCursor("erp_boms");
        List<BomHeaderDto> headers = client.boms(cursor);
        OffsetDateTime maxTs = cursor;

        for (BomHeaderDto h : headers) {
            if (h.isDeleted()) {
                jdbc.update("UPDATE tb_bom_header SET is_deleted=1, deleted_at=UTC_TIMESTAMP() WHERE bom_id=?", h.bomId());
                jdbc.update("UPDATE tb_bom_line SET is_deleted=1, deleted_at=UTC_TIMESTAMP() WHERE bom_id=?", h.bomId());
            } else {
                jdbc.update("""
          INSERT INTO tb_bom_header (bom_id, item_id, revision, eff_from, eff_to, is_deleted, created_by, created_at)
          VALUES (?, ?, ?, ?, ?, 0, 'sync', UTC_TIMESTAMP())
          ON DUPLICATE KEY UPDATE
            item_id  = VALUES(item_id),
            revision = VALUES(revision),
            eff_from = VALUES(eff_from),
            eff_to   = VALUES(eff_to),
            is_deleted=0,
            deleted_at=NULL,
            modified_by='sync',
            modified_at=UTC_TIMESTAMP()
        """,
                        h.bomId(), h.itemId(), h.revision(), ts(h.effFrom()), ts(h.effTo())
                );

                jdbc.update("DELETE FROM tb_bom_line WHERE bom_id=?", h.bomId());
                for (BomLineDto l : h.lines()) {
                    String componentItemId = findItemIdByIdOrCode(l.componentId());
                    jdbc.update("""
            INSERT INTO tb_bom_line (bom_id, component_id, qty, uom, scrap_rate, is_deleted, created_by, created_at)
            VALUES (?, ?, ?, ?, ?, 0, 'sync', UTC_TIMESTAMP())
            ON DUPLICATE KEY UPDATE
              qty=VALUES(qty), uom=VALUES(uom), scrap_rate=VALUES(scrap_rate),
              is_deleted=0, deleted_at=NULL, modified_by='sync', modified_at=UTC_TIMESTAMP()
          """, h.bomId(), componentItemId, l.qty(), l.uom(), l.scrapRate()==null?0:l.scrapRate());
                }
            }
            if (h.updatedAt()!=null && h.updatedAt().isAfter(maxTs)) maxTs = h.updatedAt();
        }
        if (!headers.isEmpty()) setCursor("erp_boms", maxTs);
    }

    private String findItemIdByIdOrCode(String candidate) {
        try {
            return jdbc.queryForObject(
                    "SELECT item_id FROM tb_item WHERE item_id=? LIMIT 1",
                    String.class, candidate
            );
        } catch (EmptyResultDataAccessException e1) {
            try {
                return jdbc.queryForObject(
                        "SELECT item_id FROM tb_item WHERE item_code=? LIMIT 1",
                        String.class, candidate
                );
            } catch (EmptyResultDataAccessException e2) {
                throw new IllegalStateException("BOM_COMPONENT_NOT_FOUND:" + candidate);
            }
        }
    }

    private Timestamp ts(OffsetDateTime odt) { return odt==null?null:Timestamp.from(odt.toInstant()); }

    private OffsetDateTime getCursor(String key) {
        return jdbc.query("""
      SELECT last_synced_at FROM tb_sync_cursor WHERE cursor_key=?
    """, rs -> rs.next() ? rs.getTimestamp(1).toInstant().atOffset(ZoneOffset.UTC) : OffsetDateTime.parse("1970-01-01T00:00:00Z"), key);
    }

    private void setCursor(String key, OffsetDateTime odt) {
        jdbc.update("""
      INSERT INTO tb_sync_cursor (cursor_key, last_synced_at)
      VALUES (?, ?) ON DUPLICATE KEY UPDATE last_synced_at=VALUES(last_synced_at)
    """, key, Timestamp.from(odt.toInstant()));
    }
}