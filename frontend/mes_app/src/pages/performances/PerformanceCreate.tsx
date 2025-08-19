import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { isAxiosError } from "axios";
import { createPerformance } from "../../lib/perf";
import { toUtcIso, parseServerDate } from "../../lib/datetime";
import { api } from "../../lib/api";

type WoDetail = {
  workOrderId: string;
  workOrderNumber: string;
  itemId: string;
  processId: string;
  equipmentId: string;
  orderQty: number;
  producedQty: number;
  status: string | null; // "P"|"R"|"C"|null
  startTs?: string | null; // ISO(UTC) 가정 
  createdAt?: string | null; 
};

export default function PerformanceCreate() {
  const [sp] = useSearchParams();
  const [workOrderId, setWo] = useState("");
  const [workOrderNumber, setWoNumber] = useState("");
  const [itemId, setItem] = useState("");
  const [processId, setProc] = useState("");
  const [equipmentId, setEqp] = useState("");
  const [woStatus, setWoStatus] = useState<string | null>(null);
  const [woBaselineIso, setWoBaselineIso] = useState<string | null>(null);

  const [producedQty, setProduced] = useState<number>(100);
  const [defectQty, setDefect] = useState<number>(5);
  const [date, setDate] = useState<string>(new Date().toISOString().slice(0, 10));
  const [startTime, setStart] = useState<string>("09:00");
  const [endTime, setEnd] = useState<string>("09:30");

  const [err, setErr] = useState<string>("");
  const [isSubmitting, setSubmitting] = useState(false);
  const nav = useNavigate();

  // 쿼리 파라미터 바인딩 + 서버에서 최신 지시 정보/상태 가져오기(R 가드에 사용)
  useEffect(() => {
    const t = (s: string | null) => (s ? s.trim() : "");

    const woId = t(sp.get("woId"));
    const woNo = t(sp.get("woNumber"));
    const itm  = t(sp.get("itemId"));
    const proc = t(sp.get("processId"));
    const eqp  = t(sp.get("equipmentId"));
    const stat = t(sp.get("status")).toUpperCase(); // 'P'|'R'|'C' or ""

    

    if (woId) setWo(woId);
    if (woNo) setWoNumber(woNo);
    if (itm)  setItem(itm);
    if (proc) setProc(proc);
    if (eqp)  setEqp(eqp);
    if (stat) setWoStatus(stat);

    if (!woId) return;

    // 서버 상세로 최신값 보정
    api.get<WoDetail>(`/work-orders/${encodeURIComponent(woId)}`)
      .then(({ data: wo }) => {
        // 상태 보정(서버 우선)
        const statusCode = (wo.status ?? stat ?? "").toUpperCase();
        setWoStatus(statusCode || null);

        // 식별자 보정(서버 우선, 없으면 쿼리값)
        setItem((wo.itemId || itm).trim());
        setProc((wo.processId || proc).trim());
        setEqp((wo.equipmentId || eqp).trim());

        // 기준 시각(지시 시작이 우선, 없으면 생성 시각)
        const baseDate = parseServerDate(wo.startTs ?? wo.createdAt);
        if (baseDate) setWoBaselineIso(baseDate.toISOString()); // 내부 비교는 UTC 기준으로
        
      })
      .catch(() => { /* 조회 실패면 쿼리 파라미터 기준으로만 진행 */ });
  }, [sp]); // sp는 useSearchParams()로 얻은 객체

              // ISO(UTC)로 내려오게

const stIso = toUtcIso(date, startTime);
const etIso = toUtcIso(date, endTime);
const stMs = new Date(stIso).getTime();
const etMs = new Date(etIso).getTime();
const baseMs = woBaselineIso ? new Date(woBaselineIso).getTime() : undefined;

const fieldsOk = workOrderId.trim() && itemId.trim() && processId.trim() && equipmentId.trim();
const qtyOk = Number.isFinite(producedQty) && Number.isFinite(defectQty)
           && producedQty >= 0 && defectQty >= 0 && defectQty <= producedQty;
const timeOk = Number.isFinite(stMs) && Number.isFinite(etMs) && stMs <= etMs;
const baselineOk = baseMs === undefined ? true : (stMs >= baseMs && etMs >= baseMs);
const statusOk = (woStatus ?? "").toUpperCase() === "R";

const canSave = !!fieldsOk && qtyOk && timeOk && baselineOk && statusOk && !isSubmitting;

  

  async function submit(e: React.FormEvent) {
  e.preventDefault();
  setErr("");

  if (!canSave) { // canSave 계산을 위에서 먼저 하므로 간단히 체크
    setErr(woStatus !== "R" ? "작업지시 상태가 R이 아닙니다." : "입력값을 확인하세요.");
    return;
  }

  const dto = {
    workOrderId: workOrderId.trim(),
    itemId: itemId.trim(),
    processId: processId.trim(),
    equipmentId: equipmentId.trim(),
    producedQty,
    defectQty,
    startTime: stIso, // UTC
    endTime: etIso,   // UTC
    // requestId: crypto.randomUUID() // 멱등키 적용 시
  };

  try {
    
    setSubmitting(true);
    await createPerformance(dto); // 한 번만 호출
    alert("등록 완료");
    nav("/performances", { replace: true });
    } catch (error) {
      const msg = isAxiosError<{ message?: string }>(error)
        ? error.response?.data?.message ?? "등록 실패"
        : "등록 실패";
      setErr(msg);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h1 className="text-lg font-semibold mb-3">실적 등록</h1>
      <form onSubmit={submit} className="grid gap-3 max-w-md">
        {err && <div className="text-red-600">{err}</div>}

        <input className="border px-2 py-1 bg-gray-50" placeholder="지시번호"
              defaultValue={workOrderNumber} readOnly />
        <input className="border px-2 py-1 bg-gray-50" placeholder="작업지시ID(UUID)" 
               defaultValue={workOrderId} hidden required />
        <input className="border px-2 py-1 bg-gray-50" placeholder="품목ID"
               defaultValue={itemId} readOnly required />
        <input className="border px-2 py-1 bg-gray-50" placeholder="공정ID"
               defaultValue={processId} readOnly required />
        <input className="border px-2 py-1 bg-gray-50" placeholder="설비ID"
               defaultValue={equipmentId} readOnly required />

        <div className="text-sm text-gray-600">
          지시 상태: <b>{woStatus ?? "-"}</b> {woStatus !== "R" && "(R 상태에서만 등록 가능)"}
        </div>

        <div className="grid grid-cols-2 gap-2">
          <div>
            <label className="text-sm text-gray-600">생산 수량</label>
            <input className="border px-2 py-1 w-full" type="number" min={0}
                   value={producedQty} onChange={(e) => setProduced(Number(e.target.value))} required />
          </div>
          <div>
            <label className="text-sm text-gray-600">불량 수량</label>
            <input className="border px-2 py-1 w-full" type="number" min={0}
                   value={defectQty} onChange={(e) => setDefect(Number(e.target.value))} required />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2">
          {woBaselineIso && (
            <div className="text-xs text-gray-500">
              기준 시각(지시 시작/생성, KST): {new Date(woBaselineIso).toLocaleString("ko-KR", { timeZone: "Asia/Seoul" })}
            </div>
          )}
          <div>
            <label className="text-sm text-gray-600">날짜(KST 기준)</label>
            <input className="border px-2 py-1 w-full" type="date"
                   value={date} onChange={(e) => setDate(e.target.value)} />
          </div>
          <div>
            <label className="text-sm text-gray-600">시작(KST)</label>
            <input className="border px-2 py-1 w-full" type="time"
                   value={startTime} onChange={(e) => setStart(e.target.value)} />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2">
          <div>
            <label className="text-sm text-gray-600">종료(KST)</label>
            <input className="border px-2 py-1 w-full" type="time"
                   value={endTime} onChange={(e) => setEnd(e.target.value)} />
          </div>
          <div className="flex items-end">
          <button
            className={`px-3 py-2 rounded ${canSave ? "bg-black text-white" : "bg-gray-300 text-gray-600"}`}
            disabled={!canSave}
          >
            {isSubmitting ? "저장 중..." : "저장"}
          </button>
          </div>
        </div>
      </form>
    </div>
  );
}