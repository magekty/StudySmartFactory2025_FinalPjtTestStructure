START TRANSACTION;

-- `tb_menu` 테이블에 신규 메뉴 데이터 삽입 (auto_increment이므로 menu_id는 제거)
INSERT INTO tb_menu (parent_id, menu_code, menu_name, path, sort_order, is_active, created_by, created_at)
VALUES
    (2, 'WO_CREATE', '작업지시생성', '/work-orders/create', 1, 1, 'seed', NOW()),
    (6, 'QC_INSP', '공정검사', '/quality/inspection', 1, 1, 'seed', NOW());

-- 2. `tb_role_menu` 테이블에 시나리오별 권한 데이터 삽입 (role_menu_id는 제거)

-- 시나리오 1: 관리자 (ROLE_ADMIN, role_id 3)
-- 관리자는 모든 메뉴에 대한 모든 권한을 가진다.
INSERT INTO tb_role_menu (role_id, menu_id, allow_read, allow_write, allow_exec, is_deleted, created_by, created_at)
VALUES
    (3, 8, 1, 1, 1, 0, 'seed', NOW()), -- 작업지시생성
    (3, 9, 1, 1, 1, 0, 'seed', NOW()); -- 공정검사

-- 시나리오 2: 운영자 (ROLE_OP, role_id 1)
-- 작업지시(menu_id 2) 및 그 하위 메뉴인 작업지시생성(menu_id 8)에 대한 권한
-- '품질'(menu_id 6) 메뉴는 권한이 없어야 함
INSERT INTO tb_role_menu (role_id, menu_id, allow_read, allow_write, allow_exec, is_deleted, created_by, created_at)
VALUES
    (1, 8, 1, 1, 0, 0, 'seed', NOW()), -- 작업지시생성
    (1, 6, 0, 0, 0, 0, 'seed', NOW()); -- 품질 (권한 없음)

-- 시나리오 3: 품질 (ROLE_QA, role_id 2)
-- 품질(menu_id 6) 및 그 하위 메뉴인 공정검사(menu_id 9)에 대한 권한
INSERT INTO tb_role_menu (role_id, menu_id, allow_read, allow_write, allow_exec, is_deleted, created_by, created_at)
VALUES
    (2, 9, 1, 1, 0, 0, 'seed', NOW()); -- 공정검사

COMMIT;

-- 만약 중간에 오류가 발생하면 롤백을 통해 모든 변경사항을 취소할 수 있습니다.
-- 예: ROLLBACK;