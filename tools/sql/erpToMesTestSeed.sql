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

SELECT COUNT(*) AS missing_fk
FROM tb_bom_line bl
LEFT JOIN tb_item i ON bl.component_id=i.item_id
WHERE bl.bom_id='BOM-TEST-01' AND i.item_id IS NULL;

-- erp_plans 커서 초기 등록(최초 1회)
INSERT INTO `tb_sync_cursor` (`cursor_key`, `last_synced_at`)
VALUES ('erp_plans', '1970-01-01 00:00:00')
ON DUPLICATE KEY UPDATE `last_synced_at` = VALUES(`last_synced_at`);

SELECT DISTINCT l.plan_id FROM tb_production_plan_line l LEFT JOIN tb_production_plan p ON p.plan_id = l.plan_id WHERE p.plan_id IS NULL;
SELECT plan_id FROM tb_production_plan WHERE plan_id = 'PL-001';

UPDATE tb_sync_cursor SET last_synced_at='2025-09-02 03:09:00' WHERE cursor_key='erp_plans';

SELECT cursor_key,last_synced_at FROM tb_sync_cursor WHERE cursor_key IN ('erp_items','erp_boms','erp_plans');

INSERT INTO mes_outbox (event_type, payload_json, idempotency_key, status, retry_count, next_retry_at, created_at) VALUES ('BACKFLUSH', '{"workOrderId":"WO-RECON-TEST"}', NULL, 'SHADOWED', 0, NULL, UTC_TIMESTAMP()), ('BACKFLUSH', '{"workOrderId":"WO-RECON-TEST"}', NULL, 'SENT', 0, NULL, UTC_TIMESTAMP());
INSERT INTO tb_production_log (event_id, source, event_type, event_timestamp, work_order_id, item_id, value_qty, uom) VALUES ('recon-g-1','MES','GoodQty', UTC_TIMESTAMP(),'WO-RECON-TEST','I-0001', 10.0,'EA'), ('recon-d-1','MES','DefectQty', UTC_TIMESTAMP(),'WO-RECON-TEST','I-0001', 2.0,'EA');