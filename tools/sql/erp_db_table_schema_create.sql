-- 스키마 생성
CREATE DATABASE IF NOT EXISTS erp_mvp
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE erp_mvp;

-- 공통 코드 테이블
CREATE TABLE tb_code (
  code_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  code_type        VARCHAR(50) NOT NULL,
  code             VARCHAR(50) NOT NULL,
  code_name        VARCHAR(100) NOT NULL,
  description      VARCHAR(255),
  is_active        TINYINT(1) NOT NULL DEFAULT 1,
  sort_order       INT NOT NULL DEFAULT 0,
  is_deleted       TINYINT(1) NOT NULL DEFAULT 0,
  deleted_at       DATETIME NULL,
  created_by       VARCHAR(50),
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_by      VARCHAR(50),
  modified_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version          BIGINT NOT NULL DEFAULT 0,
  UNIQUE KEY uq_tb_code_type_code (code_type, code),
  KEY idx_tb_code_type_active (code_type, is_active)
) ENGINE=InnoDB;

-- 제품 마스터 (UUID PK)
CREATE TABLE tb_product (
  product_id       CHAR(36) PRIMARY KEY,            -- UUID
  product_code     VARCHAR(50) NOT NULL,            -- 비즈니스 키
  name             VARCHAR(100) NOT NULL,
  type             VARCHAR(10) NOT NULL,            -- 'FG','SG','RM'
  unit             VARCHAR(20) NOT NULL,
  description      VARCHAR(255),
  is_deleted       TINYINT(1) NOT NULL DEFAULT 0,
  deleted_at       DATETIME NULL,
  created_by       VARCHAR(50),
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_by      VARCHAR(50),
  modified_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version          BIGINT NOT NULL DEFAULT 0,
  UNIQUE KEY uq_tb_product_code (product_code),
  KEY idx_tb_product_type (type),
  KEY idx_tb_product_is_deleted (is_deleted)
) ENGINE=InnoDB;

-- 표준단가 (UUID PK)
CREATE TABLE tb_material_cost (
  cost_id          CHAR(36) PRIMARY KEY,            -- UUID
  product_id       CHAR(36) NOT NULL,
  currency         VARCHAR(3) NOT NULL DEFAULT 'KRW',
  std_cost         DECIMAL(18,6) NOT NULL,
  effective_from   DATE NOT NULL,
  effective_to     DATE NULL,
  is_deleted       TINYINT(1) NOT NULL DEFAULT 0,
  deleted_at       DATETIME NULL,
  created_by       VARCHAR(50),
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_by      VARCHAR(50),
  modified_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version          BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_cost_product
    FOREIGN KEY (product_id) REFERENCES tb_product(product_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  UNIQUE KEY uq_cost_product_from (product_id, effective_from),
  KEY idx_cost_product_valid (product_id, effective_to),
  KEY idx_cost_is_deleted (is_deleted)
) ENGINE=InnoDB;

-- BOM 헤더 (UUID PK) - 유효기간 포함
CREATE TABLE tb_bom_header (
  bom_id           CHAR(36) PRIMARY KEY,            -- UUID
  product_id       CHAR(36) NOT NULL,               -- FG/SG
  revision         VARCHAR(20) NOT NULL DEFAULT 'R1',
  is_active        TINYINT(1) NOT NULL DEFAULT 1,
  effective_from   DATE NOT NULL,
  effective_to     DATE NULL,
  note             VARCHAR(255),
  is_deleted       TINYINT(1) NOT NULL DEFAULT 0,
  deleted_at       DATETIME NULL,
  created_by       VARCHAR(50),
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_by      VARCHAR(50),
  modified_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version          BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_bom_header_product
    FOREIGN KEY (product_id) REFERENCES tb_product(product_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  UNIQUE KEY uq_bom_product_rev (product_id, revision),
  KEY idx_bom_product_active (product_id, is_active),
  KEY idx_bom_effective (effective_from, effective_to),
  KEY idx_bom_is_deleted (is_deleted)
) ENGINE=InnoDB;

-- BOM 라인 (UUID PK) - 다단계, 부모+자재 유니크
CREATE TABLE tb_bom_line (
  bom_line_id          CHAR(36) PRIMARY KEY,        -- UUID
  bom_id               CHAR(36) NOT NULL,
  parent_line_id       CHAR(36) NULL,               -- 최상위는 NULL
  component_product_id CHAR(36) NOT NULL,
  qty                  DECIMAL(18,6) NOT NULL,
  scrap_rate           DECIMAL(9,6) NOT NULL DEFAULT 0,
  note                 VARCHAR(255),
  is_deleted           TINYINT(1) NOT NULL DEFAULT 0,
  deleted_at           DATETIME NULL,
  created_by           VARCHAR(50),
  created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_by          VARCHAR(50),
  modified_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version              BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_bom_line_header
    FOREIGN KEY (bom_id) REFERENCES tb_bom_header(bom_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_bom_line_parent
    FOREIGN KEY (parent_line_id) REFERENCES tb_bom_line(bom_line_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_bom_line_component_product
    FOREIGN KEY (component_product_id) REFERENCES tb_product(product_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  UNIQUE KEY uq_bom_parent_component (bom_id, parent_line_id, component_product_id),
  KEY idx_bom_line_bom (bom_id),
  KEY idx_bom_line_parent (parent_line_id),
  KEY idx_bom_line_component (component_product_id),
  KEY idx_bom_line_is_deleted (is_deleted)
) ENGINE=InnoDB;

-- 생산 계획 (UUID PK)
CREATE TABLE tb_production_plan (
  plan_id          CHAR(36) PRIMARY KEY,           -- UUID
  plan_code        VARCHAR(50) NOT NULL,           -- 비즈니스 키
  product_id       CHAR(36) NOT NULL,
  qty              DECIMAL(18,6) NOT NULL,
  start_date       DATE NOT NULL,
  end_date         DATE NOT NULL,
  status           VARCHAR(30) NOT NULL,           -- DRAFT, CONFIRMED, PENDING, IN_PRODUCTION, COMPLETED, CANCELED
  note             VARCHAR(255),
  is_deleted       TINYINT(1) NOT NULL DEFAULT 0,
  deleted_at       DATETIME NULL,
  created_by       VARCHAR(50),
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_by      VARCHAR(50),
  modified_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version          BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_plan_product
    FOREIGN KEY (product_id) REFERENCES tb_product(product_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  UNIQUE KEY uq_plan_code (plan_code),
  KEY idx_plan_product (product_id),
  KEY idx_plan_status (status),
  KEY idx_plan_dates (start_date, end_date),
  KEY idx_plan_is_deleted (is_deleted)
) ENGINE=InnoDB;

-- 원가 스냅샷 (UUID PK)
CREATE TABLE tb_cost_snapshot (
  snapshot_id      CHAR(36) PRIMARY KEY,           -- UUID
  plan_id          CHAR(36) NULL,
  product_id       CHAR(36) NOT NULL,
  qty              DECIMAL(18,6) NOT NULL,
  total_material   DECIMAL(18,6) NOT NULL,
  labor            DECIMAL(18,6) NOT NULL,
  overhead         DECIMAL(18,6) NOT NULL,
  total_cost       DECIMAL(18,6) NOT NULL,
  labor_rate       DECIMAL(9,6) NOT NULL,
  overhead_rate    DECIMAL(9,6) NOT NULL,
  method           VARCHAR(20) NOT NULL DEFAULT 'RATE',
  calculated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  note             VARCHAR(255),
  is_deleted       TINYINT(1) NOT NULL DEFAULT 0,
  deleted_at       DATETIME NULL,
  created_by       VARCHAR(50),
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_by      VARCHAR(50),
  modified_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version          BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_cost_snapshot_plan
    FOREIGN KEY (plan_id) REFERENCES tb_production_plan(plan_id)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT fk_cost_snapshot_product
    FOREIGN KEY (product_id) REFERENCES tb_product(product_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  KEY idx_cost_snapshot_plan (plan_id),
  KEY idx_cost_snapshot_product (product_id),
  KEY idx_cost_snapshot_calc_at (calculated_at),
  KEY idx_cost_snapshot_is_deleted (is_deleted)
) ENGINE=InnoDB;

-- 원가 스냅샷 상세 (BIGINT AI PK)
CREATE TABLE tb_cost_snapshot_detail (
  snapshot_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  snapshot_id        CHAR(36) NOT NULL,
  component_product_id CHAR(36) NOT NULL,
  level              INT NOT NULL DEFAULT 0,
  base_qty           DECIMAL(18,6) NOT NULL,
  scrap_rate         DECIMAL(9,6) NOT NULL DEFAULT 0,
  exploded_qty       DECIMAL(18,6) NOT NULL,
  unit_cost          DECIMAL(18,6) NOT NULL,
  material_cost      DECIMAL(18,6) NOT NULL,
  note               VARCHAR(255),
  CONSTRAINT fk_cost_detail_snapshot
    FOREIGN KEY (snapshot_id) REFERENCES tb_cost_snapshot(snapshot_id)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT fk_cost_detail_component_product
    FOREIGN KEY (component_product_id) REFERENCES tb_product(product_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  KEY idx_cost_detail_snapshot (snapshot_id),
  KEY idx_cost_detail_component (component_product_id)
) ENGINE=InnoDB;

-- 상태/타입 시드
INSERT INTO tb_code (code_type, code, code_name, sort_order) VALUES
('PRODUCT_TYPE','FG','완제품',10),
('PRODUCT_TYPE','SG','반제품',20),
('PRODUCT_TYPE','RM','자재',30),
('PLAN_STATUS','DRAFT','초안',10),
('PLAN_STATUS','CONFIRMED','확정',20),
('PLAN_STATUS','PENDING','대기',30),
('PLAN_STATUS','IN_PRODUCTION','생산 중',40),
('PLAN_STATUS','COMPLETED','완료',50),
('PLAN_STATUS','CANCELED','취소',60);