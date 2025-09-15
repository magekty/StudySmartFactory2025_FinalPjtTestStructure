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

