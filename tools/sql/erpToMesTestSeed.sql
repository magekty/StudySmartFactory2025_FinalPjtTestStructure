INSERT INTO mes_outbox (event_type, payload_json, idempotency_key, status, retry_count, next_retry_at, created_at) VALUES ('WO_CREATE', '{"workOrderId":"WO-123","workOrderNumber":"WO-2025-0001","itemId":"I-0001","qty":100,"status":"P"}', NULL, 'PENDING', 0, NULL, NOW());
INSERT INTO mes_outbox (event_type, payload_json, idempotency_key, status, retry_count, next_retry_at, created_at) VALUES ('WO_STATUS', '{"workOrderId":"WO-123","status":"R","changedAt":"2025-08-29T09:00:00Z"}', NULL, 'PENDING', 0, NULL, NOW());
INSERT INTO mes_outbox (event_type, payload_json, idempotency_key, status, retry_count, next_retry_at, created_at) VALUES ('PERF_CREATE', '{"workOrderId":"WO-123","itemId":"I-0001","processId":"P-0001","equipmentId":"E-0001","goodQty":98,"defectQty":2,"startTime":"2025-08-29T09:00:00Z","endTime":"2025-08-29T09:30:00Z"}', NULL, 'PENDING', 0, NULL, NOW());
INSERT INTO mes_outbox (event_type, payload_json, idempotency_key, status, retry_count, next_retry_at, created_at) VALUES ('BACKFLUSH', '{"workOrderId":"WO-123","lines":[{"componentId":"RM-001","qty":98.0,"uom":"EA"},{"componentId":"RM-002","qty":49.0,"uom":"EA"}]}', NULL, 'PENDING', 0, NULL, NOW());
 SELECT outbox_id, event_type, status, retry_count, next_retry_at, last_error FROM mes_outbox ORDER BY outbox_id DESC;
 SELECT outbox_id,event_type,status,retry_count,last_error FROM mes_outbox ORDER BY outbox_id DESC LIMIT 10;
 -- 초기값
INSERT INTO tb_sync_cursor (cursor_key, last_synced_at)
VALUES ('erp_items', '1970-01-01 00:00:00'), ('erp_boms', '1970-01-01 00:00:00')
ON DUPLICATE KEY UPDATE last_synced_at=VALUES(last_synced_at);

ALTER TABLE tb_item
  ADD COLUMN uom VARCHAR(20) NOT NULL DEFAULT 'EA' AFTER item_name;
  
UPDATE tb_item SET uom = unit WHERE uom <> unit OR uom IS NULL;

INSERT INTO mes_outbox (event_type, payload_json, idempotency_key, status, retry_count, next_retry_at, created_at) VALUES ('WO_CREATE', '{"workOrderId":"RETRY-TEST","workOrderNumber":"WO-RETRY","itemId":"I-0001","qty":100,"status":"P"}', NULL, 'PENDING', 0, NULL, UTC_TIMESTAMP());
SELECT outbox_id,event_type,status,retry_count,next_retry_at,last_error FROM mes_outbox ORDER BY outbox_id DESC LIMIT 5;
UPDATE mes_outbox SET status='PENDING', retry_count=0, next_retry_at=NULL, last_error=NULL WHERE outbox_id = 9;
SELECT outbox_id,event_type,status,retry_count,next_retry_at,last_error FROM mes_outbox WHERE outbox_id = 9;
SELECT * FROM tb_item;
SELECT * FROM tb_sync_cursor WHERE cursor_key='erp_items';

SELECT bom_id, item_id, revision, eff_from, eff_to, is_deleted
FROM tb_bom_header
WHERE bom_id='BOM-TEST-01';