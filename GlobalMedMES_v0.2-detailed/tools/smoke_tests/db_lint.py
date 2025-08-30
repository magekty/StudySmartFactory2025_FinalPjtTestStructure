# db_lint.py
# 목적: 배포 전 DB 품질 자동 검증(무결성·성능·일관성)과 리포트 생성
# 체크: FK 인덱스/타입, 필수 인덱스·CHECK, CHECK↔FK 충돌, 구간겹침, 수량·시간 규칙, 고아 worker, 비활성 코드
# 정책: critical/major 발견 시 실패, XOR/이메일 NULL은 예외; 결과는 .md로 저장
import os
import re
from datetime import datetime, timezone
from collections import defaultdict
from sqlalchemy import create_engine, text

# 1) DB 연결 정보(여기만 채우면 됨)
DB = dict(
    user     = "root",
    password = "1121",
    host     = "127.0.0.1",
    port     = 3306,
    database = "mes_pjt_test"
)
DSN = f"mysql+mysqlconnector://{DB['user']}:{DB['password']}@{DB['host']}:{DB['port']}/{DB['database']}?charset=utf8mb4"

# 2) 리포트 파일 경로
REPORT_PATH = "output/db_lint_report.md"

# 3) Fail 수준(critical, major, minor 중 선택)
FAIL_ON = {"critical", "major"}  # 둘 중 하나라도 있으면 종료코드 != 0

# 4) 린트 설정
CONFIG = {
    # 필수 인덱스(컬럼 순서 기준, 이름은 무시)
    "required_indexes": {
        "TB_PRODUCTION_PERFORMANCE": [
            ["equipment_id", "start_time"],
            ["work_order_id", "start_time"]
        ],
        "TB_EQUIPMENT_STATUS_LOG": [
            ["equipment_id", "start_time"]
        ],
        "TB_SHIFT_CALENDAR": [
            ["shift_date", "shift_id", "equipment_id"],
            ["shift_date", "shift_id", "workcenter_id"]
        ],
        "TB_MENU": [
            ["parent_id", "sort_order"]
        ],
    },
    # 필수 CHECK(존재만 검증; 이름 기준)
    "required_checks": {
        "TB_PRODUCTION_PLAN": ["ck_plan_dates", "ck_plan_status", "ck_plan_qty_nonneg"],
        "TB_WORK_ORDER": ["ck_wo_qty_nonneg", "ck_wo_time_order"],
        "TB_BOM": ["ck_bom_qty_pos"],
        "TB_MATERIAL_LOT": ["ck_matlot_qty_nonneg"],
        "TB_PRODUCTION_PERFORMANCE": ["ck_perf_qty_nonneg", "ck_perf_qty_rel", "ck_perf_time_order"],
        "TB_EQUIPMENT_STATUS_LOG": ["ck_log_time_order"],
        "TB_SHIFT_CALENDAR": ["ck_shiftcal_scope_exclusive", "ck_shiftcal_time_order"],
        "TB_SHIFT_ASSIGNMENT": ["ck_assign_scope_exclusive", "ck_assign_time_order"],
        "TB_ITEM": ["ck_item_type"],
        "TB_KPI_TARGET": ["ck_kpi_oee_range", "ck_kpi_yield_range", "ck_kpi_prod_nonneg"],
        "TB_ROLE": ["ck_role_is_deleted"],
        "TB_USER": ["ck_user_is_active", "ck_user_is_deleted", "ck_user_failed_cnt"],
        "TB_USER_ROLE": ["ck_userrole_is_deleted"],
        "TB_MENU": ["ck_menu_flags"],  # self-parent CHECK는 의도적으로 제거
        "TB_ROLE_MENU": ["ck_rm_flags"],
        "TB_AUDIT_LOG": ["ck_audit_is_deleted"],
    },
    # NULL 포함 유니크 허용 화이트리스트(테이블.인덱스)
    "null_unique_whitelist": {
        "TB_SHIFT_CALENDAR.UK_SHIFTCAL_EQP",
        "TB_SHIFT_CALENDAR.UK_SHIFTCAL_WC",
        "TB_SHIFT_ASSIGNMENT.UK_ASSIGN_EQP",
        "TB_SHIFT_ASSIGNMENT.UK_ASSIGN_WC",
        "TB_USER.UK_USER_EMAIL",
    },
    # CHECK에 쓰인 컬럼이 FK에서 SET NULL/CASCADE이면 충돌로 간주
    "fail_on_check_fk_conflict": True,
    # 구간 겹침 검사 대상: (table, group_cols, start_col, end_col)
    "interval_overlap_rules": [
        ("TB_EQUIPMENT_STATUS_LOG", ["equipment_id"], "start_time", "end_time"),
        ("TB_SHIFT_ASSIGNMENT", ["shift_date", "shift_id", "worker_id",
                                 "COALESCE(equipment_id, workcenter_id)"], "start_ts", "end_ts"),
        ("TB_PRODUCTION_PERFORMANCE", ["work_order_id"], "start_time", "end_time"),
    ],
    # 코드 활성( use_yn = 'Y') 강제 대상 (table, fk_col)
    "active_code_rules": [
        ("TB_EQUIPMENT", "status_code_id"),
        ("TB_WORK_ORDER", "status_code_id"),
        ("TB_INSPECTION", "inspection_type_code_id"),
        ("TB_DEFECT", "defect_type_code_id"),
        ("TB_EQUIPMENT_STATUS_LOG", "status_code_id"),
        ("TB_EQUIPMENT_STATUS_LOG", "reason_code_id"),
    ],
    # worker FK 강제 대상
    "worker_fk_tables": [
        ("TB_SHIFT_ASSIGNMENT", "worker_id"),
        ("TB_PRODUCTION_PERFORMANCE", "worker_id"),
        ("TB_INSPECTION", "worker_id"),
        ("TB_NON_CONFORMANCE", "worker_id"),
    ],
    # 선택 인덱스(권고)
    "optional_indexes": {
        "TB_KPI_TARGET": [["kpi_date"]],
    },
    # 실패 수준 분류
    "levels": {
        "critical": {
            "FK_INDEX_MISSING", "FK_TYPE_MISMATCH",
            "REQ_INDEX_MISSING", "REQ_CHECK_MISSING",
            "CHECK_FK_CONFLICT",
            "INTERVAL_OVERLAP", "TIME_ORDER_VIOLATION", "QTY_RULE_VIOLATION",
            "ORPHAN_WORKER", "INACTIVE_CODE_REF"
        },
        "major": {"NULL_UNIQUE_SUSPECT", "MENU_SELF_PARENT"},
        "minor": {"OPTIONAL_INDEX_MISSING"}
    }
}

# ---------- 유틸 ----------

def md_header():
    t = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
    return f"# DB Lint Report\n\n- Generated: {t}\n"

def fetchall(conn, q):
    return conn.execute(text(q)).mappings().all()

def qualify_expr(expr: str, alias: str) -> str:
    """
    COALESCE(equipment_id, workcenter_id) 같은 표현식 내부의 '컬럼'에만 테이블 별칭을 붙인다.
    함수/키워드는 건드리지 않음.
    """
    tokens = re.findall(r"[A-Za-z_][A-Za-z0-9_]*|\W", expr)
    keywords = {"COALESCE", "IFNULL", "NULL", "AND", "OR", "IS", "NOT"}
    out = []
    for t in tokens:
        if re.match(r"^[A-Za-z_][A-Za-z0-9_]*$", t) and t.upper() not in keywords:
            out.append(f"{alias}.{t}")
        else:
            out.append(t)
    return "".join(out)

def build_group_predicates(groups):
    """
    groups 예: ["shift_date","shift_id","worker_id","COALESCE(equipment_id, workcenter_id)"]
    → a.shift_date=b.shift_date AND ... AND COALESCE(a.equipment_id,a.workcenter_id)=COALESCE(b.equipment_id,b.workcenter_id)
    """
    preds = []
    for g in groups:
        gstr = str(g)
        if "COALESCE" in gstr.upper() or "IFNULL" in gstr.upper():
            left = qualify_expr(gstr, "a")
            right = qualify_expr(gstr, "b")
            preds.append(f"{left} = {right}")
        else:
            preds.append(f"a.{gstr} = b.{gstr}")
    return " AND ".join(preds)

def overlap_count(conn, tbl, groups, s_col, e_col, pk):
    """
    시간 구간 겹침 카운트. pk는 테이블별 PK 컬럼명.
    """
    group_expr = build_group_predicates(groups)
    q = f"""
        SELECT COUNT(*) AS cnt
        FROM {tbl} a
        JOIN {tbl} b
          ON {group_expr}
         AND a.{pk} < b.{pk}
         AND a.{s_col} < COALESCE(b.{e_col}, '2999-12-31 23:59:59')
         AND b.{s_col} < COALESCE(a.{e_col}, '2999-12-31 23:59:59')
    """
    return conn.execute(text(q)).scalar()

# ---------- 메인 ----------

def main():
    engine = create_engine(DSN)
    findings = []
    report = [md_header()]

    with engine.connect() as c:
        # 1) FK 인덱스 누락
        miss_fk_idx = fetchall(c, """
        SELECT rc.CONSTRAINT_NAME, rc.TABLE_NAME, kcu.COLUMN_NAME
        FROM information_schema.REFERENTIAL_CONSTRAINTS rc
        JOIN information_schema.KEY_COLUMN_USAGE kcu
          ON rc.CONSTRAINT_SCHEMA=kcu.CONSTRAINT_SCHEMA
         AND rc.TABLE_NAME=kcu.TABLE_NAME
         AND rc.CONSTRAINT_NAME=kcu.CONSTRAINT_NAME
        LEFT JOIN information_schema.STATISTICS s
          ON s.TABLE_SCHEMA=kcu.TABLE_SCHEMA
         AND s.TABLE_NAME=kcu.TABLE_NAME
         AND s.COLUMN_NAME=kcu.COLUMN_NAME
        WHERE rc.CONSTRAINT_SCHEMA = DATABASE()
          AND s.COLUMN_NAME IS NULL;
        """)
        for r in miss_fk_idx:
            findings.append(dict(level="critical", code="FK_INDEX_MISSING",
                title=f"[{r['TABLE_NAME']}] FK 인덱스 누락",
                detail=f"constraint={r['CONSTRAINT_NAME']}, column={r['COLUMN_NAME']}", sample=""))

        # 2) FK 타입/길이/콜레이션 불일치
        mismatch = fetchall(c, """
        SELECT k.TABLE_NAME, k.COLUMN_NAME, k.REFERENCED_TABLE_NAME ref_table, k.REFERENCED_COLUMN_NAME ref_col,
               c.DATA_TYPE col_type, c.CHARACTER_MAXIMUM_LENGTH col_len, c.COLLATION_NAME col_coll,
               rc.DATA_TYPE ref_type, rc.CHARACTER_MAXIMUM_LENGTH ref_len, rc.COLLATION_NAME ref_coll
        FROM information_schema.KEY_COLUMN_USAGE k
        JOIN information_schema.COLUMNS c
          ON c.TABLE_SCHEMA=k.TABLE_SCHEMA AND c.TABLE_NAME=k.TABLE_NAME AND c.COLUMN_NAME=k.COLUMN_NAME
        JOIN information_schema.COLUMNS rc
          ON rc.TABLE_SCHEMA=k.TABLE_SCHEMA AND rc.TABLE_NAME=k.REFERENCED_TABLE_NAME AND rc.COLUMN_NAME=k.REFERENCED_COLUMN_NAME
        WHERE k.TABLE_SCHEMA=DATABASE() AND k.REFERENCED_TABLE_NAME IS NOT NULL
          AND (c.DATA_TYPE<>rc.DATA_TYPE
            OR COALESCE(c.CHARACTER_MAXIMUM_LENGTH,0)<>COALESCE(rc.CHARACTER_MAXIMUM_LENGTH,0)
            OR COALESCE(c.COLLATION_NAME,'')<>COALESCE(rc.COLLATION_NAME,''));
        """)
        for r in mismatch:
            findings.append(dict(level="critical", code="FK_TYPE_MISMATCH",
                title=f"[{r['TABLE_NAME']}] FK 타입/길이/콜레이션 불일치",
                detail=f"{r['TABLE_NAME']}.{r['COLUMN_NAME']} ({r['col_type']},{r['col_len']},{r['col_coll']}) != "
                       f"{r['ref_table']}.{r['ref_col']} ({r['ref_type']},{r['ref_len']},{r['ref_coll']})",
                sample=""))

        # 3) 필수 인덱스 존재(컬럼 순서 기준)
        idx_rows = fetchall(c, """
        SELECT TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX, COLUMN_NAME
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA=DATABASE();
        """)
        by_table = defaultdict(lambda: defaultdict(list))
        for r in idx_rows:
            t = r["TABLE_NAME"].upper()
            n = r["INDEX_NAME"].upper()
            by_table[t][n].append((r["SEQ_IN_INDEX"], r["COLUMN_NAME"]))
        # 정렬 후 컬럼 시퀀스만 보관
        for t in by_table:
            for n in by_table[t]:
                by_table[t][n] = [col for _, col in sorted(by_table[t][n])]
        for t, reqs in CONFIG["required_indexes"].items():
            cols_list = list(by_table.get(t.upper(), {}).values())
            for req in reqs:
                if not any(cols[:len(req)] == req for cols in cols_list):
                    findings.append(dict(level="critical", code="REQ_INDEX_MISSING",
                        title=f"[{t}] 필수 인덱스 누락",
                        detail=f"required prefix={req}", sample=str(cols_list)))

        # 선택 인덱스(권고)
        for t, reqs in CONFIG["optional_indexes"].items():
            cols_list = list(by_table.get(t, {}).values())
            for req in reqs:
                if not any(cols[:len(req)] == req for cols in cols_list):
                    findings.append(dict(level="minor", code="OPTIONAL_INDEX_MISSING",
                        title=f"[{t}] 선택 인덱스 제안",
                        detail=f"recommended prefix={req}", sample=""))

        # 4) 필수 CHECK 존재
        chk_rows = fetchall(c, """
        SELECT UPPER(tc.TABLE_NAME) AS TABLE_NAME, UPPER(tc.CONSTRAINT_NAME) AS CONSTRAINT_NAME
        FROM information_schema.TABLE_CONSTRAINTS tc
        WHERE tc.CONSTRAINT_SCHEMA=DATABASE() AND tc.CONSTRAINT_TYPE='CHECK';
        """)
        chk_map = defaultdict(set)
        for r in chk_rows:
            chk_map[r["TABLE_NAME"]].add(r["CONSTRAINT_NAME"])

        # 비교
        for t, req_names in CONFIG["required_checks"].items():
            have = chk_map.get(t.upper(), set())
            miss = [x for x in req_names if x.upper() not in have]
            if miss:
                findings.append(dict(level="critical", code="REQ_CHECK_MISSING",
                    title=f"[{t}] 필수 CHECK 누락", detail=f"missing={miss}", sample=f"have={list(have)}"))

        # 5) CHECK↔FK(SET NULL/CASCADE) 충돌
        if CONFIG["fail_on_check_fk_conflict"]:
            ck_detail = fetchall(c, """
            SELECT tc.TABLE_NAME, tc.CONSTRAINT_NAME, cc.CHECK_CLAUSE
            FROM information_schema.TABLE_CONSTRAINTS tc
            JOIN information_schema.CHECK_CONSTRAINTS cc
              ON tc.CONSTRAINT_SCHEMA=cc.CONSTRAINT_SCHEMA AND tc.CONSTRAINT_NAME=cc.CONSTRAINT_NAME
            WHERE tc.CONSTRAINT_SCHEMA=DATABASE() AND tc.CONSTRAINT_TYPE='CHECK';
            """)
            fk_rule = fetchall(c, """
            SELECT CONSTRAINT_NAME, TABLE_NAME, DELETE_RULE
            FROM information_schema.REFERENTIAL_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA=DATABASE();
            """)
            rule_by_fk = {(r["TABLE_NAME"], r["CONSTRAINT_NAME"]): r["DELETE_RULE"] for r in fk_rule}
            fk_cols = fetchall(c, """
            SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL;
            """)
            cols_by_fk = defaultdict(list)
            for r in fk_cols:
                cols_by_fk[(r["TABLE_NAME"], r["CONSTRAINT_NAME"])].append(r["COLUMN_NAME"])

            for ck in ck_detail:
                tbl, cname, clause = ck["TABLE_NAME"], ck["CONSTRAINT_NAME"], ck["CHECK_CLAUSE"]
                for (t2, fkname), cols in cols_by_fk.items():
                    if t2 != tbl:
                        continue
                    rule = rule_by_fk.get((t2, fkname), "RESTRICT")
                    if rule in ("SET NULL","CASCADE") and any((f"`{col}`" in clause) or (col in clause) for col in cols):
                        findings.append(dict(level="critical", code="CHECK_FK_CONFLICT",
                            title=f"[{tbl}] CHECK와 FK 삭제 규칙 충돌",
                            detail=f"check={cname}, fk={fkname}, rule={rule}, cols={cols}", sample=clause))
                        break

        # sus 처리부 직전에 화이트리스트를 전부 upper()로 표준화
        CONFIG["null_unique_whitelist"] = {s.upper() for s in CONFIG["null_unique_whitelist"]}
        
        # 6) NULL 포함 유니크(화이트리스트 제외)
        sus = fetchall(c, """
        SELECT UPPER(s.TABLE_NAME) AS TABLE_NAME, UPPER(s.INDEX_NAME) AS INDEX_NAME,
                GROUP_CONCAT(s.COLUMN_NAME ORDER BY s.SEQ_IN_INDEX) AS cols
        FROM information_schema.STATISTICS s
        JOIN information_schema.COLUMNS c
            ON c.TABLE_SCHEMA = s.TABLE_SCHEMA
        AND c.TABLE_NAME   = s.TABLE_NAME
        AND c.COLUMN_NAME  = s.COLUMN_NAME
        WHERE s.TABLE_SCHEMA = DATABASE()
            AND s.NON_UNIQUE = 0
        GROUP BY s.TABLE_NAME, s.INDEX_NAME
        HAVING SUM(CASE WHEN c.IS_NULLABLE = 'YES' THEN 1 ELSE 0 END) > 0;
        """)
        for r in sus:
            key = f"{r['TABLE_NAME']}.{r['INDEX_NAME']}"  # 이미 UPPER
            if key not in CONFIG["null_unique_whitelist"]:
                findings.append(dict(level="major", code="NULL_UNIQUE_SUSPECT",
                    title=f"[{r['TABLE_NAME']}] NULL 포함 유니크",
                    detail=f"index={r['INDEX_NAME']}, cols={r['cols']}", sample=""))

        # 7) 구간 겹침
        pk_map = {
            "TB_EQUIPMENT_STATUS_LOG": "log_id",
            "TB_SHIFT_ASSIGNMENT": "assignment_id",
            "TB_PRODUCTION_PERFORMANCE": "performance_id",
        }
        for tbl, groups, s_col, e_col in CONFIG["interval_overlap_rules"]:
            cnt = overlap_count(c, tbl, groups, s_col, e_col, pk_map.get(tbl, "id"))
            if cnt and cnt > 0:
                findings.append(dict(level="critical", code="INTERVAL_OVERLAP",
                    title=f"[{tbl}] 시간 구간 겹침", detail=f"count={cnt}", sample=""))

        # 8) 수량/시간 위반(추가 방어)
        cnt = c.execute(text("""
            SELECT COUNT(*) FROM TB_PRODUCTION_PERFORMANCE
            WHERE produced_qty < 0 OR defect_qty < 0 OR defect_qty > produced_qty
               OR end_time < start_time
        """)).scalar()
        if cnt and cnt>0:
            findings.append(dict(level="critical", code="QTY_RULE_VIOLATION",
                title="[TB_PRODUCTION_PERFORMANCE] 수량/시간 규칙 위반", detail=f"count={cnt}", sample=""))

        cnt = c.execute(text("""
            SELECT COUNT(*) FROM TB_EQUIPMENT_STATUS_LOG
            WHERE end_time IS NOT NULL AND end_time < start_time
        """)).scalar()
        if cnt and cnt>0:
            findings.append(dict(level="critical", code="TIME_ORDER_VIOLATION",
                title="[TB_EQUIPMENT_STATUS_LOG] 시간 역전", detail=f"count={cnt}", sample=""))

        cnt = c.execute(text("""
            SELECT COUNT(*) FROM TB_SHIFT_ASSIGNMENT
            WHERE end_ts <= start_ts
        """)).scalar()
        if cnt and cnt>0:
            findings.append(dict(level="critical", code="TIME_ORDER_VIOLATION",
                title="[TB_SHIFT_ASSIGNMENT] 시간 역전/동일", detail=f"count={cnt}", sample=""))

        # 9) 고아 worker
        for tbl, col in CONFIG["worker_fk_tables"]:
            cnt = c.execute(text(f"""
                SELECT COUNT(*) FROM {tbl} t
                LEFT JOIN TB_USER u ON u.user_id = t.{col}
                WHERE t.{col} IS NOT NULL AND u.user_id IS NULL
            """)).scalar()
            if cnt and cnt>0:
                findings.append(dict(level="critical", code="ORPHAN_WORKER",
                    title=f"[{tbl}] 고아 worker 참조", detail=f"{col}, count={cnt}", sample=""))

        # 10) 비활성 코드 참조
        for tbl, col in CONFIG["active_code_rules"]:
            cnt = c.execute(text(f"""
                SELECT COUNT(*) FROM {tbl} t
                LEFT JOIN TB_CODE c ON c.code_id = t.{col}
                WHERE t.{col} IS NOT NULL AND c.use_yn <> 'Y'
            """)).scalar()
            if cnt and cnt>0:
                findings.append(dict(level="critical", code="INACTIVE_CODE_REF",
                    title=f"[{tbl}] 비활성 코드 참조", detail=f"{col}, count={cnt}", sample=""))

        # 11) 메뉴 self-parent(데이터 검사)
        cnt = c.execute(text("SELECT COUNT(*) FROM TB_MENU WHERE parent_id = menu_id")).scalar()
        if cnt and cnt>0:
            findings.append(dict(level="major", code="MENU_SELF_PARENT",
                title="[TB_MENU] self-parent 행 발견", detail=f"count={cnt}", sample=""))

        # 12) EXPLAIN 샘플(정보 목적)
        try:
            exp = fetchall(c, "EXPLAIN SELECT * FROM TB_KPI_TARGET WHERE kpi_date BETWEEN CURDATE()-INTERVAL 7 DAY AND CURDATE()")
            report.append("## EXPLAIN (KPI 최근 7일)\n```\n" + "\n".join([str(dict(r)) for r in exp]) + "\n```\n")
        except Exception as e:
            report.append(f"## EXPLAIN (KPI 최근 7일)\n실패: {e}\n")

    # 리포트 작성
    report.append("## Summary\n")
    by_level = defaultdict(list)
    for f in findings:
        by_level[f["level"]].append(f)
    for lv in ["critical","major","minor"]:
        report.append(f"- {lv}: {len(by_level.get(lv,[]))}")

    report.append("\n## Details\n")
    for lv in ["critical","major","minor"]:
        for f in by_level.get(lv, []):
            report.append(f"### [{lv.upper()}] {f['code']} - {f['title']}\n")
            if f.get("detail"): report.append(f"- detail: {f['detail']}")
            if f.get("sample"): report.append(f"- sample: {f['sample']}")
            report.append("")

    # 파일 출력
    if os.path.dirname(REPORT_PATH):
        os.makedirs(os.path.dirname(REPORT_PATH), exist_ok=True)
    with open(REPORT_PATH, "w", encoding="utf-8") as fp:
        fp.write("\n".join(report))

    # 종료 코드
    exit_code = 0
    if by_level.get("critical") and "critical" in FAIL_ON:
        exit_code = 2
    elif by_level.get("major") and "major" in FAIL_ON:
        exit_code = 1

    print(f"[DONE] report => {REPORT_PATH} | critical={len(by_level.get('critical',[]))} major={len(by_level.get('major',[]))} minor={len(by_level.get('minor',[]))}")
    raise SystemExit(exit_code)

if __name__ == "__main__":
    main()