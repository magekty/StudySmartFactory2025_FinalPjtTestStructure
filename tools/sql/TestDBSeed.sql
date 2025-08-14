/* ==========================================
   GlobalMed MES • Smoke Seeds v1 (for T01~T16)
   MySQL 8.0+, UTC, 스키마 v1 전제
   ========================================== */

SET NAMES utf8mb4;
SET time_zone = '+00:00';


/* ==========================================
   ROLE 삭제 RESTRICT 재현용 시드 패치
   전제: 스키마 v1, MySQL 8+, UTC
   ========================================== */

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- 1) ROLE_OP 보장(이미 있으면 갱신)
INSERT INTO tb_role (role_code, role_name, description, created_by)
VALUES ('ROLE_OP','운영자','작업 실행 권한','seed')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), description=VALUES(description);

-- 2) 작업자 계정 보장(이전 시드와 동일 ID 사용) 비밀번호: gmmes1121
INSERT INTO tb_user (user_id, username, password_hash, password_algo, is_active, created_by, email)
VALUES ('00000000-0000-0000-0000-0000000000OP','op','$2a$10$1CRPepUcsXEBY/r..LUbjObOrb7k8PpTV8D4LBwbME6oUC6tSfdI2','bcrypt',1,'seed', NULL)
ON DUPLICATE KEY UPDATE is_active=VALUES(is_active);

-- 3) USER_ROLE 매핑 생성(멱등)
--   - uk_user_role(user_id, role_id) 때문에 NOT EXISTS로 안전 삽입
INSERT INTO tb_user_role (user_id, role_id, created_by)
SELECT '00000000-0000-0000-0000-0000000000OP', r.role_id, 'seed'
FROM tb_role r
WHERE r.role_code = 'ROLE_OP'
  AND NOT EXISTS (
    SELECT 1 FROM tb_user_role ur
    WHERE ur.user_id = '00000000-0000-0000-0000-0000000000OP'
      AND ur.role_id = r.role_id
  );

-- 4) 검증(선택): 매핑 존재 확인
-- SELECT ur.user_role_id, ur.user_id, ur.role_id
-- FROM tb_user_role ur
-- JOIN tb_role r ON r.role_id = ur.role_id
-- WHERE ur.user_id='00000000-0000-0000-0000-0000000000OP' AND r.role_code='ROLE_OP';

/* 1) 코드 그룹/코드 (WO_STATUS, EQP_STATUS) */
INSERT INTO tb_code_group (group_code, group_name, created_by)
VALUES ('WO_STATUS','작업지시 상태','seed')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

INSERT INTO tb_code_group (group_code, group_name, created_by)
VALUES ('EQP_STATUS','설비 상태','seed')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

-- WO_STATUS: P/R/C
INSERT INTO tb_code (group_code, code, name, use_yn, sort_order, created_by)
VALUES
('WO_STATUS','P','Planned','Y',1,'seed'),
('WO_STATUS','R','Released','Y',2,'seed'),
('WO_STATUS','C','Completed','Y',3,'seed')
ON DUPLICATE KEY UPDATE name=VALUES(name), use_yn=VALUES(use_yn), sort_order=VALUES(sort_order);

-- EQP_STATUS: RUN/IDLE/DOWN
INSERT INTO tb_code (group_code, code, name, use_yn, sort_order, created_by)
VALUES
('EQP_STATUS','RUN','가동','Y',1,'seed'),
('EQP_STATUS','IDLE','유휴','Y',2,'seed'),
('EQP_STATUS','DOWN','정지','Y',3,'seed')
ON DUPLICATE KEY UPDATE name=VALUES(name), use_yn=VALUES(use_yn), sort_order=VALUES(sort_order);

-- 코드 id 변수 확보
SET @WO_P := (SELECT code_id FROM tb_code WHERE group_code='WO_STATUS' AND code='P');
SET @WO_R := (SELECT code_id FROM tb_code WHERE group_code='WO_STATUS' AND code='R');
SET @EQ_RUN := (SELECT code_id FROM tb_code WHERE group_code='EQP_STATUS' AND code='RUN');

/* 2) 마스터(작업장/작업센터/공정/품목/창고) */
INSERT INTO tb_workshop (workshop_id, workshop_name, created_by)
VALUES ('WS-0001','MAIN_WORKSHOP','seed')
ON DUPLICATE KEY UPDATE workshop_name=VALUES(workshop_name);

INSERT INTO tb_workcenter (workcenter_id, workcenter_name, workshop_id, created_by)
VALUES ('WC-0001','LINE_A','WS-0001','seed')
ON DUPLICATE KEY UPDATE workcenter_name=VALUES(workcenter_name), workshop_id=VALUES(workshop_id);

INSERT INTO tb_process (process_id, process_name, created_by)
VALUES ('P-0001','STENT_PROC','seed')
ON DUPLICATE KEY UPDATE process_name=VALUES(process_name);

INSERT INTO tb_item (item_id, item_code, item_name, item_type, unit, created_by)
VALUES
('I-0001','STENT-01','STENT_01','F','EA','seed'),
('I-0002','WIRE-01','WIRE_01','R','EA','seed')
ON DUPLICATE KEY UPDATE item_name=VALUES(item_name), item_type=VALUES(item_type), unit=VALUES(unit);

INSERT INTO tb_warehouse (warehouse_id, warehouse_name, created_by)
VALUES ('W-0001','MAIN_WH','seed')
ON DUPLICATE KEY UPDATE warehouse_name=VALUES(warehouse_name);

/* 3) 사용자(작업자) — SHIFT_ASSIGNMENT 테스트용 */
-- 데모용 비밀번호 해시 placeholder (교체 권장)
INSERT INTO tb_user (user_id, username, password_hash, password_algo, is_active, created_by, email)
VALUES ('00000000-0000-0000-0000-0000000000OP','op','$2a$10$demo_hash_replace','bcrypt',1,'seed', NULL)
ON DUPLICATE KEY UPDATE is_active=VALUES(is_active);

/* 4) 교대(shift_id=1 보장) */
-- shift_id=1이 필요하므로 명시 삽입
INSERT INTO tb_shift (shift_id, shift_code, shift_name, start_time, end_time, created_by)
VALUES (1,'A','주간조','08:00:00','17:00:00','seed')
ON DUPLICATE KEY UPDATE shift_name=VALUES(shift_name), start_time=VALUES(start_time), end_time=VALUES(end_time);

/* 5) 설비 — EQP_STATUS(RUN) 상태코드 FK 필요 */
INSERT INTO tb_equipment (equipment_id, equipment_name, workcenter_id, process_id, status_code_id, created_by)
VALUES ('E-0001','STENT_LINE_01','WC-0001','P-0001', @EQ_RUN, 'seed')
ON DUPLICATE KEY UPDATE equipment_name=VALUES(equipment_name), workcenter_id=VALUES(workcenter_id),
                        process_id=VALUES(process_id), status_code_id=@EQ_RUN;

/* 6) 기준 작업지시 — WO-0001 (상태=R 권장) */
INSERT INTO tb_work_order (work_order_id, work_order_number, item_id, process_id, equipment_id,
                           order_qty, produced_qty, status_code_id, created_by)
VALUES ('WO-0001','WO-0001','I-0001','P-0001','E-0001', 100, 0, @WO_R, 'seed')
ON DUPLICATE KEY UPDATE item_id=VALUES(item_id), process_id=VALUES(process_id),
                        equipment_id=VALUES(equipment_id), order_qty=VALUES(order_qty),
                        status_code_id=@WO_R;

/* 7) (선택) KPI_TARGET 데모 1건 — 스모크엔 필수 아님 */
INSERT IGNORE INTO tb_kpi_target (kpi_date, equipment_id, process_id, item_id, target_oee, target_yield, target_productivity, created_by)
VALUES (CURRENT_DATE(),'E-0001','P-0001','I-0001',75.00,98.00,120.0000,'seed');
