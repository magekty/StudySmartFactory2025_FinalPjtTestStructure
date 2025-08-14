-- Schema_v1.sql
-- -----------------------------------------------------
-- Database 생성
-- -----------------------------------------------------
CREATE DATABASE IF NOT EXISTS `globalmed` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `globalmed`;

-- Batch 1: 코드 그룹 및 코드 테이블
-- 역할/사용 시나리오: 시스템 전반에 사용되는 공통 코드(상태, 유형, 사유 등)를 관리.
-- 주요 조인 키: tb_code_group.group_code
-- 삭제 정책: 소프트 삭제(is_deleted)
-- UTC: created_at, modified_at, deleted_at 컬럼은 UTC 저장.

CREATE TABLE `tb_code_group` (
  `group_code` VARCHAR(50) NOT NULL COMMENT '코드 그룹 ID (PK)',
  `group_name` VARCHAR(100) NOT NULL COMMENT '코드 그룹명 (유일)',
  `description` VARCHAR(255) NULL COMMENT '그룹에 대한 상세 설명',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`group_code`),
  UNIQUE KEY `uk_code_group_name` (`group_name`)
) ENGINE=InnoDB COMMENT='코드 그룹 마스터: 시스템 공통 코드의 상위 그룹을 정의하는 테이블. 대표 조인: tb_code.';


CREATE TABLE `tb_code` (
  `code_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '코드 ID (PK, 서로게이트 키)',
  `group_code` VARCHAR(50) NOT NULL COMMENT '코드 그룹 ID (FK)',
  `code` VARCHAR(50) NOT NULL COMMENT '코드 값 (그룹 내 유일)',
  `name` VARCHAR(100) NOT NULL COMMENT '코드 명칭',
  `description` VARCHAR(255) NULL COMMENT '코드에 대한 상세 설명',
  `use_yn` CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부 (Y/N)',
  `sort_order` INT NULL COMMENT '정렬 순서',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`code_id`),
  UNIQUE KEY `uk_code_group_code` (`group_code`, `code`),
  KEY `idx_code_group_sort` (`group_code`, `sort_order`),
  CONSTRAINT `fk_code_group_code` FOREIGN KEY (`group_code`) REFERENCES `tb_code_group`(`group_code`) ON DELETE RESTRICT,
  CONSTRAINT `ck_code_use_yn` CHECK (`use_yn` IN ('Y', 'N'))
) ENGINE=InnoDB COMMENT='코드 마스터: 그룹에 종속된 개별 코드를 정의하는 테이블. 대표 조인: tb_equipment.status_code_id, tb_work_order.status_code_id 등.';
-- Batch 2: 교대, 달력, 작업자 배치 테이블
-- 역할/사용 시나리오: 생산/작업 계획 수립 및 실적 집계를 위한 시간/인력 할당 기준을 정의.
-- 주요 조인 키: tb_shift.shift_id, tb_shift_calendar.calendar_id, tb_shift_assignment.assignment_id
-- 삭제 정책: 소프트 삭제(is_deleted)
-- UTC: 모든 DATETIME 컬럼은 UTC 저장.

CREATE TABLE `tb_shift` (
  `shift_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '교대 ID (PK)',
  `shift_code` VARCHAR(10) NOT NULL COMMENT '교대 코드 (유일, 예: A, B, C)',
  `shift_name` VARCHAR(50) NOT NULL COMMENT '교대 명칭 (예: 주간조)',
  `start_time` TIME NOT NULL COMMENT '교대 시작 시간',
  `end_time` TIME NOT NULL COMMENT '교대 종료 시간',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`shift_id`),
  UNIQUE KEY `uk_shift_code` (`shift_code`)
) ENGINE=InnoDB COMMENT='교대 마스터: 교대 근무의 기본 정보(코드, 시간 등)를 정의하는 테이블.';


CREATE TABLE `tb_workshop` (
  `workshop_id` VARCHAR(36) NOT NULL COMMENT '작업장 그룹 ID (PK, UUID)',
  `workshop_name` VARCHAR(255) NOT NULL COMMENT '작업장 그룹명 (유일)',
  `description` VARCHAR(255) NULL COMMENT '설명',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`workshop_id`),
  UNIQUE KEY `uk_workshop_name` (`workshop_name`)
) ENGINE=InnoDB COMMENT='작업장 그룹 마스터: 생산 라인의 상위 그룹을 정의하는 테이블. 대표 조인: tb_workcenter.';



CREATE TABLE `tb_workcenter` (
  `workcenter_id` VARCHAR(36) NOT NULL COMMENT '작업장 ID (PK, UUID)',
  `workcenter_name` VARCHAR(255) NOT NULL COMMENT '작업장명 (유일)',
  `workshop_id` VARCHAR(36) NOT NULL COMMENT '작업장 그룹 ID (FK)',
  `description` VARCHAR(255) NULL COMMENT '설명',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`workcenter_id`),
  UNIQUE KEY `uk_workcenter_name` (`workcenter_name`),
  KEY `idx_workcenter_workshop_id` (`workshop_id`),
  CONSTRAINT `fk_workcenter_workshop` FOREIGN KEY (`workshop_id`) REFERENCES `tb_workshop`(`workshop_id`) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='작업장 마스터: 설비 및 공정의 상위 그룹. 대표 조인: tb_equipment, tb_shift_calendar.';


CREATE TABLE `tb_process` (
  `process_id` VARCHAR(36) NOT NULL COMMENT '공정 ID (PK, UUID)',
  `process_name` VARCHAR(255) NOT NULL COMMENT '공정명 (유일)',
  `description` VARCHAR(255) NULL COMMENT '설명',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`process_id`),
  UNIQUE KEY `uk_process_name` (`process_name`)
) ENGINE=InnoDB COMMENT='공정 마스터: 생산 라우팅의 작업 단계를 정의하는 테이블. 대표 조인: tb_equipment, tb_work_order, tb_production_performance.';


CREATE TABLE `tb_equipment` (
  `equipment_id` VARCHAR(36) NOT NULL COMMENT '설비 ID (PK, UUID)',
  `equipment_name` VARCHAR(255) NOT NULL COMMENT '설비명 (유일)',
  `workcenter_id` VARCHAR(36) NOT NULL COMMENT '작업장 ID (FK)',
  `process_id` VARCHAR(36) NOT NULL COMMENT '공정 ID (FK)',
  `status_code_id` BIGINT NOT NULL COMMENT '설비 상태 코드 ID (FK→tb_code)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`equipment_id`),
  UNIQUE KEY `uk_equipment_name` (`equipment_name`),
  KEY `idx_equipment_workcenter_id` (`workcenter_id`),
  KEY `idx_equipment_process_id` (`process_id`),
  KEY `idx_equipment_status_code_id` (`status_code_id`),
  KEY `idx_equipment_proc_wc` (`process_id`, `workcenter_id`),
  CONSTRAINT `fk_eqp_wc` FOREIGN KEY (`workcenter_id`) REFERENCES `tb_workcenter`(`workcenter_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_eqp_proc` FOREIGN KEY (`process_id`) REFERENCES `tb_process`(`process_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_eqp_status` FOREIGN KEY (`status_code_id`) REFERENCES `tb_code`(`code_id`) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='설비 마스터: 실적/상태로그/작업지시/교대달력과 결합되는 핵심 마스터(UTC).';


CREATE TABLE `tb_shift_calendar` (
  `calendar_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '교대 달력 ID (PK)',
  `shift_date` DATE NOT NULL COMMENT '근무 날짜',
  `shift_id` BIGINT NOT NULL COMMENT '교대 ID (FK)',
  `equipment_id` VARCHAR(36) NULL COMMENT '설비 ID (FK) - 선택',
  `workcenter_id` VARCHAR(36) NULL COMMENT '작업장 ID (FK) - 선택',
  `start_ts` DATETIME NOT NULL COMMENT '교대 시작 (UTC)',
  `end_ts` DATETIME NOT NULL COMMENT '교대 종료 (UTC)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`calendar_id`),
  -- 스코프별 부분 유니크(중복 차단)
  UNIQUE KEY `uk_shiftcal_eqp` (`shift_date`, `shift_id`, `equipment_id`),
  UNIQUE KEY `uk_shiftcal_wc`  (`shift_date`, `shift_id`, `workcenter_id`),
  KEY `idx_shiftcal_date_shift_eqp` (`shift_date`, `shift_id`, `equipment_id`),
  KEY `idx_shiftcal_date_shift_wc`  (`shift_date`, `shift_id`, `workcenter_id`),
  CONSTRAINT `fk_shiftcal_shift` FOREIGN KEY (`shift_id`) REFERENCES `tb_shift`(`shift_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_shiftcal_eqp`   FOREIGN KEY (`equipment_id`) REFERENCES `tb_equipment`(`equipment_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_shiftcal_wc`    FOREIGN KEY (`workcenter_id`) REFERENCES `tb_workcenter`(`workcenter_id`) ON DELETE RESTRICT,
  -- 한 로우는 설비 또는 작업장 한 가지만 지정(XOR)
  CONSTRAINT `ck_shiftcal_scope_exclusive`
    CHECK ( (equipment_id IS NOT NULL) <> (workcenter_id IS NOT NULL) ),
  -- 시간 무결성
  CONSTRAINT `ck_shiftcal_time_order`
    CHECK (`end_ts` > `start_ts`)
) ENGINE=InnoDB COMMENT='교대 달력: 날짜/교대 × (설비 또는 작업장) 단위의 집계 프레임(UTC). XOR+부분 유니크로 중복 차단.';


-- tb_role (최종안)
CREATE TABLE `tb_role` (
  `role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '역할 ID (PK)',
  `role_code` VARCHAR(50) NOT NULL COMMENT '역할 코드 (전역 유일) 예: ROLE_OP, ROLE_QA, ROLE_ADMIN',
  `role_name` VARCHAR(100) NOT NULL COMMENT '역할 명칭',
  `description` VARCHAR(255) NULL COMMENT '설명',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  CONSTRAINT `ck_role_is_deleted` CHECK (`is_deleted` IN (0,1))
) ENGINE=InnoDB COMMENT='역할 마스터: 인가 레벨 정의. ROLE_OP/QA/ADMIN 기본 시드 권장(UTC).';


-- tb_user (최종안)
CREATE TABLE `tb_user` (
  `user_id` VARCHAR(36) NOT NULL COMMENT '사용자 ID (UUID, PK)',
  `username` VARCHAR(50) NOT NULL COMMENT '로그인 ID (유일, 소문자 권장)',
  `email` VARCHAR(255) NULL COMMENT '이메일(선택, 유일 권장)',
  `password_hash` VARCHAR(100) NOT NULL COMMENT '비밀번호 해시(예: BCrypt 60자, Argon2id 가능)',
  `password_algo` VARCHAR(20) NOT NULL DEFAULT 'bcrypt' COMMENT '해시 알고리즘(bcrypt/argon2id)',
  `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '활성 여부(1/0)',
  `failed_login_count` INT NOT NULL DEFAULT 0 COMMENT '연속 실패 횟수(>=0)',
  `locked_until` DATETIME NULL COMMENT '잠금 해제 예정 시각(UTC)',
  `last_login_at` DATETIME NULL COMMENT '마지막 로그인 시각(UTC)',
  `phone` VARCHAR(30) NULL COMMENT '연락처(선택)',
  `display_name` VARCHAR(100) NULL COMMENT '표시명(선택)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`),
  CONSTRAINT `ck_user_is_active`  CHECK (`is_active` IN (0,1)),
  CONSTRAINT `ck_user_is_deleted` CHECK (`is_deleted` IN (0,1)),
  CONSTRAINT `ck_user_failed_cnt` CHECK (`failed_login_count` >= 0)
  -- 선택: 자주 쓰면 인덱스 추가
  -- , KEY `idx_user_last_login` (`last_login_at`)
) ENGINE=InnoDB COMMENT='사용자 마스터: 로그인/상태/보안 메타. 해시는 필수, 평문 금지(UTC).';

-- tb_user_role (보수형: FK RESTRICT 유지)
CREATE TABLE `tb_user_role` (
  `user_role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자-역할 매핑 ID (PK)',
  `user_id` VARCHAR(36) NOT NULL COMMENT '사용자 ID (FK)',
  `role_id` BIGINT NOT NULL COMMENT '역할 ID (FK)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그(0/1)',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`user_role_id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_userrole_user` (`user_id`),
  KEY `idx_userrole_role` (`role_id`),
  CONSTRAINT `fk_userrole_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user`(`user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_userrole_role` FOREIGN KEY (`role_id`) REFERENCES `tb_role`(`role_id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_userrole_is_deleted` CHECK (`is_deleted` IN (0,1))
) ENGINE=InnoDB COMMENT='사용자-역할 매핑(보수형): FK 삭제 RESTRICT, 중복 배정 방지(UTC).';


-- tb_menu (보수형: 부모 FK RESTRICT, 셀프 루프 금지)
CREATE TABLE `tb_menu` (
  `menu_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '메뉴 ID (PK)',
  `parent_id` BIGINT NULL COMMENT '부모 메뉴 ID (Self FK, 최상위 NULL)',
  `menu_code` VARCHAR(100) NOT NULL COMMENT '메뉴 코드(전역 유일) 예: DASHBOARD, PROD_RESULT',
  `menu_name` VARCHAR(150) NOT NULL COMMENT '메뉴 표시명',
  `path` VARCHAR(255) NOT NULL COMMENT '라우트/URL 경로 예: /dashboard',
  `component` VARCHAR(255) NULL COMMENT '프론트 컴포넌트 키(선택)',
  `icon` VARCHAR(100) NULL COMMENT '아이콘 키(선택)',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
  `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '비로그인 접근(0/1)',
  `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '활성(0/1)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제(0/1)',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`menu_id`),
  UNIQUE KEY `uk_menu_code` (`menu_code`),
  KEY `idx_menu_parent` (`parent_id`),
  KEY `idx_menu_path` (`path`),
  KEY `idx_menu_sort` (`sort_order`),
  KEY `idx_menu_parent_sort` (`parent_id`, `sort_order`),
  CONSTRAINT `fk_menu_parent` FOREIGN KEY (`parent_id`) REFERENCES `tb_menu`(`menu_id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_menu_flags` CHECK (`is_public` IN (0,1) AND `is_active` IN (0,1) AND `is_deleted` IN (0,1))
) ENGINE=InnoDB COMMENT='메뉴 마스터(보수형): 부모 삭제 전 자식 정리 필요, 트리/정렬 인덱스 강화(UTC).';


-- tb_role_menu (보수형: FK RESTRICT 유지)
CREATE TABLE `tb_role_menu` (
  `role_menu_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '역할-메뉴 매핑 ID (PK)',
  `role_id` BIGINT NOT NULL COMMENT '역할 ID (FK)',
  `menu_id` BIGINT NOT NULL COMMENT '메뉴 ID (FK)',
  `allow_read` TINYINT NOT NULL DEFAULT 1 COMMENT '읽기 허용(0/1)',
  `allow_write` TINYINT NOT NULL DEFAULT 0 COMMENT '쓰기 허용(0/1)',
  `allow_exec` TINYINT NOT NULL DEFAULT 0 COMMENT '실행 허용(0/1)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제(0/1)',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`role_menu_id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_rm_role` (`role_id`),
  KEY `idx_rm_menu` (`menu_id`),
  CONSTRAINT `fk_rm_role` FOREIGN KEY (`role_id`) REFERENCES `tb_role`(`role_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_rm_menu` FOREIGN KEY (`menu_id`) REFERENCES `tb_menu`(`menu_id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_rm_flags`
    CHECK (`allow_read` IN (0,1) AND `allow_write` IN (0,1) AND `allow_exec` IN (0,1) AND `is_deleted` IN (0,1))
) ENGINE=InnoDB COMMENT='역할-메뉴 매핑(보수형): FK 삭제 RESTRICT, 플래그 무결성(UTC).';



CREATE TABLE `tb_shift_assignment` (
  `assignment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 ID (PK)',
  `shift_date` DATE NOT NULL COMMENT '근무 날짜',
  `shift_id` BIGINT NOT NULL COMMENT '교대 ID (FK)',
  `worker_id` VARCHAR(36) NOT NULL COMMENT '작업자 ID (FK→tb_user.user_id)',
  `equipment_id` VARCHAR(36) NULL COMMENT '설비 배치 시 사용',
  `workcenter_id` VARCHAR(36) NULL COMMENT '작업장 배치 시 사용',
  `start_ts` DATETIME NOT NULL COMMENT '배치 시작 (UTC)',
  `end_ts` DATETIME NOT NULL COMMENT '배치 종료 (UTC)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`assignment_id`),
  -- 스코프별 부분 유니크(동일 교대/날짜/작업자에 동일 스코프 중복 차단)
  UNIQUE KEY `uk_assign_eqp` (`shift_date`, `shift_id`, `worker_id`, `equipment_id`),
  UNIQUE KEY `uk_assign_wc`  (`shift_date`, `shift_id`, `worker_id`, `workcenter_id`),
  KEY `idx_assign_shift_worker` (`shift_date`, `shift_id`, `worker_id`),
  KEY `idx_assign_worker` (`worker_id`),
  KEY `idx_assign_eqp` (`equipment_id`),
  KEY `idx_assign_wc`  (`workcenter_id`),
  CONSTRAINT `fk_assign_shift` FOREIGN KEY (`shift_id`) REFERENCES `tb_shift`(`shift_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_assign_user`  FOREIGN KEY (`worker_id`) REFERENCES `tb_user`(`user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_assign_eqp`   FOREIGN KEY (`equipment_id`) REFERENCES `tb_equipment`(`equipment_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_assign_wc`    FOREIGN KEY (`workcenter_id`) REFERENCES `tb_workcenter`(`workcenter_id`) ON DELETE RESTRICT,
  -- 한 로우는 설비 또는 작업장 한 가지만 지정(XOR)
  CONSTRAINT `ck_assign_scope_exclusive`
    CHECK ( (equipment_id IS NOT NULL) <> (workcenter_id IS NOT NULL) ),
  -- 시간 무결성
  CONSTRAINT `ck_assign_time_order`
    CHECK (`end_ts` > `start_ts`)
) ENGINE=InnoDB COMMENT='교대 배치: 날짜/교대/작업자 × (설비 또는 작업장) 다건 배정(UTC). XOR+부분 유니크, 작업자=tb_user FK.';


-- Batch 3: 핵심 마스터 테이블
-- 역할/사용 시나리오: 생산의 4대 요소(설비, 공정, 품목, 위치)를 정의.
-- 주요 조인 키: tb_item.item_id, tb_process.process_id, tb_workcenter.workcenter_id, tb_workshop.workshop_id, tb_warehouse.warehouse_id
-- 삭제 정책: 소프트 삭제(is_deleted)
-- UTC: created_at, modified_at, deleted_at, start_ts/end_ts 컬럼은 UTC 저장.







CREATE TABLE `tb_item` (
  `item_id` VARCHAR(36) NOT NULL COMMENT '품목 ID (PK, UUID)',
  `item_code` VARCHAR(50) NOT NULL COMMENT '품목 코드 (유일)',
  `item_name` VARCHAR(255) NOT NULL COMMENT '품목명',
  `item_type` CHAR(1) NOT NULL COMMENT '품목 유형 (R:원자재, P:반제품, F:완제품)',
  `unit` VARCHAR(10) NOT NULL COMMENT '단위 (예: EA, KG)',
  `description` VARCHAR(255) NULL COMMENT '설명',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `uk_item_code` (`item_code`),
  KEY `idx_item_name` (`item_name`),
  CONSTRAINT `ck_item_type` CHECK (`item_type` IN ('R','P','F'))
) ENGINE=InnoDB COMMENT='품목 마스터: 생산/재고 관리 대상. 대표 조인: tb_work_order, tb_bom, tb_material_lot.';


CREATE TABLE `tb_warehouse` (
  `warehouse_id` VARCHAR(36) NOT NULL COMMENT '창고 ID (PK, UUID)',
  `warehouse_name` VARCHAR(255) NOT NULL COMMENT '창고명 (유일)',
  `location` VARCHAR(255) NULL COMMENT '위치 정보',
  `description` VARCHAR(255) NULL COMMENT '설명',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`warehouse_id`),
  UNIQUE KEY `uk_warehouse_name` (`warehouse_name`)
) ENGINE=InnoDB COMMENT='창고 마스터: 자재 및 제품의 보관 장소를 정의하는 테이블. 대표 조인: tb_material_lot.';
-- Batch 4: 설비 마스터, 생산 계획, 작업 지시 테이블
-- 역할/사용 시나리오: 생산의 핵심 흐름(설비, 계획, 지시)을 정의.
-- 주요 조인 키: tb_equipment.equipment_id, tb_production_plan.plan_id, tb_work_order.work_order_id
-- 삭제 정책: 소프트 삭제(is_deleted)
-- UTC: created_at, modified_at, deleted_at, start_ts/end_ts 컬럼은 UTC 저장.


CREATE TABLE `tb_production_plan` (
  `plan_id` VARCHAR(36) NOT NULL COMMENT '생산 계획 ID (PK, UUID)',
  `plan_number` VARCHAR(50) NOT NULL COMMENT '계획 번호 (유일)',
  `item_id` VARCHAR(36) NOT NULL COMMENT '계획 품목 ID (FK)',
  `target_qty` DECIMAL(10,4) NOT NULL COMMENT '계획 수량',
  `start_date` DATE NOT NULL COMMENT '계획 시작일',
  `end_date` DATE NOT NULL COMMENT '계획 종료일',
  `status` CHAR(1) NOT NULL COMMENT '계획 상태 (P:Planned, R:Released, C:Completed)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`plan_id`),
  UNIQUE KEY `uk_plan_number` (`plan_number`),
  KEY `idx_plan_item_id` (`item_id`),
  KEY `idx_plan_start_date` (`start_date`),
  CONSTRAINT `fk_plan_item` FOREIGN KEY (`item_id`) REFERENCES `tb_item`(`item_id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_plan_dates` CHECK (`end_date` >= `start_date`),
  CONSTRAINT `ck_plan_status` CHECK (`status` IN ('P','R','C')),
  CONSTRAINT `ck_plan_qty_nonneg` CHECK (`target_qty` >= 0)
) ENGINE=InnoDB COMMENT='생산 계획: 목표/기간/상태 무결성 보장. 대표 조인: tb_work_order.';

-- tb_work_order (최종안)
CREATE TABLE `tb_work_order` (
  `work_order_id` VARCHAR(36) NOT NULL COMMENT '작업 지시 ID (PK, UUID)',
  `plan_id` VARCHAR(36) NULL COMMENT '생산 계획 ID (FK) - 선택',
  `work_order_number` VARCHAR(50) NOT NULL COMMENT '작업 지시 번호 (유일)',
  `item_id` VARCHAR(36) NOT NULL COMMENT '생산 품목 ID (FK)',
  `process_id` VARCHAR(36) NOT NULL COMMENT '지시 공정 ID (FK)',
  `equipment_id` VARCHAR(36) NOT NULL COMMENT '지시 설비 ID (FK)',
  `order_qty` DECIMAL(10,4) NOT NULL COMMENT '지시 수량',
  `produced_qty` DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '생산 완료 수량',
  `start_ts` DATETIME NULL COMMENT '지시 시작 타임스탬프 (UTC)',
  `end_ts` DATETIME NULL COMMENT '지시 종료 타임스탬프 (UTC)',
  `status_code_id` BIGINT NOT NULL COMMENT '작업 지시 상태 코드 ID (FK→tb_code)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`work_order_id`),
  UNIQUE KEY `uk_wo_number` (`work_order_number`),
  KEY `idx_wo_plan_id` (`plan_id`),
  KEY `idx_wo_item_id` (`item_id`),
  KEY `idx_wo_process_id` (`process_id`),
  KEY `idx_wo_equipment_id` (`equipment_id`),
  KEY `idx_wo_status_code_id` (`status_code_id`),
  KEY `idx_wo_eqp_start` (`equipment_id`, `start_ts`),
  CONSTRAINT `fk_wo_plan` FOREIGN KEY (`plan_id`) REFERENCES `tb_production_plan`(`plan_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_wo_item` FOREIGN KEY (`item_id`) REFERENCES `tb_item`(`item_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_wo_process` FOREIGN KEY (`process_id`) REFERENCES `tb_process`(`process_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_wo_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `tb_equipment`(`equipment_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_wo_status_code` FOREIGN KEY (`status_code_id`) REFERENCES `tb_code`(`code_id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_wo_qty_nonneg` CHECK (`order_qty` >= 0 AND `produced_qty` >= 0),
  CONSTRAINT `ck_wo_time_order`
    CHECK ( (`start_ts` IS NULL OR `end_ts` IS NULL) OR (`end_ts` >= `start_ts`) )
) ENGINE=InnoDB COMMENT='작업 지시: 실작업 단위. 시간/수량/상태 무결성 보강, 설비×시간 인덱스로 조회 최적화(UTC).';


-- tb_bom (최종안)
CREATE TABLE `tb_bom` (
  `bom_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'BOM ID (PK)',
  `parent_item_id` VARCHAR(36) NOT NULL COMMENT '상위 품목 ID (FK)',
  `child_item_id` VARCHAR(36) NOT NULL COMMENT '하위 품목 ID (FK)',
  `quantity` DECIMAL(10,4) NOT NULL COMMENT '소요 수량(>0)',
  `line_no` INT NOT NULL COMMENT 'BOM 라인 번호',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`bom_id`),
  UNIQUE KEY `uk_bom_line` (`parent_item_id`, `child_item_id`, `line_no`),
  KEY `idx_bom_parent_item_id` (`parent_item_id`),
  KEY `idx_bom_child_item_id` (`child_item_id`),
  CONSTRAINT `fk_bom_parent` FOREIGN KEY (`parent_item_id`) REFERENCES `tb_item`(`item_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_bom_child` FOREIGN KEY (`child_item_id`) REFERENCES `tb_item`(`item_id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_bom_qty_pos` CHECK (`quantity` > 0)
) ENGINE=InnoDB COMMENT='BOM: 제품 구성. parent×child×line 유일, 수량>0 무결성(UTC).';


-- tb_material_lot (최종안)
CREATE TABLE `tb_material_lot` (
  `material_lot_id` VARCHAR(36) NOT NULL COMMENT '자재 LOT ID (PK, UUID)',
  `lot_number` VARCHAR(50) NOT NULL COMMENT 'LOT 번호 (유일)',
  `item_id` VARCHAR(36) NOT NULL COMMENT '품목 ID (FK)',
  `warehouse_id` VARCHAR(36) NOT NULL COMMENT '현재 창고 ID (FK)',
  `quantity` DECIMAL(10,4) NOT NULL COMMENT 'LOT 수량(>=0)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`material_lot_id`),
  UNIQUE KEY `uk_lot_number` (`lot_number`),
  KEY `idx_material_lot_item_id` (`item_id`),
  KEY `idx_material_lot_warehouse_id` (`warehouse_id`),
  KEY `idx_matlot_item_wh` (`item_id`, `warehouse_id`),
  CONSTRAINT `fk_material_lot_item` FOREIGN KEY (`item_id`) REFERENCES `tb_item`(`item_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_material_lot_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `tb_warehouse`(`warehouse_id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_matlot_qty_nonneg` CHECK (`quantity` >= 0)
) ENGINE=InnoDB COMMENT='자재 LOT: 입고 추적 단위. 수량>=0, 품목×창고 조회 최적화(UTC).';


CREATE TABLE `tb_production_performance` (
  `performance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '생산 실적 ID (PK)',
  `work_order_id` VARCHAR(36) NOT NULL COMMENT '작업 지시 ID (FK)',
  `item_id` VARCHAR(36) NOT NULL COMMENT '생산 품목 ID (FK)',
  `process_id` VARCHAR(36) NOT NULL COMMENT '실적 공정 ID (FK)',
  `equipment_id` VARCHAR(36) NOT NULL COMMENT '실적 설비 ID (FK)',
  `produced_qty` DECIMAL(10,4) NOT NULL COMMENT '생산 수량(>=0)',
  `defect_qty` DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '불량 수량(>=0, 생산 수량 이내)',
  `start_time` DATETIME NOT NULL COMMENT '작업 시작 시점 (UTC)',
  `end_time` DATETIME NOT NULL COMMENT '작업 종료 시점 (UTC)',
  `worker_id` VARCHAR(36) NULL COMMENT '작업자 ID (FK→tb_user.user_id, 선택)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`performance_id`),
  KEY `idx_perf_wo_id` (`work_order_id`),
  KEY `idx_perf_item_id` (`item_id`),
  KEY `idx_perf_process_id` (`process_id`),
  KEY `idx_perf_equipment_id` (`equipment_id`),
  KEY `idx_perf_equipment_start_time` (`equipment_id`, `start_time`),
  KEY `idx_perf_wo_start` (`work_order_id`, `start_time`),
  KEY `idx_perf_worker` (`worker_id`),
  CONSTRAINT `fk_perf_wo`        FOREIGN KEY (`work_order_id`) REFERENCES `tb_work_order`(`work_order_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_perf_item`      FOREIGN KEY (`item_id`)      REFERENCES `tb_item`(`item_id`)           ON DELETE RESTRICT,
  CONSTRAINT `fk_perf_process`   FOREIGN KEY (`process_id`)   REFERENCES `tb_process`(`process_id`)     ON DELETE RESTRICT,
  CONSTRAINT `fk_perf_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `tb_equipment`(`equipment_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_perf_user`      FOREIGN KEY (`worker_id`)    REFERENCES `tb_user`(`user_id`)           ON DELETE RESTRICT,
  CONSTRAINT `ck_perf_qty_nonneg` CHECK (`produced_qty` >= 0 AND `defect_qty` >= 0),
  CONSTRAINT `ck_perf_qty_rel`    CHECK (`defect_qty` <= `produced_qty`),
  CONSTRAINT `ck_perf_time_order` CHECK (`end_time` >= `start_time`)
) ENGINE=InnoDB COMMENT='생산 실적: 시간/수량 무결성+설비/WO 타임라인 최적화(UTC). 작업자=tb_user FK(선택).';


CREATE TABLE `tb_equipment_status_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 ID (PK)',
  `equipment_id` VARCHAR(36) NOT NULL COMMENT '설비 ID (FK)',
  `status_code_id` BIGINT NOT NULL COMMENT '설비 상태 코드 ID (FK→tb_code)',
  `reason_code_id` BIGINT NULL COMMENT '비가동 사유 코드 ID (FK→tb_code)',
  `work_order_id` VARCHAR(36) NULL COMMENT '관련 작업 지시 ID (FK) - 선택',
  `shift_id` BIGINT NULL COMMENT '관련 교대 ID (FK) - 선택',
  `start_time` DATETIME NOT NULL COMMENT '상태 시작 시점 (UTC)',
  `end_time` DATETIME NULL COMMENT '상태 종료 시점 (UTC)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`log_id`),
  KEY `idx_log_equipment_id` (`equipment_id`),
  KEY `idx_log_status_code_id` (`status_code_id`),
  KEY `idx_log_reason_code_id` (`reason_code_id`),
  KEY `idx_log_wo_id` (`work_order_id`),
  KEY `idx_log_shift_id` (`shift_id`),
  KEY `idx_log_equipment_start_time` (`equipment_id`, `start_time`),
  CONSTRAINT `fk_log_equipment`   FOREIGN KEY (`equipment_id`)   REFERENCES `tb_equipment`(`equipment_id`)   ON DELETE RESTRICT,
  CONSTRAINT `fk_log_status_code` FOREIGN KEY (`status_code_id`) REFERENCES `tb_code`(`code_id`)              ON DELETE RESTRICT,
  CONSTRAINT `fk_log_reason_code` FOREIGN KEY (`reason_code_id`) REFERENCES `tb_code`(`code_id`)              ON DELETE SET NULL,
  CONSTRAINT `fk_log_wo`          FOREIGN KEY (`work_order_id`)  REFERENCES `tb_work_order`(`work_order_id`)  ON DELETE SET NULL,
  CONSTRAINT `fk_log_shift`       FOREIGN KEY (`shift_id`)       REFERENCES `tb_shift`(`shift_id`)            ON DELETE SET NULL,
  CONSTRAINT `ck_log_time_order`  CHECK (`end_time` IS NULL OR `end_time` >= `start_time`)
) ENGINE=InnoDB COMMENT='설비 상태 로그: 설비 가동/비가동 시계열. 시간 무결성 보장, 설비×시간 최적화(UTC).';
-- Batch 7: 품질 검사 및 불량 기록
-- 역할/사용 시나리오: 생산 과정 중 발생하는 품질 검사 활동 및 불량 정보를 기록.
-- 주요 조인 키: tb_inspection.inspection_id, tb_defect.defect_id
-- 삭제 정책: 소프트 삭제(is_deleted)
-- UTC: created_at, modified_at, deleted_at, inspection_time, report_date 컬럼은 UTC 저장.

CREATE TABLE `tb_inspection` (
  `inspection_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '검사 ID (PK)',
  `work_order_id`   VARCHAR(36) NULL COMMENT '검사 대상 작업지시 ID (FK) - 선택',
  `material_lot_id` VARCHAR(36) NULL COMMENT '검사 대상 자재 LOT ID (FK) - 선택',
  `item_id` VARCHAR(36) NOT NULL COMMENT '검사 대상 품목 ID (FK)',
  `inspection_type_code_id` BIGINT NOT NULL COMMENT '검사 유형 코드 ID (FK→tb_code)',
  `inspection_time` DATETIME NOT NULL COMMENT '검사 시점 (UTC)',
  `worker_id` VARCHAR(36) NOT NULL COMMENT '검사자 ID (FK→tb_user.user_id)',
  `process_id` VARCHAR(36) NULL COMMENT '검사 공정 ID (FK) - 선택',
  `equipment_id` VARCHAR(36) NULL COMMENT '검사 설비 ID (FK) - 선택',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`inspection_id`),
  KEY `idx_inspection_wo_id` (`work_order_id`),
  KEY `idx_inspection_lot_id` (`material_lot_id`),
  KEY `idx_inspection_item_id` (`item_id`),
  KEY `idx_inspection_type_id` (`inspection_type_code_id`),
  KEY `idx_inspection_time` (`inspection_time`),
  KEY `idx_inspection_process_id` (`process_id`),
  KEY `idx_inspection_equipment_id` (`equipment_id`),
  KEY `idx_insp_worker` (`worker_id`),
  CONSTRAINT `fk_insp_wo`    FOREIGN KEY (`work_order_id`)      REFERENCES `tb_work_order`(`work_order_id`)      ON DELETE SET NULL,
  CONSTRAINT `fk_insp_lot`   FOREIGN KEY (`material_lot_id`)    REFERENCES `tb_material_lot`(`material_lot_id`)  ON DELETE SET NULL,
  CONSTRAINT `fk_insp_item`  FOREIGN KEY (`item_id`)            REFERENCES `tb_item`(`item_id`)                  ON DELETE RESTRICT,
  CONSTRAINT `fk_insp_type`  FOREIGN KEY (`inspection_type_code_id`) REFERENCES `tb_code`(`code_id`)             ON DELETE RESTRICT,
  CONSTRAINT `fk_insp_user`  FOREIGN KEY (`worker_id`)          REFERENCES `tb_user`(`user_id`)                  ON DELETE RESTRICT,
  CONSTRAINT `fk_insp_proc`  FOREIGN KEY (`process_id`)         REFERENCES `tb_process`(`process_id`)            ON DELETE RESTRICT,
  CONSTRAINT `fk_insp_eqp`   FOREIGN KEY (`equipment_id`)       REFERENCES `tb_equipment`(`equipment_id`)        ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='검사: 품목/유형/시간 인덱싱, 작업자=tb_user FK(UTC). 대상(지시/LOT/공정/설비) 선택 FK.';

-- tb_inspection_result (최종안)
CREATE TABLE `tb_inspection_result` (
  `result_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '검사 결과 ID (PK)',
  `inspection_id` BIGINT NOT NULL COMMENT '검사 ID (FK)',
  `item_id` VARCHAR(36) NOT NULL COMMENT '검사 대상 품목 ID (FK)',
  `result_value` VARCHAR(255) NOT NULL COMMENT '측정값(문자열)',
  -- 선택 확장 컬럼(리포팅/판정 편의). 필요 시 주석 해제
  -- `result_num` DECIMAL(18,6) NULL COMMENT '선택: 숫자형 측정값',
  -- `judgment_code_id` BIGINT NULL COMMENT '선택: 판정 코드 ID(FK→tb_code, 예: PASS/FAIL)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`result_id`),
  -- 동일 검사×품목 중복 방지(부분 유니크)
  UNIQUE KEY `uk_result_insp_item` (`inspection_id`, `item_id`),
  KEY `idx_result_inspection_id` (`inspection_id`),
  KEY `idx_result_item_id` (`item_id`),
  -- 선택: 판정 코드 인덱스
  -- KEY `idx_result_judgment` (`judgment_code_id`),
  CONSTRAINT `fk_result_inspection` FOREIGN KEY (`inspection_id`) REFERENCES `tb_inspection`(`inspection_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_result_item`       FOREIGN KEY (`item_id`)       REFERENCES `tb_item`(`item_id`)               ON DELETE RESTRICT
  -- 선택: 판정 코드 FK
  -- , CONSTRAINT `fk_result_judgment` FOREIGN KEY (`judgment_code_id`) REFERENCES `tb_code`(`code_id`) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='검사 결과: 동일 검사×품목 1건 보장. 숫자형/판정 컬럼은 선택 확장(UTC).';


-- tb_defect (최종안)
CREATE TABLE `tb_defect` (
  `defect_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '불량 ID (PK)',
  `inspection_id` BIGINT NOT NULL COMMENT '검사 ID (FK)',
  `defect_type_code_id` BIGINT NOT NULL COMMENT '불량 유형 코드 ID (FK→tb_code)',
  `defect_qty` DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '불량 수량(>=0)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`defect_id`),
  -- 동일 검사×불량유형 중복 방지(부분 유니크)
  UNIQUE KEY `uk_defect_insp_type` (`inspection_id`, `defect_type_code_id`),
  KEY `idx_defect_inspection_id` (`inspection_id`),
  KEY `idx_defect_type_code_id` (`defect_type_code_id`),
  CONSTRAINT `fk_defect_inspection`  FOREIGN KEY (`inspection_id`)      REFERENCES `tb_inspection`(`inspection_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_defect_type_code`   FOREIGN KEY (`defect_type_code_id`) REFERENCES `tb_code`(`code_id`)            ON DELETE RESTRICT,
  CONSTRAINT `ck_defect_qty_nonneg`  CHECK (`defect_qty` >= 0)
) ENGINE=InnoDB COMMENT='불량: 동일 검사×유형 1건 보장, 수량 비음수 무결성(UTC).';


-- tb_non_conformance (최종안)
CREATE TABLE `tb_non_conformance` (
  `non_conformance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '부적합 ID (PK)',
  `defect_id` BIGINT NOT NULL COMMENT '불량 ID (FK, 1:1)',
  `cause` TEXT NULL COMMENT '부적합 원인',
  `corrective_action` TEXT NULL COMMENT '시정 조치',
  `preventive_action` TEXT NULL COMMENT '예방 조치',
  `report_date` DATETIME NOT NULL COMMENT '보고 일시 (UTC)',
  `worker_id` VARCHAR(36) NOT NULL COMMENT '보고자/담당자 ID (FK→tb_user.user_id)',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`non_conformance_id`),
  UNIQUE KEY `uk_non_conformance_defect` (`defect_id`),
  KEY `idx_nc_report_date` (`report_date`),
  KEY `idx_nc_worker` (`worker_id`),
  CONSTRAINT `fk_nc_defect` FOREIGN KEY (`defect_id`) REFERENCES `tb_defect`(`defect_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_nc_user`   FOREIGN KEY (`worker_id`) REFERENCES `tb_user`(`user_id`)     ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='부적합: 불량 1:1 확장. 보고자=tb_user FK, 보고일 조회 최적화(UTC).';



-- Batch 8: KPI 목표 및 관리 로그
-- 역할/사용 시나리오: 생산 성능 목표를 설정하고, 시스템 활동 기록을 관리.
-- 주요 조인 키: tb_kpi_target.target_id, tb_audit_log.log_id
-- 삭제 정책: 소프트 삭제(is_deleted)
-- UTC: created_at, modified_at, deleted_at, report_date 컬럼은 UTC 저장.

-- tb_kpi_target (최종안)
CREATE TABLE `tb_kpi_target` (
  `target_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '목표 ID (PK)',
  `kpi_date` DATE NOT NULL COMMENT 'KPI 기준 날짜',
  `equipment_id` VARCHAR(36) NOT NULL COMMENT '설비 ID (FK)',
  `process_id` VARCHAR(36) NOT NULL COMMENT '공정 ID (FK)',
  `item_id` VARCHAR(36) NOT NULL COMMENT '품목 ID (FK)',
  `target_oee` DECIMAL(5,2)  NOT NULL DEFAULT 0 COMMENT 'OEE 목표치 (%) [0~100]',
  `target_productivity` DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '생산성 목표치 (단위/시간, >=0)',
  `target_yield` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '수율 목표치 (%) [0~100]',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`target_id`),
  UNIQUE KEY `uk_kpi_target_date_eqp_proc_item` (`kpi_date`, `equipment_id`, `process_id`, `item_id`),
  -- 선택: 일자 범위 검색 최적화가 필요할 때
  -- KEY `idx_kpi_date` (`kpi_date`),
  KEY `idx_kpi_target_equipment_id` (`equipment_id`),
  KEY `idx_kpi_target_process_id` (`process_id`),
  KEY `idx_kpi_target_item_id` (`item_id`),
  CONSTRAINT `fk_kpi_target_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `tb_equipment`(`equipment_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_kpi_target_process`   FOREIGN KEY (`process_id`)   REFERENCES `tb_process`(`process_id`)   ON DELETE RESTRICT,
  CONSTRAINT `fk_kpi_target_item`      FOREIGN KEY (`item_id`)      REFERENCES `tb_item`(`item_id`)         ON DELETE RESTRICT,
  CONSTRAINT `ck_kpi_oee_range`   CHECK (`target_oee` BETWEEN 0 AND 100),
  CONSTRAINT `ck_kpi_yield_range` CHECK (`target_yield` BETWEEN 0 AND 100),
  CONSTRAINT `ck_kpi_prod_nonneg` CHECK (`target_productivity` >= 0)
) ENGINE=InnoDB COMMENT='KPI 목표: 일×설비×공정×품목 축. 퍼센트/음수 가드로 무결성 보장(UTC).';


-- tb_audit_log (최종안)
CREATE TABLE `tb_audit_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 ID (PK)',
  `user_id` VARCHAR(36) NULL COMMENT '관련 사용자 ID (tb_user.user_id FK, UUID)',
  `log_type` VARCHAR(50) NOT NULL COMMENT '로그 유형 (예: CREATE, UPDATE, DELETE, LOGIN)',
  `message` TEXT NOT NULL COMMENT '로그 메시지',
  `resource_table` VARCHAR(100) NULL COMMENT '영향 테이블명',
  `resource_id` VARCHAR(255) NULL COMMENT '영향 레코드 ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '소프트삭제 플래그',
  `deleted_at` DATETIME NULL COMMENT 'UTC',
  `created_by` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'UTC',
  `modified_by` VARCHAR(50) NULL,
  `modified_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'UTC',
  PRIMARY KEY (`log_id`),
  KEY `idx_audit_log_user_id` (`user_id`),
  KEY `idx_audit_log_res` (`resource_table`, `resource_id`),
  KEY `idx_audit_created_at` (`created_at`),
  KEY `idx_audit_log_type` (`log_type`),
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user`(`user_id`) ON DELETE SET NULL,
  CONSTRAINT `ck_audit_is_deleted` CHECK (`is_deleted` IN (0,1))
) ENGINE=InnoDB COMMENT='감사 로그: 사용자/리소스/시간 기준 조회 최적화, 사용자 삭제 시 로그 보존(SET NULL, UTC).';