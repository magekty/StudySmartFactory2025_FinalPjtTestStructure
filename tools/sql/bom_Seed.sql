-- =========================================
-- MES DEV SEED (MySQL)
-- - 스키마명: globalmed (필요 시 교체)
-- - FK 안전, 증분/삭제/HOP 테스트용
-- =========================================

-- 0) 스키마 선택
USE `globalmed`;

-- 1) 옵션: 테이블 정리(DEV에서만!)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE tb_bom_line;
TRUNCATE TABLE tb_bom_header;
TRUNCATE TABLE tb_item;
SET FOREIGN_KEY_CHECKS = 1;

-- 2) 시간 변수 (UTC)
SET @now_utc = UTC_TIMESTAMP(6);
SET @base_utc = DATE_SUB(@now_utc, INTERVAL 30 MINUTE);

-- 3) 아이템(부모) SEED
-- 완제품(F): ITEM-1001..ITEM-1010
-- 원자재(R): RM-0001..RM-0020
INSERT INTO tb_item
(item_id, item_code, item_name, item_type, unit, description, is_deleted, created_by, created_at, modified_by)
VALUES
('ITEM-1001','ITEM-1001','완제품1','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1002','ITEM-1002','완제품2','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1003','ITEM-1003','완제품3','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1004','ITEM-1004','완제품4','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1005','ITEM-1005','완제품5','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1006','ITEM-1006','완제품6','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1007','ITEM-1007','완제품7','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1008','ITEM-1008','완제품8','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1009','ITEM-1009','완제품9','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-1010','ITEM-1010','완제품10','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0001','RM-0001','원자재1','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0002','RM-0002','원자재2','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0003','RM-0003','원자재3','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0004','RM-0004','원자재4','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0005','RM-0005','원자재5','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0006','RM-0006','원자재6','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0007','RM-0007','원자재7','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0008','RM-0008','원자재8','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0009','RM-0009','원자재9','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0010','RM-0010','원자재10','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0011','RM-0011','원자재11','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0012','RM-0012','원자재12','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0013','RM-0013','원자재13','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0014','RM-0014','원자재14','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0015','RM-0015','원자재15','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0016','RM-0016','원자재16','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0017','RM-0017','원자재17','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0018','RM-0018','원자재18','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0019','RM-0019','원자재19','R','EA', NULL, 0, 'seed', @base_utc, NULL),
('RM-0020','RM-0020','원자재20','R','EA', NULL, 0, 'seed', @base_utc, NULL)
ON DUPLICATE KEY UPDATE item_name=VALUES(item_name), item_type=VALUES(item_type), unit=VALUES(unit),
description=VALUES(description), is_deleted=VALUES(is_deleted), modified_by=VALUES(modified_by);

-- 4) BOM 헤더 SEED
INSERT INTO tb_bom_header
(bom_id, item_id, revision, alt_code, eff_from, eff_to, description, is_deleted, created_by, created_at, modified_by)
VALUES
(CONCAT('BOM-','ITEM-1001','|','A','|','STD'),'ITEM-1001','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1002','|','A','|','STD'),'ITEM-1002','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1003','|','A','|','STD'),'ITEM-1003','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1004','|','A','|','STD'),'ITEM-1004','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1005','|','A','|','STD'),'ITEM-1005','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1006','|','A','|','STD'),'ITEM-1006','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1007','|','A','|','STD'),'ITEM-1007','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1008','|','A','|','STD'),'ITEM-1008','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1009','|','A','|','STD'),'ITEM-1009','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL),
(CONCAT('BOM-','ITEM-1010','|','A','|','STD'),'ITEM-1010','A','STD',@base_utc,NULL,NULL,0,'seed',@base_utc,NULL)
ON DUPLICATE KEY UPDATE eff_from=VALUES(eff_from), is_deleted=VALUES(is_deleted), 
modified_by=VALUES(modified_by);

-- 5) BOM 라인 SEED
INSERT INTO tb_bom_line
(bom_id, line_no, component_id, qty, uom, scrap_rate, is_deleted, created_by, created_at, modified_by)
VALUES
(CONCAT('BOM-','ITEM-1001','|A|STD'), 1, 'RM-0001', 1.0, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1001','|A|STD'), 2, 'RM-0002', 0.5, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1001','|A|STD'), 3, 'RM-0003', 2.0, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1002','|A|STD'), 1, 'RM-0004', 1.0, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1002','|A|STD'), 2, 'RM-0005', 0.7, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1002','|A|STD'), 3, 'RM-0006', 1.5, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1003','|A|STD'), 1, 'RM-0007', 0.9, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1003','|A|STD'), 2, 'RM-0008', 0.6, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1003','|A|STD'), 3, 'RM-0009', 1.8, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1004','|A|STD'), 1, 'RM-0010', 1.1, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1004','|A|STD'), 2, 'RM-0011', 0.5, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1004','|A|STD'), 3, 'RM-0012', 1.6, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1005','|A|STD'), 1, 'RM-0013', 1.2, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1005','|A|STD'), 2, 'RM-0014', 0.8, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1005','|A|STD'), 3, 'RM-0015', 1.9, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1006','|A|STD'), 1, 'RM-0016', 1.0, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1006','|A|STD'), 2, 'RM-0017', 0.5, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1006','|A|STD'), 3, 'RM-0018', 1.5, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1007','|A|STD'), 1, 'RM-0019', 0.9, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1007','|A|STD'), 2, 'RM-0020', 0.6, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1007','|A|STD'), 3, 'RM-0001', 1.7, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1008','|A|STD'), 1, 'RM-0002', 1.1, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1008','|A|STD'), 2, 'RM-0003', 0.5, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1008','|A|STD'), 3, 'RM-0004', 1.4, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1009','|A|STD'), 1, 'RM-0005', 1.3, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1009','|A|STD'), 2, 'RM-0006', 0.7, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1009','|A|STD'), 3, 'RM-0007', 1.8, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1010','|A|STD'), 1, 'RM-0008', 1.0, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1010','|A|STD'), 2, 'RM-0009', 0.6, 'EA', 0.0, 0, 'seed', @base_utc, NULL),
(CONCAT('BOM-','ITEM-1010','|A|STD'), 3, 'RM-0010', 1.6, 'EA', 0.0, 0, 'seed', @base_utc, NULL)
ON DUPLICATE KEY UPDATE qty=VALUES(qty), uom=VALUES(uom), scrap_rate=VALUES(scrap_rate), 
is_deleted=VALUES(is_deleted), modified_by=VALUES(modified_by);

-- 6) tombstone 1건(삭제 시나리오 검증)
UPDATE tb_bom_header
SET is_deleted = 1, modified_by = 'seed', modified_at = @now_utc, deleted_at = @now_utc
WHERE bom_id = CONCAT('BOM-','ITEM-1002','|','A','|','STD');

-- 7) 커서 초기화(증분 재현 용도) — 필요 시 실행
-- UPDATE tb_sync_cursor SET last_synced_at='1970-01-01 00:00:00' WHERE cursor_key IN ('erp_items','erp_boms');

-- 8) 무결성 빠른 자가 점검(선택)
-- SELECT item_id, revision, alt_code, COUNT(*) c FROM tb_bom_header GROUP BY 1,2,3 HAVING c>1;
-- SELECT bom_id, line_no, COUNT(*) c FROM tb_bom_line GROUP BY 1,2 HAVING c>1;
-- SELECT COUNT(*) orphan_lines FROM tb_bom_line l LEFT JOIN tb_bom_header h ON h.bom_id=l.bom_id WHERE h.bom_id IS NULL;
-- 시간
SET @now_utc  = UTC_TIMESTAMP(6);
SET @base_utc = DATE_SUB(@now_utc, INTERVAL 30 MINUTE);

-- FG: ITEM-2001..ITEM-2050 (필요 수량만큼 조절)
-- =========================================
-- 추가 아이템(품목) SEED
-- =========================================

-- 3) 추가 아이템 SEED
INSERT INTO tb_item
(item_id, item_code, item_name, item_type, unit, description, is_deleted, created_by, created_at, modified_by)
VALUES
('ITEM-2001','ITEM-2001','추가제품1','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2002','ITEM-2002','추가제품2','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2003','ITEM-2003','추가제품3','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2004','ITEM-2004','추가제품4','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2005','ITEM-2005','추가제품5','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2006','ITEM-2006','추가제품6','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2007','ITEM-2007','추가제품7','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2008','ITEM-2008','추가제품8','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2009','ITEM-2009','추가제품9','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2010','ITEM-2010','추가제품10','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2011','ITEM-2011','추가제품11','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2012','ITEM-2012','추가제품12','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2013','ITEM-2013','추가제품13','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2014','ITEM-2014','추가제품14','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2015','ITEM-2015','추가제품15','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2016','ITEM-2016','추가제품16','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2017','ITEM-2017','추가제품17','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2018','ITEM-2018','추가제품18','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2019','ITEM-2019','추가제품19','F','EA', NULL, 0, 'seed', @base_utc, NULL),
('ITEM-2020','ITEM-2020','추가제품20','F','EA', NULL, 0, 'seed', @base_utc, NULL)
ON DUPLICATE KEY UPDATE item_name=VALUES(item_name), item_type=VALUES(item_type),
unit=VALUES(unit), description=VALUES(description), is_deleted=VALUES(is_deleted),
modified_by=VALUES(modified_by);

