// smoke.mjs (Node 18+ ESM, DB 조회 기반 finalBaseline 계산 + 전체 출력 강화)
// 실행 예(PS):
// $env:API_BASE="http://localhost:8080"
// $env:VIEWER_TOKEN="eyJhbGciOi..."   # viewer JWT
// $env:OP_TOKEN="eyJhbGciOi..."       # op JWT
// $env:DB_HOST="127.0.0.1"
// $env:DB_PORT="3306"
// $env:DB_NAME="globalmed"
// $env:DB_USER="root"
// $env:DB_PASSWORD="pass"
// node .\smoke.mjs

import { randomUUID } from 'node:crypto';

// fetch 폴리필(노드 18 미만 대비)
if (typeof fetch === 'undefined') {
  const { default: nf } = await import('node-fetch');
  globalThis.fetch = nf;
}

// mysql2/promise 로드
import mysql from 'mysql2/promise';

// 환경
const BASE = process.env.API_BASE ?? "http://localhost:8080";
const viewerToken = process.env.VIEWER_TOKEN ?? "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwVlciLCJyb2xlcyI6WyJST0xFX1ZJRVdFUiJdLCJpYXQiOjE3NTU4Mzc1MjMsImV4cCI6MTc1NTgzOTMyM30.nQH3yM5kWw9vTuJivvZooWmepW2yna_qstPmhVB0ZBk";
const opToken     = process.env.OP_TOKEN     ?? "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwT1AiLCJyb2xlcyI6WyJST0xFX09QIl0sImlhdCI6MTc1NTgzNzUzMiwiZXhwIjoxNzU1ODM5MzMyfQ.-IsAODJ9Jfx29IsUOFWE7k2HFj_fDQGeZx1XUN_BJxU";

const DB_HOST = process.env.DB_HOST ?? "127.0.0.1";
const DB_PORT = Number(process.env.DB_PORT ?? "3307");
const DB_NAME = process.env.DB_NAME ?? "globalmed";
const DB_USER = process.env.DB_USER ?? "mes_user";
const DB_PASSWORD = process.env.DB_PASSWORD ?? "mes_pwd";

// 테스트용 WO
const WO_R_NORMAL   = process.env.WO_R_NORMAL   ?? "77478b91-22ef-4a21-9a32-22532980ba6a"; // R, 정상 등록
const WO_P_INVALID  = process.env.WO_P_INVALID  ?? "11111111-1111-1111-1111-111111111111"; // P, 상태오류
const WO_R_BASELINE = process.env.WO_R_BASELINE ?? "22222222-2222-2222-2222-222222222222"; // R, baseline 위반

// 공통 필드
const itemId = "I-0001";
const processId = "P-0001";
const equipmentId = "E-0001";

// 로깅 유틸
const hr = () => console.log("----------------------------------------------------------------");
function logCase(title) { hr(); console.log(`CASE: ${title}`); }
function logReq(body, role = "") {
  console.log(`→ Request(${role}):`, {
    workOrderId: body.workOrderId,
    startTime: body.startTime,
    endTime: body.endTime,
    producedQty: body.producedQty,
    defectQty: body.defectQty,
    requestId: body.requestId
  });
}
function logResp(res) {
  console.log(`← Response: status=${res.status} body=`, res.body);
}
function logFinalBaseline(tag, baseIso, lastEndIso, floorIso) {
  console.log(`• ${tag} baseline: ${baseIso} | lastPerfEnd: ${lastEndIso ?? "-"} | finalBaseline: ${floorIso}`);
}

// 어서션 유틸
function expectStatus(actual, expected, ctx) {
  if (actual !== expected) throw new Error(`${ctx} → expected ${expected}, got ${actual}`);
}
function expectErr(body, expected, ctx) {
  const code = body?.code;
  const msg  = body?.message;
  if (code !== expected && msg !== expected) {
    throw new Error(`${ctx} → expected ${expected}, got code=${code}, msg=${msg}`);
  }
}

// 시간 유틸
function toIsoZ(ms) {
  return new Date(ms).toISOString().replace(/\.\d{3}Z$/, "Z");
}
function msFromAny(isoLike) {
  if (!isoLike) return NaN;
  const ms = Date.parse(String(isoLike).replace(' ', 'T').replace(/Z?$/, 'Z'));
  return Number.isFinite(ms) ? ms : NaN;
}
function msFromDateOrString(v) {
  if (!v) return NaN;
  if (v instanceof Date) return v.getTime();
  return msFromAny(v);
}

// HTTP 유틸
async function get(url, token) {
  const res = await fetch(`${BASE}${url}`, {
    method: "GET",
    headers: {
      "Accept": "application/json",
      ...(token ? { "Authorization": `Bearer ${token}` } : {}),
    },
  });
  const text = await res.text();
  let json; try { json = text ? JSON.parse(text) : null; } catch { json = { raw: text }; }
  return { status: res.status, body: json };
}
async function post(url, token, body) {
  const res = await fetch(`${BASE}${url}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Accept": "application/json",
      ...(token ? { "Authorization": `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  });
  const text = await res.text();
  let json; try { json = text ? JSON.parse(text) : null; } catch { json = { raw: text }; }
  return { status: res.status, body: json };
}

// DB 커넥션
async function getConn() {
  return mysql.createConnection({
    host: DB_HOST,
    port: DB_PORT,
    user: DB_USER,
    password: DB_PASSWORD,
    database: DB_NAME,
    timezone: 'Z', // UTC
    dateStrings: false // DATETIME을 JS Date로 받고 싶으면 false
  });
}

// DB에서 기준선 계산: finalBaseline = max(wo.start_ts ?? wo.created_at, lastPerfEnd)
async function computeFinalBaselineDb(woId) {
  const conn = await getConn();
  try {
    // 1) 지시 기준선
    const [rows1] = await conn.execute(
      "SELECT start_ts, created_at FROM tb_work_order WHERE work_order_id=?",
      [woId]
    );
    if (!rows1 || rows1.length === 0) throw new Error(`WO not found: ${woId}`);
    const r1 = rows1[0];
    const baseMs = msFromDateOrString(r1.start_ts) || msFromDateOrString(r1.created_at);
    if (!Number.isFinite(baseMs)) throw new Error(`WO baseline parse fail: ${r1.start_ts} / ${r1.created_at}`);
    const baseIso = toIsoZ(baseMs);

    // 2) 마지막 실적 종료시각(없으면 null)
    const [rows2] = await conn.execute(
      "SELECT MAX(COALESCE(end_time, start_time)) AS last_ts FROM tb_production_performance WHERE work_order_id=?",
      [woId]
    );
    const lastTs = rows2 && rows2[0] ? rows2[0].last_ts : null;
    const lastEndMs = msFromDateOrString(lastTs);
    const lastEndIso = Number.isFinite(lastEndMs) ? toIsoZ(lastEndMs) : null;

    // 3) finalBaseline
    const floorMs = Math.max(baseMs, Number.isFinite(lastEndMs) ? lastEndMs : baseMs);
    const floorIso = toIsoZ(floorMs);

    logFinalBaseline(woId, baseIso, lastEndIso, floorIso);
    return { floorMs, floorIso, baseMs, baseIso, lastEndMs, lastEndIso };
  } finally {
    await conn.end().catch(() => {});
  }
}

async function run() {
  console.log("API_BASE =", BASE);
  console.log("DB =", { DB_HOST, DB_PORT, DB_NAME, DB_USER });
  console.log("WO_R_NORMAL   =", WO_R_NORMAL);
  console.log("WO_P_INVALID  =", WO_P_INVALID);
  console.log("WO_R_BASELINE =", WO_R_BASELINE);
  hr();

  const results = [];
  let fails = 0;

  // 1) viewer 403 (권한 차단)
  try {
    logCase("1) viewer FORBIDDEN (403)");
    const { floorMs } = await computeFinalBaselineDb(WO_R_NORMAL);
    const stMs = floorMs + 60 * 60 * 1000;
    const etMs = stMs  + 30 * 60 * 1000;

    const body = {
      workOrderId: WO_R_NORMAL,
      itemId, processId, equipmentId,
      producedQty: 2, defectQty: 0,
      startTime: toIsoZ(stMs),
      endTime:   toIsoZ(etMs),
      requestId: randomUUID(),
    };
    logReq(body, "viewer");
    const r = await post("/performances", viewerToken, body);
    logResp(r);
    expectStatus(r.status, 403, "viewer FORBIDDEN");
    results.push("1) viewer 403 OK");
  } catch (e) {
    results.push("1) viewer 403 FAIL: " + e.message);
    fails++;
  }

  // 2) op 201 정상 등록 — finalBaseline + 5분 (DB 기준)
  let requestIdForDup = randomUUID();
  try {
    logCase("2) op CREATE (201)");
    const { floorMs } = await computeFinalBaselineDb(WO_R_NORMAL);
    const stMs = floorMs + 5 * 60 * 1000;   // 마지막 실적 종료 직후 + 5분
    const etMs = stMs  + 30 * 60 * 1000;    // +30분
    console.log("• Using start/end:", new Date(stMs).toISOString(), new Date(etMs).toISOString());

    const body = {
      workOrderId: WO_R_NORMAL,
      itemId, processId, equipmentId,
      producedQty: 2, defectQty: 0,
      startTime: toIsoZ(stMs),
      endTime:   toIsoZ(etMs),
      requestId: requestIdForDup,
    };
    logReq(body, "op");
    const r = await post("/performances", opToken, body);
    logResp(r);
    expectStatus(r.status, 201, "op create");
    results.push("2) op 201 OK");
  } catch (e) {
    results.push("2) op 201 FAIL: " + e.message);
    fails++;
  }

  // 3) DUPLICATE_KEY — 동일 requestId 재전송 (동일 st/et)
  try {
    logCase("3) DUPLICATE_KEY (409 or code=DUPLICATE_KEY)");
    const { floorMs } = await computeFinalBaselineDb(WO_R_NORMAL);
    const stMs = floorMs + 5 * 60 * 1000;
    const etMs = stMs  + 30 * 60 * 1000;
    const body = {
      workOrderId: WO_R_NORMAL,
      itemId, processId, equipmentId,
      producedQty: 2, defectQty: 0,
      startTime: toIsoZ(stMs),
      endTime:   toIsoZ(etMs),
      requestId: requestIdForDup,
    };
    logReq(body, "op");
    const r = await post("/performances", opToken, body);
    logResp(r);
    expectErr(r.body, "DUPLICATE_KEY", "duplicate");
    results.push("3) DUPLICATE_KEY OK");
  } catch (e) {
    results.push("3) DUPLICATE_KEY FAIL: " + e.message);
    fails++;
  }

  // 4) TIME_ORDER_INVALID (end < start) — finalBaseline 이후 구간에서 역전
  try {
    logCase("4) TIME_ORDER_INVALID (400)");
    const { floorMs } = await computeFinalBaselineDb(WO_R_NORMAL);
    const stMs = floorMs + 2 * 60 * 60 * 1000;
    const etMs = stMs  - 10 * 60 * 1000; // 역전
    const body = {
      workOrderId: WO_R_NORMAL,
      itemId, processId, equipmentId,
      producedQty: 1, defectQty: 0,
      startTime: toIsoZ(stMs),
      endTime:   toIsoZ(etMs),
      requestId: randomUUID(),
    };
    logReq(body, "op");
    const r = await post("/performances", opToken, body);
    logResp(r);
    expectStatus(r.status, 400, "time invalid status");
    expectErr(r.body, "TIME_ORDER_INVALID", "time invalid code");
    results.push("4) TIME_ORDER_INVALID OK");
  } catch (e) {
    results.push("4) TIME_ORDER_INVALID FAIL: " + e.message);
    fails++;
  }

  // 5) WO_STATUS_INVALID (지시 상태 P)
  try {
    logCase("5) WO_STATUS_INVALID (400)");
    const now = Date.now();
    const stMs = now + 60 * 60 * 1000;
    const etMs = stMs + 10 * 60 * 1000;
    const body = {
      workOrderId: WO_P_INVALID, // P 상태
      itemId, processId, equipmentId,
      producedQty: 1, defectQty: 0,
      startTime: toIsoZ(stMs),
      endTime:   toIsoZ(etMs),
      requestId: randomUUID(),
    };
    logReq(body, "op");
    const r = await post("/performances", opToken, body);
    logResp(r);
    expectStatus(r.status, 400, "wo status invalid status");
    expectErr(r.body, "WO_STATUS_INVALID", "wo status invalid code");
    results.push("5) WO_STATUS_INVALID OK");
  } catch (e) {
    results.push("5) WO_STATUS_INVALID FAIL: " + e.message);
    fails++;
  }

  // 6) PERF_BEFORE_WO (finalBaseline 이전)
  try {
    logCase("6) PERF_BEFORE_WO (400)");
    const { floorMs } = await computeFinalBaselineDb(WO_R_BASELINE);
    const stMs = floorMs - 10 * 60 * 1000;
    const etMs = floorMs - 5  * 60 * 1000;
    const body = {
      workOrderId: WO_R_BASELINE,
      itemId, processId, equipmentId,
      producedQty: 1, defectQty: 0,
      startTime: toIsoZ(stMs),
      endTime:   toIsoZ(etMs),
      requestId: randomUUID(),
    };
    logReq(body, "op");
    const r = await post("/performances", opToken, body);
    logResp(r);
    expectStatus(r.status, 400, "perf before wo status");
    expectErr(r.body, "PERF_BEFORE_WO", "perf before wo code");
    results.push("6) PERF_BEFORE_WO OK");
  } catch (e) {
    results.push("6) PERF_BEFORE_WO FAIL: " + e.message);
    fails++;
  }

  hr();
  console.log("=== Smoke Result ===");
  results.forEach((l) => console.log(l));
  if (fails > 0) {
    console.error(`FAILED: ${fails} case(s)`);
    process.exit(1);
  } else {
    console.log("ALL PASS");
    process.exit(0);
  }
}

run().catch((e) => {
  console.error("Smoke fatal:", e);
  process.exit(2);
});