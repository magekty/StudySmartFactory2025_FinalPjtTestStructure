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

USE erp_mvp;

-- 자재(RM) 시드
INSERT INTO tb_product (product_id, product_code, name, type, unit, description, created_at, modified_at, version)
VALUES
(UUID(), 'RM-0001', '알루미늄 판재 1T',      'RM', 'KG', '알루미늄 시트 1.0T',         UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0002', '스테인리스 볼트 M6',    'RM', 'EA', 'M6x20 볼트 SUS304',         UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0003', '구리 동선 2.0SQ',       'RM', 'M',  '전선/도체 2.0SQ',           UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0004', '실리콘 오일',           'RM', 'KG', '윤활/코팅용',               UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0005', '폴리카보네이트 수지',    'RM', 'KG', '사출용 PC 수지',            UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0006', '포장 박스(소)',         'RM', 'EA', '골판지 소형 박스',           UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0007', '라벨 스티커',           'RM', 'EA', '제품 식별 라벨',            UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0008', '주석-은 솔더 페이스트',  'RM', 'KG', 'Sn-Ag 솔더 페이스트',       UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0009', 'PCB 기판(4L, FR-4)',    'RM', 'EA', '4 Layer FR-4',              UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0010', 'SMT 저항 10k 0603',     'RM', 'EA', 'SMD Res 10k 1% 0603',       UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0011', 'SMT 커패시터 100nF 0603','RM', 'EA', 'SMD Cap 100nF 16V 0603',   UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0012', '마이크로컨트롤러(MCU)',  'RM', 'EA', 'ARM Cortex-M 시리즈',        UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0013', '전원 어댑터 24V 2A',     'RM', 'EA', '어댑터 24V/2A',            UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0014', '케이블 하네스 A',        'RM', 'EA', '하네스 세트 A',             UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'RM-0015', '히트싱크 알루미늄',      'RM', 'EA', '방열판',                   UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0);

-- 반제품(SG) 시드
INSERT INTO tb_product (product_id, product_code, name, type, unit, description, created_at, modified_at, version)
VALUES
(UUID(), 'SG-0001', '전원 모듈 서브어셈블리', 'SG', 'EA', 'SMPS+하네스 세트',          UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'SG-0002', '제어 보드 서브어셈블리', 'SG', 'EA', 'MCU/수동소자 실장 보드',    UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'SG-0003', '방열 서브어셈블리',      'SG', 'EA', '히트싱크+볼트 체결',        UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0);

-- 참고용 완제품(FG) 몇 개 (BOM 연결 테스트용)
INSERT INTO tb_product (product_id, product_code, name, type, unit, description, created_at, modified_at, version)
VALUES
(UUID(), 'FG-1001', '완제품-A', 'FG', 'EA', '테스트 완제품 A', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0),
(UUID(), 'FG-1002', '완제품-B', 'FG', 'EA', '테스트 완제품 B', UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0);

USE erp_mvp;

-- RM 단가
INSERT INTO tb_material_cost (cost_id, product_id, currency, std_cost, effective_from, created_at, modified_at, version)
SELECT UUID(), p.product_id, 'KRW', t.std_cost, CURRENT_DATE, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_product p
JOIN (
    SELECT 'RM-0001' code,  9000.000000 std_cost UNION ALL
    SELECT 'RM-0002' code,   120.000000 std_cost UNION ALL
    SELECT 'RM-0003' code,   850.000000 std_cost UNION ALL
    SELECT 'RM-0004' code, 12000.000000 std_cost UNION ALL
    SELECT 'RM-0005' code,  6800.000000 std_cost UNION ALL
    SELECT 'RM-0006' code,   450.000000 std_cost UNION ALL
    SELECT 'RM-0007' code,    50.000000 std_cost UNION ALL
    SELECT 'RM-0008' code, 58000.000000 std_cost UNION ALL
    SELECT 'RM-0009' code, 15000.000000 std_cost UNION ALL
    SELECT 'RM-0010' code,     8.000000 std_cost UNION ALL
    SELECT 'RM-0011' code,     9.000000 std_cost UNION ALL
    SELECT 'RM-0012' code, 78000.000000 std_cost UNION ALL
    SELECT 'RM-0013' code, 22000.000000 std_cost UNION ALL
    SELECT 'RM-0014' code,  3500.000000 std_cost UNION ALL
    SELECT 'RM-0015' code,  1800.000000 std_cost
) t ON p.product_code = t.code;

-- SG 단가(반제품 표준단가; 조립/공정비가 포함된 기준가라고 가정)
INSERT INTO tb_material_cost (cost_id, product_id, currency, std_cost, effective_from, created_at, modified_at, version)
SELECT UUID(), p.product_id, 'KRW', t.std_cost, CURRENT_DATE, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_product p
JOIN (
    SELECT 'SG-0001' code, 35000.000000 std_cost UNION ALL
    SELECT 'SG-0002' code, 42000.000000 std_cost UNION ALL
    SELECT 'SG-0003' code, 18000.000000 std_cost
) t ON p.product_code = t.code;

-- FG 단가(참고용: 표준원가 목표치 등으로 세팅, 실제 계산 검증시 비교용)
INSERT INTO tb_material_cost (cost_id, product_id, currency, std_cost, effective_from, created_at, modified_at, version)
SELECT UUID(), p.product_id, 'KRW', t.std_cost, CURRENT_DATE, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_product p
JOIN (
    SELECT 'FG-1001' code, 120000.000000 std_cost UNION ALL
    SELECT 'FG-1002' code, 145000.000000 std_cost
) t ON p.product_code = t.code;

-- FG-1001용 BOM 헤더
INSERT INTO tb_bom_header (bom_id, product_id, revision, is_active, effective_from, created_at, modified_at, version)
SELECT UUID(), p.product_id, 'R1', 1, CURRENT_DATE, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_product p WHERE p.product_code='FG-1001';

-- 상위 BOM ID 획득을 위해 변수 사용 (MySQL 클라이언트에서 지원 시)
-- 아래 SELECT는 환경에 맞게 한 번 확인
-- SELECT h.bom_id FROM tb_bom_header h JOIN tb_product p ON p.product_id=h.product_id WHERE p.product_code='FG-1001' AND h.revision='R1';

-- SG-0002(제어 보드) BOM
INSERT INTO tb_bom_header (bom_id, product_id, revision, is_active, effective_from, created_at, modified_at, version)
SELECT UUID(), p.product_id, 'R1', 1, CURRENT_DATE, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_product p WHERE p.product_code='SG-0002';

-- SG-0003(방열 서브) BOM
INSERT INTO tb_bom_header (bom_id, product_id, revision, is_active, effective_from, created_at, modified_at, version)
SELECT UUID(), p.product_id, 'R1', 1, CURRENT_DATE, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_product p WHERE p.product_code='SG-0003';

-- SG-0002 라인: PCB 1EA, 저항 20EA, 커패 10EA, MCU 1EA, 솔더 페이스트 0.01KG
INSERT INTO tb_bom_line (bom_line_id, bom_id, parent_line_id, component_product_id, qty, scrap_rate, created_at, modified_at, version)
SELECT UUID(), h.bom_id, NULL, c.product_id, q.qty, q.scrap, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_bom_header h
JOIN (
    SELECT 'RM-0009' code, 1.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'RM-0010' code, 20.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'RM-0011' code, 10.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'RM-0012' code, 1.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'RM-0008' code, 0.010000 qty, 0.000000 scrap
) q
JOIN tb_product c ON c.product_code=q.code
JOIN tb_product p ON p.product_code='SG-0002'
WHERE h.product_id = p.product_id AND h.revision='R1';

-- SG-0003 라인: 히트싱크 1EA, 볼트 4EA, 스크랩율 2% 예시는 볼트에만 적용
INSERT INTO tb_bom_line (bom_line_id, bom_id, parent_line_id, component_product_id, qty, scrap_rate, created_at, modified_at, version)
SELECT UUID(), h.bom_id, NULL, c.product_id, q.qty, q.scrap, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_bom_header h
JOIN (
    SELECT 'RM-0015' code, 1.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'RM-0002' code, 4.000000 qty, 0.020000 scrap
) q
JOIN tb_product c ON c.product_code=q.code
JOIN tb_product p ON p.product_code='SG-0003'
WHERE h.product_id = p.product_id AND h.revision='R1';

-- FG-1001 라인: SG-0001 1EA, SG-0002 1EA, SG-0003 1EA, 포장박스 1EA, 라벨 1EA
INSERT INTO tb_bom_line (bom_line_id, bom_id, parent_line_id, component_product_id, qty, scrap_rate, created_at, modified_at, version)
SELECT UUID(), h.bom_id, NULL, c.product_id, q.qty, q.scrap, UTC_TIMESTAMP(), UTC_TIMESTAMP(), 0
FROM tb_bom_header h
JOIN (
    SELECT 'SG-0001' code, 1.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'SG-0002' code, 1.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'SG-0003' code, 1.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'RM-0006' code, 1.000000 qty, 0.000000 scrap UNION ALL
    SELECT 'RM-0007' code, 1.000000 qty, 0.000000 scrap
) q
JOIN tb_product c ON c.product_code=q.code
JOIN tb_product p ON p.product_code='FG-1001'
WHERE h.product_id = p.product_id AND h.revision='R1';

