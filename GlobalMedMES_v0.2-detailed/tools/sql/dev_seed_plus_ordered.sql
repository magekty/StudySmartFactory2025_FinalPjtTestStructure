/* ==========================================
   GlobalMed MES • dev_seed_plus_ordered.sql
   목적: v1 개발/테스트/데모용 확장 더미데이터(메뉴/RBAC 포함)
   특성: FK 순서 준수, 일부 멱등(ON DUPLICATE/IGNORE/NOT EXISTS),
        v1 시나리오(로그인→지시→RUN→실적→KPI) + 다장비/다일자 테스트 가능
   ========================================== */

SET NAMES utf8mb4;
SET time_zone = '+00:00';

/* 1) 코드 그룹/코드 (WO_STATUS/EQP_STATUS/INSPECTION_TYPE/DEFECT_TYPE) */
INSERT INTO tb_code_group (group_code, group_name, created_by)
VALUES ('WO_STATUS','작업지시 상태','seed')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

INSERT INTO tb_code_group (group_code, group_name, created_by)
VALUES ('EQP_STATUS','설비 상태','seed')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

INSERT INTO tb_code_group (group_code, group_name, created_by)
VALUES ('INSPECTION_TYPE','검사 유형','seed')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

INSERT INTO tb_code_group (group_code, group_name, created_by)
VALUES ('DEFECT_TYPE','불량 유형','seed')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

-- WO_STATUS
INSERT INTO tb_code (group_code, code, name, use_yn, sort_order, created_by) VALUES
('WO_STATUS','P','Planned','Y',1,'seed'),
('WO_STATUS','R','Released','Y',2,'seed'),
('WO_STATUS','C','Completed','Y',3,'seed')
ON DUPLICATE KEY UPDATE name=VALUES(name), use_yn=VALUES(use_yn), sort_order=VALUES(sort_order);

-- EQP_STATUS
INSERT INTO tb_code (group_code, code, name, use_yn, sort_order, created_by) VALUES
('EQP_STATUS','RUN','가동','Y',1,'seed'),
('EQP_STATUS','IDLE','유휴','Y',2,'seed'),
('EQP_STATUS','DOWN','정지','Y',3,'seed')
ON DUPLICATE KEY UPDATE name=VALUES(name), use_yn=VALUES(use_yn), sort_order=VALUES(sort_order);

-- INSPECTION_TYPE
INSERT INTO tb_code (group_code, code, name, use_yn, sort_order, created_by) VALUES
('INSPECTION_TYPE','INCOMING','수입검사','Y',1,'seed'),
('INSPECTION_TYPE','PROCESS','공정검사','Y',2,'seed'),
('INSPECTION_TYPE','OUTGOING','출하검사','Y',3,'seed')
ON DUPLICATE KEY UPDATE name=VALUES(name), use_yn=VALUES(use_yn);

-- DEFECT_TYPE
INSERT INTO tb_code (group_code, code, name, use_yn, sort_order, created_by) VALUES
('DEFECT_TYPE','SCRATCH','스크래치','Y',1,'seed'),
('DEFECT_TYPE','BURR','버어','Y',2,'seed'),
('DEFECT_TYPE','CRACK','크랙','Y',3,'seed')
ON DUPLICATE KEY UPDATE name=VALUES(name), use_yn=VALUES(use_yn);

-- 코드 id 변수
SET @WO_P    := (SELECT code_id FROM tb_code WHERE group_code='WO_STATUS' AND code='P');
SET @WO_R    := (SELECT code_id FROM tb_code WHERE group_code='WO_STATUS' AND code='R');
SET @WO_C    := (SELECT code_id FROM tb_code WHERE group_code='WO_STATUS' AND code='C');
SET @EQ_RUN  := (SELECT code_id FROM tb_code WHERE group_code='EQP_STATUS' AND code='RUN');
SET @EQ_IDLE := (SELECT code_id FROM tb_code WHERE group_code='EQP_STATUS' AND code='IDLE');
SET @EQ_DOWN := (SELECT code_id FROM tb_code WHERE group_code='EQP_STATUS' AND code='DOWN');
SET @INS_PROC:= (SELECT code_id FROM tb_code WHERE group_code='INSPECTION_TYPE' AND code='PROCESS');
SET @DEF_SCR := (SELECT code_id FROM tb_code WHERE group_code='DEFECT_TYPE' AND code='SCRATCH');

/* 2) 역할/사용자/메뉴/RBAC */
INSERT INTO tb_role (role_code, role_name, created_by) VALUES
('ROLE_OP','운영자','seed'),
('ROLE_QA','품질','seed'),
('ROLE_ADMIN','관리자','seed'),
('ROLE_VIEWER','열람자','seed')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- demo hash(교체 권장) 비밀번호: gmmes1121
INSERT INTO tb_user (user_id, username, password_hash, password_algo, is_active, display_name, created_by, email) VALUES
('00000000-0000-0000-0000-0000000000AD','admin','$2a$10$1CRPepUcsXEBY/r..LUbjObOrb7k8PpTV8D4LBwbME6oUC6tSfdI2','bcrypt',1,'Admin','seed','admin@example.com'),
('00000000-0000-0000-0000-0000000000OP','op','$2a$10$1CRPepUcsXEBY/r..LUbjObOrb7k8PpTV8D4LBwbME6oUC6tSfdI2','bcrypt',1,'Operator','seed',NULL),
('00000000-0000-0000-0000-0000000000QA','qa','$2a$10$1CRPepUcsXEBY/r..LUbjObOrb7k8PpTV8D4LBwbME6oUC6tSfdI2','bcrypt',1,'QA','seed',NULL),
('00000000-0000-0000-0000-0000000000VW','viewer','$2a$10$1CRPepUcsXEBY/r..LUbjObOrb7k8PpTV8D4LBwbME6oUC6tSfdI2','bcrypt',1,'Viewer','seed',NULL)
ON DUPLICATE KEY UPDATE is_active=VALUES(is_active), display_name=VALUES(display_name);

-- USER_ROLE 매핑
INSERT INTO tb_user_role (user_id, role_id, created_by)
SELECT '00000000-0000-0000-0000-0000000000AD', r.role_id, 'seed' FROM tb_role r WHERE r.role_code='ROLE_ADMIN'
ON DUPLICATE KEY UPDATE user_id=user_id;
INSERT INTO tb_user_role (user_id, role_id, created_by)
SELECT '00000000-0000-0000-0000-0000000000OP', r.role_id, 'seed' FROM tb_role r WHERE r.role_code='ROLE_OP'
ON DUPLICATE KEY UPDATE user_id=user_id;
INSERT INTO tb_user_role (user_id, role_id, created_by)
SELECT '00000000-0000-0000-0000-0000000000QA', r.role_id, 'seed' FROM tb_role r WHERE r.role_code='ROLE_QA'
ON DUPLICATE KEY UPDATE user_id=user_id;
INSERT INTO tb_user_role (user_id, role_id, created_by)
SELECT '00000000-0000-0000-0000-0000000000VW', r.role_id, 'seed' FROM tb_role r WHERE r.role_code='ROLE_VIEWER'
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 메뉴
INSERT INTO tb_menu (menu_code, menu_name, path, sort_order, created_by) VALUES
('DASH','대시보드','/dashboard',0,'seed'),
('WO','작업지시','/work-orders',1,'seed'),
('EQPSTAT','설비상태','/equip-status',2,'seed'),
('PERF','실적','/performances',3,'seed'),
('KPI','KPI','/kpi',4,'seed'),
('QUALITY','품질','/quality',5,'seed'),
('ADMIN','관리','/admin',99,'seed')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), path=VALUES(path);

-- ROLE_MENU 권한
-- OP: WO/EQPSTAT/PERF/KPI 읽기·쓰기
INSERT INTO tb_role_menu (role_id, menu_id, allow_read, allow_write, allow_exec, created_by)
SELECT r.role_id, m.menu_id, 1, 1, 0, 'seed'
FROM tb_role r JOIN tb_menu m ON m.menu_code IN ('WO','EQPSTAT','PERF','KPI')
WHERE r.role_code='ROLE_OP'
AND NOT EXISTS(SELECT 1 FROM tb_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);

-- QA: KPI/QUALITY 읽기, QUALITY 쓰기(라이트)
INSERT INTO tb_role_menu (role_id, menu_id, allow_read, allow_write, allow_exec, created_by)
SELECT r.role_id, m.menu_id, 1, IF(m.menu_code='QUALITY',1,0), 0, 'seed'
FROM tb_role r JOIN tb_menu m ON m.menu_code IN ('KPI','QUALITY')
WHERE r.role_code='ROLE_QA'
AND NOT EXISTS(SELECT 1 FROM tb_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);

-- VIEWER: 주요 메뉴 읽기만
INSERT INTO tb_role_menu (role_id, menu_id, allow_read, allow_write, allow_exec, created_by)
SELECT r.role_id, m.menu_id, 1, 0, 0, 'seed'
FROM tb_role r JOIN tb_menu m ON m.menu_code IN ('DASH','WO','EQPSTAT','PERF','KPI')
WHERE r.role_code='ROLE_VIEWER'
AND NOT EXISTS(SELECT 1 FROM tb_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);

-- ADMIN: 전 메뉴 읽기·쓰기·실행
INSERT INTO tb_role_menu (role_id, menu_id, allow_read, allow_write, allow_exec, created_by)
SELECT r.role_id, m.menu_id, 1, 1, 1, 'seed'
FROM tb_role r JOIN tb_menu m
WHERE r.role_code='ROLE_ADMIN'
AND NOT EXISTS(SELECT 1 FROM tb_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);

/* 3) 마스터(워크숍→작업장→공정/품목/창고→설비 2대) */
INSERT INTO tb_workshop (workshop_id, workshop_name, created_by)
VALUES ('WS-0001','MAIN_WORKSHOP','seed')
ON DUPLICATE KEY UPDATE workshop_name=VALUES(workshop_name);

INSERT INTO tb_workcenter (workcenter_id, workcenter_name, workshop_id, created_by) VALUES
('WC-0001','LINE_A','WS-0001','seed'),
('WC-0002','LINE_B','WS-0001','seed')
ON DUPLICATE KEY UPDATE workcenter_name=VALUES(workcenter_name), workshop_id=VALUES(workshop_id);

INSERT INTO tb_process (process_id, process_name, created_by) VALUES
('P-0001','STENT_PROC_A','seed'),
('P-0002','STENT_PROC_B','seed')
ON DUPLICATE KEY UPDATE process_name=VALUES(process_name);

INSERT INTO tb_item (item_id, item_code, item_name, item_type, unit, created_by) VALUES
('I-0001','STENT-01','STENT_01','F','EA','seed'),
('I-0002','WIRE-01','WIRE_01','R','EA','seed'),
('I-0003','SUB-01','SUB_ASSY_01','P','EA','seed')
ON DUPLICATE KEY UPDATE item_name=VALUES(item_name), item_type=VALUES(item_type), unit=VALUES(unit);

INSERT INTO tb_warehouse (warehouse_id, warehouse_name, created_by) VALUES
('W-0001','MAIN_WH','seed')
ON DUPLICATE KEY UPDATE warehouse_name=VALUES(warehouse_name);

INSERT INTO tb_equipment (equipment_id, equipment_name, workcenter_id, process_id, status_code_id, created_by) VALUES
('E-0001','STENT_LINE_01','WC-0001','P-0001', @EQ_RUN,'seed'),
('E-0002','STENT_LINE_02','WC-0002','P-0002', @EQ_RUN,'seed')
ON DUPLICATE KEY UPDATE equipment_name=VALUES(equipment_name), workcenter_id=VALUES(workcenter_id),
                        process_id=VALUES(process_id), status_code_id=@EQ_RUN;

/* 4) 교대/캘린더/배치 (A/B/C, 오늘 기준) */
INSERT INTO tb_shift (shift_id, shift_code, shift_name, start_time, end_time, created_by) VALUES
(1,'A','주간조','08:00:00','16:00:00','seed'),
(2,'B','중간조','16:00:00','00:00:00','seed'),
(3,'C','야간조','00:00:00','08:00:00','seed')
ON DUPLICATE KEY UPDATE shift_name=VALUES(shift_name), start_time=VALUES(start_time), end_time=VALUES(end_time);

-- 오늘 라인A(설비 스코프) 캘린더
INSERT INTO tb_shift_calendar (shift_date, shift_id, equipment_id, workcenter_id, start_ts, end_ts, created_by) VALUES
(CURRENT_DATE(),1,'E-0001',NULL, CONCAT(CURRENT_DATE(),' 08:00:00'), CONCAT(CURRENT_DATE(),' 16:00:00'),'seed')
ON DUPLICATE KEY UPDATE start_ts=VALUES(start_ts), end_ts=VALUES(end_ts);

-- 오늘 라인B(설비 스코프) 캘린더
INSERT INTO tb_shift_calendar (shift_date, shift_id, equipment_id, workcenter_id, start_ts, end_ts, created_by) VALUES
(CURRENT_DATE(),1,'E-0002',NULL, CONCAT(CURRENT_DATE(),' 08:00:00'), CONCAT(CURRENT_DATE(),' 16:00:00'),'seed')
ON DUPLICATE KEY UPDATE start_ts=VALUES(start_ts), end_ts=VALUES(end_ts);

-- 배치: OP를 두 설비에 각각 배치
INSERT INTO tb_shift_assignment (shift_date, shift_id, worker_id, equipment_id, start_ts, end_ts, created_by) VALUES
(CURRENT_DATE(),1,'00000000-0000-0000-0000-0000000000OP','E-0001', CONCAT(CURRENT_DATE(),' 08:00:00'), CONCAT(CURRENT_DATE(),' 16:00:00'),'seed')
ON DUPLICATE KEY UPDATE end_ts=VALUES(end_ts);

INSERT INTO tb_shift_assignment (shift_date, shift_id, worker_id, equipment_id, start_ts, end_ts, created_by) VALUES
(CURRENT_DATE(),1,'00000000-0000-0000-0000-0000000000OP','E-0002', CONCAT(CURRENT_DATE(),' 08:00:00'), CONCAT(CURRENT_DATE(),' 16:00:00'),'seed')
ON DUPLICATE KEY UPDATE end_ts=VALUES(end_ts);

/* 5) 계획/지시(오늘/어제, P/R/C 섞기) */
INSERT INTO tb_production_plan (plan_id, plan_number, item_id, target_qty, start_date, end_date, status, created_by) VALUES
('PL-0001','PL-0001','I-0001', 200, CURRENT_DATE(), CURRENT_DATE(), 'P','seed'),
('PL-0002','PL-0002','I-0001', 180, DATE_SUB(CURRENT_DATE(),INTERVAL 1 DAY), DATE_SUB(CURRENT_DATE(),INTERVAL 1 DAY), 'P','seed')
ON DUPLICATE KEY UPDATE target_qty=VALUES(target_qty);

INSERT INTO tb_work_order (work_order_id, work_order_number, item_id, process_id, equipment_id,
                           order_qty, produced_qty, status_code_id, created_by) VALUES
('WO-0001','WO-0001','I-0001','P-0001','E-0001', 120, 0, @WO_R,'seed'),
('WO-0002','WO-0002','I-0001','P-0002','E-0002',  80, 0, @WO_P,'seed'),
('WO-0003','WO-0003','I-0001','P-0001','E-0001', 100, 0, @WO_C,'seed'), -- 완료 표본
('WO-0004','WO-0004','I-0001','P-0002','E-0002', 150, 0, @WO_R,'seed')
ON DUPLICATE KEY UPDATE order_qty=VALUES(order_qty), status_code_id=VALUES(status_code_id);

/* 6) 설비 상태 로그(오늘/어제, RUN/IDLE MIX) */
INSERT INTO tb_equipment_status_log (equipment_id,status_code_id,start_time,end_time,created_by) VALUES
('E-0001', @EQ_RUN,  CONCAT(CURRENT_DATE(),' 08:00:00'), CONCAT(CURRENT_DATE(),' 10:00:00'),'seed'),
('E-0001', @EQ_IDLE, CONCAT(CURRENT_DATE(),' 10:00:00'), CONCAT(CURRENT_DATE(),' 10:30:00'),'seed'),
('E-0001', @EQ_RUN,  CONCAT(CURRENT_DATE(),' 10:30:00'), NULL,'seed'),
('E-0002', @EQ_RUN,  CONCAT(CURRENT_DATE(),' 08:00:00'), CONCAT(CURRENT_DATE(),' 12:00:00'),'seed'),
('E-0002', @EQ_DOWN, CONCAT(CURRENT_DATE(),' 12:00:00'), CONCAT(CURRENT_DATE(),' 13:00:00'),'seed');

-- 어제 로그 표본
INSERT INTO tb_equipment_status_log (equipment_id,status_code_id,start_time,end_time,created_by) VALUES
('E-0001', @EQ_RUN,  CONCAT(DATE_SUB(CURRENT_DATE(),INTERVAL 1 DAY),' 08:00:00'), CONCAT(DATE_SUB(CURRENT_DATE(),INTERVAL 1 DAY),' 12:00:00'),'seed');

/* 7) 실적(오늘/어제, 다건) */
INSERT INTO tb_production_performance (work_order_id,item_id,process_id,equipment_id,produced_qty,defect_qty,start_time,end_time,created_by) VALUES
('WO-0001','I-0001','P-0001','E-0001', 100,  5, CONCAT(CURRENT_DATE(),' 09:00:00'), CONCAT(CURRENT_DATE(),' 09:30:00'),'seed'),
('WO-0001','I-0001','P-0001','E-0001',  60,  0, CONCAT(CURRENT_DATE(),' 10:00:00'), CONCAT(CURRENT_DATE(),' 10:30:00'),'seed'),
('WO-0004','I-0001','P-0002','E-0002',  80,  3, CONCAT(CURRENT_DATE(),' 09:30:00'), CONCAT(CURRENT_DATE(),' 10:10:00'),'seed'),
('WO-0004','I-0001','P-0002','E-0002',  50,  0, CONCAT(CURRENT_DATE(),' 10:40:00'), CONCAT(CURRENT_DATE(),' 11:20:00'),'seed');

-- 어제 실적
INSERT INTO tb_production_performance (work_order_id,item_id,process_id,equipment_id,produced_qty,defect_qty,start_time,end_time,created_by) VALUES
('WO-0003','I-0001','P-0001','E-0001',  90,  2, CONCAT(DATE_SUB(CURRENT_DATE(),INTERVAL 1 DAY),' 09:00:00'), CONCAT(DATE_SUB(CURRENT_DATE(),INTERVAL 1 DAY),' 09:40:00'),'seed');

/* 8) KPI 타깃(오늘/어제) */
INSERT INTO tb_kpi_target (kpi_date,equipment_id,process_id,item_id,target_oee,target_yield,target_productivity,created_by) VALUES
(CURRENT_DATE(),'E-0001','P-0001','I-0001',75.00,98.00,120.0000,'seed'),
(CURRENT_DATE(),'E-0002','P-0002','I-0001',70.00,97.00,110.0000,'seed')
ON DUPLICATE KEY UPDATE target_oee=VALUES(target_oee), target_yield=VALUES(target_yield), target_productivity=VALUES(target_productivity);

INSERT INTO tb_kpi_target (kpi_date,equipment_id,process_id,item_id,target_oee,target_yield,target_productivity,created_by) VALUES
(DATE_SUB(CURRENT_DATE(),INTERVAL 1 DAY),'E-0001','P-0001','I-0001',75.00,98.00,120.0000,'seed')
ON DUPLICATE KEY UPDATE target_oee=VALUES(target_oee);

/* 9) 품질(라이트) — 공정검사 1건 + 불량/NCR 링크 */
INSERT IGNORE INTO tb_inspection (inspection_id, work_order_id, material_lot_id, item_id, inspection_type_code_id,
                                  inspection_time, worker_id, process_id, equipment_id, created_by)
VALUES (1001,'WO-0001', NULL, 'I-0001', @INS_PROC,
        CONCAT(CURRENT_DATE(),' 11:00:00'), '00000000-0000-0000-0000-0000000000OP', 'P-0001','E-0001','seed');

INSERT IGNORE INTO tb_inspection_result (result_id, inspection_id, item_id, result_value, created_by)
VALUES (2001,1001,'I-0001','OK','seed');

INSERT IGNORE INTO tb_defect (defect_id, inspection_id, defect_type_code_id, defect_qty, created_by)
VALUES (3001,1001,@DEF_SCR,2,'seed');

INSERT IGNORE INTO tb_non_conformance (non_conformance_id, defect_id, cause, corrective_action, preventive_action, report_date, worker_id, created_by)
VALUES (4001,3001,'Scratch','Adjust tension','Add guide', CONCAT(CURRENT_DATE(),' 12:00:00'),'00000000-0000-0000-0000-0000000000OP','seed');

/* 10) 감사 로그(샘플) */
INSERT INTO tb_audit_log (user_id, log_type, message, resource_table, resource_id, created_by)
VALUES ('00000000-0000-0000-0000-0000000000AD','SEED','dev_seed_plus applied','SYSTEM','SEED','seed');
