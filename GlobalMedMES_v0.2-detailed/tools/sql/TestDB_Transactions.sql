-- =========================================
-- GlobalMed MES • DB SMOKE TEST BUNDLE v1
-- 실행 전제: MySQL 8.0.17+, UTC, 스키마 v1 반영
-- “실패가 정상”인 케이스가 다수. 주석의 기대 결과 확인.
-- =========================================

/* T01: 작업지시 수량 음수 금지 (ck_wo_qty_nonneg) */
START TRANSACTION;
INSERT INTO tb_work_order (work_order_id, work_order_number, item_id, process_id, equipment_id,
                           order_qty, produced_qty, status_code_id, created_by)
VALUES ('WO-T01','WO-T01','I-0001','P-0001','E-0001', -1, 0,
        (SELECT code_id FROM tb_code WHERE group_code='WO_STATUS' AND code='P'),'test');
-- 기대: 실패 (CHECK 위반)
ROLLBACK;

/* T02: 설비 상태 로그 시간 역전 금지 (ck_log_time_order) */
START TRANSACTION;
INSERT INTO tb_equipment_status_log (equipment_id,status_code_id,start_time,end_time,created_by)
VALUES ('E-0001',(SELECT code_id FROM tb_code WHERE group_code='EQP_STATUS' AND code='RUN'),
        '2025-08-10 10:00:00','2025-08-10 09:59:59','test');
-- 기대: 실패
ROLLBACK;

/* T03: 교대 캘린더 XOR 위반 - 둘 다 NULL (ck_shiftcal_scope_exclusive) */
START TRANSACTION;
INSERT INTO tb_shift_calendar (shift_date,shift_id,equipment_id,workcenter_id,start_ts,end_ts,created_by)
VALUES (CURRENT_DATE,1,NULL,NULL,UTC_TIMESTAMP(),DATE_ADD(UTC_TIMESTAMP(),INTERVAL 8 HOUR),'test');
-- 기대: 실패
ROLLBACK;

/* T04: 교대 캘린더 XOR 위반 - 둘 다 NOT NULL (ck_shiftcal_scope_exclusive) */
START TRANSACTION;
INSERT INTO tb_shift_calendar (shift_date,shift_id,equipment_id,workcenter_id,start_ts,end_ts,created_by)
VALUES (CURRENT_DATE,1,'E-0001','WC-0001',UTC_TIMESTAMP(),DATE_ADD(UTC_TIMESTAMP(),INTERVAL 8 HOUR),'test');
-- 기대: 실패
ROLLBACK;

/* T05: 교대 캘린더 중복(설비 스코프 부분 유니크 uk_shiftcal_eqp) */
START TRANSACTION;
INSERT INTO tb_shift_calendar (shift_date,shift_id,equipment_id,start_ts,end_ts,created_by)
VALUES ('2025-08-10',1,'E-0001','2025-08-10 00:00:00','2025-08-10 08:00:00','test');
INSERT INTO tb_shift_calendar (shift_date,shift_id,equipment_id,start_ts,end_ts,created_by)
VALUES ('2025-08-10',1,'E-0001','2025-08-10 08:00:00','2025-08-10 16:00:00','test');
-- 기대: 두 번째 INSERT 실패 (UK 충돌)
ROLLBACK;

/* T06: 실적 불량 > 생산 금지 (ck_perf_qty_rel) */
START TRANSACTION;
INSERT INTO tb_production_performance (work_order_id,item_id,process_id,equipment_id,
  produced_qty,defect_qty,start_time,end_time,created_by)
VALUES ('WO-0001','I-0001','P-0001','E-0001',100,120,'2025-08-10 09:00:00','2025-08-10 09:30:00','test');
-- 기대: 실패
ROLLBACK;

/* T07: KPI OEE 범위(0~100) (ck_kpi_oee_range) */
START TRANSACTION;
INSERT INTO tb_kpi_target (kpi_date,equipment_id,process_id,item_id,
                           target_oee,target_yield,target_productivity,created_by)
VALUES ('2025-08-10','E-0001','P-0001','I-0001', 101.00, 98.00, 120.00,'test');
-- 기대: 실패
ROLLBACK;

/* T08: ITEM 유형 허용값 이외 금지 (ck_item_type) */
START TRANSACTION;
INSERT INTO tb_item (item_id,item_code,item_name,item_type,unit,created_by)
VALUES ('I-T08','IT08','TEST_BAD_TYPE','X','EA','test');
-- 기대: 실패
ROLLBACK;

/* T09: BOM 수량 > 0 (ck_bom_qty_pos) */
START TRANSACTION;
INSERT INTO tb_bom (parent_item_id,child_item_id,quantity,line_no,created_by)
VALUES ('I-0001','I-0002', 0, 1, 'test');
-- 기대: 실패
ROLLBACK;

/* T10: MATERIAL_LOT 수량 ≥ 0 (ck_matlot_qty_nonneg) */
START TRANSACTION;
INSERT INTO tb_material_lot (material_lot_id,lot_number,item_id,warehouse_id,quantity,created_by)
VALUES ('LOT-T10','LOT-T10','I-0001','W-0001', -5, 'test');
-- 기대: 실패
ROLLBACK;

/* T11: USER 이메일 UNIQUE (NULL 허용, 그러나 동일 비NULL 중복 불가) */
START TRANSACTION;
INSERT INTO tb_user (user_id,username,password_hash,password_algo,is_active,created_by,email)
VALUES ('U-T11-1','u_t11_1','$2a$10$hash','bcrypt',1,'test','mail@example.com');
INSERT INTO tb_user (user_id,username,password_hash,password_algo,is_active,created_by,email)
VALUES ('U-T11-2','u_t11_2','$2a$10$hash','bcrypt',1,'test','mail@example.com');
-- 기대: 두 번째 INSERT 실패(uk_user_email)
ROLLBACK;

/* T12: WORK_ORDER 번호 UNIQUE (uk_wo_number) */
START TRANSACTION;
INSERT INTO tb_work_order (work_order_id, work_order_number, item_id, process_id, equipment_id,
                           order_qty, produced_qty, status_code_id, created_by)
VALUES ('WO-T12-1','WO-DUP','I-0001','P-0001','E-0001', 10, 0,
        (SELECT code_id FROM tb_code WHERE group_code='WO_STATUS' AND code='P'),'test');
INSERT INTO tb_work_order (work_order_id, work_order_number, item_id, process_id, equipment_id,
                           order_qty, produced_qty, status_code_id, created_by)
VALUES ('WO-T12-2','WO-DUP','I-0001','P-0001','E-0001', 10, 0,
        (SELECT code_id FROM tb_code WHERE group_code='WO_STATUS' AND code='P'),'test');
-- 기대: 두 번째 INSERT 실패
ROLLBACK;

/* (옵션) T13: 배치 중복(설비 스코프 UK) (uk_assign_eqp) */
START TRANSACTION;
INSERT INTO tb_shift_assignment (shift_date,shift_id,worker_id,equipment_id,start_ts,end_ts,created_by)
VALUES ('2025-08-10',1,'00000000-0000-0000-0000-0000000000OP','E-0001','2025-08-10 09:00:00','2025-08-10 17:00:00','test');
INSERT INTO tb_shift_assignment (shift_date,shift_id,worker_id,equipment_id,start_ts,end_ts,created_by)
VALUES ('2025-08-10',1,'00000000-0000-0000-0000-0000000000OP','E-0001','2025-08-10 10:00:00','2025-08-10 18:00:00','test');
-- 기대: 두 번째 INSERT 실패
ROLLBACK;

/* (옵션) T14: 배치 XOR 위반(둘 다 NOT NULL) (ck_assign_scope_exclusive) */
START TRANSACTION;
INSERT INTO tb_shift_assignment (shift_date,shift_id,worker_id,equipment_id,workcenter_id,start_ts,end_ts,created_by)
VALUES ('2025-08-10',1,'00000000-0000-0000-0000-0000000000OP','E-0001','WC-0001','2025-08-10 09:00:00','2025-08-10 17:00:00','test');
-- 기대: 실패
ROLLBACK;

/* (옵션) T15: KPI 유니크(일×설비×공정×품목) */
START TRANSACTION;
INSERT INTO tb_kpi_target (kpi_date,equipment_id,process_id,item_id,target_oee,created_by)
VALUES ('2025-08-10','E-0001','P-0001','I-0001',75,'test');
INSERT INTO tb_kpi_target (kpi_date,equipment_id,process_id,item_id,target_oee,created_by)
VALUES ('2025-08-10','E-0001','P-0001','I-0001',80,'test');
-- 기대: 두 번째 INSERT 실패(UK 충돌)
ROLLBACK;

/* (옵션) T16: ROLE 삭제 RESTRICT */
START TRANSACTION;
-- 전제: tb_user_role에 ROLE_OP 참조 존재
DELETE FROM tb_role WHERE role_code='ROLE_OP';
-- 기대: 실패(FK RESTRICT)
ROLLBACK;
