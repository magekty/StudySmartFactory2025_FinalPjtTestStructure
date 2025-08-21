// src/pages/performances/PerformanceCreate.tsx
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { isAxiosError } from "axios";
import { api } from "../../lib/api";
import { toUtcIso } from "../../lib/datetime";
import type { WorkOrderItem } from "../../types/workorder";
import { usePerms } from "../../hooks/usePerms";

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
  const { canWrite } = usePerms();

  // 쿼리 파라미터 바인딩 + 상세 보정
  useEffect(() => {
    const t = (s: string | null) => (s ? s.trim() : "");
    const woId = t(sp.get("woId"));
    const woNo = t(sp.get("woNumber"));
    const itm = t(sp.get("itemId"));
    const proc = t(sp.get("processId"));
    const eqp = t(sp.get("equipmentId"));
    const stat = t(sp.get("status")).toUpperCase();

    if (woId) setWo(woId);
    if (woNo) setWoNumber(woNo);
    if (itm) setItem(itm);
    if (proc) setProc(proc);
    if (eqp) setEqp(eqp);
    if (stat) setWoStatus(stat || null);

    if (!woId) return;

    api.get<WorkOrderItem>(`/work-orders/${encodeURIComponent(woId)}`)
      .then(({ data: wo }) => {
        setWoStatus((wo.status ?? stat ?? "").toUpperCase() || null);
        setItem((wo.itemId || itm).trim());
        setProc((wo.processId || proc).trim());
        setEqp((wo.equipmentId || eqp).trim());
        const base = wo.startTs ?? wo.createdAt ?? null; // 서버는 UTC ISO로 내려옴
        if (base) setWoBaselineIso(new Date(base).toISOString()); // 내부 비교는 UTC
      })
      .catch(() => {});
  }, [sp]);

  // 버튼 활성 조건
  const fieldsOk =
    workOrderId.trim().length > 0 &&
    itemId.trim().length > 0 &&
    processId.trim().length > 0 &&
    equipmentId.trim().length > 0;

  const qtyOk =
    Number.isFinite(producedQty) &&
    Number.isFinite(defectQty) &&
    producedQty >= 0 &&
    defectQty >= 0 &&
    defectQty <= producedQty;

  const stIso = toUtcIso(date, startTime);
  const etIso = toUtcIso(date, endTime);
  const stMs = new Date(stIso).getTime();
  const etMs = new Date(etIso).getTime();
  const timeOk = Number.isFinite(stMs) && Number.isFinite(etMs) && stMs <= etMs;

  const baselineOk = !woBaselineIso
    ? true
    : stMs >= new Date(woBaselineIso).getTime() && etMs >= new Date(woBaselineIso).getTime();

  const statusOk = (woStatus ?? "").toUpperCase() === "R";
  const canSave = canWrite && fieldsOk && qtyOk && timeOk && baselineOk && statusOk && !isSubmitting;

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");

    if (!canSave) {
      setErr(statusOk ? "입력값을 확인하세요." : "작업지시 상태가 R이 아닙니다.");
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
      requestId: crypto.randomUUID(), // 멱등키
    };

    try {
      setSubmitting(true);
      await api.post("/performances", dto);
      alert("등록 완료");
      nav("/performances", { replace: true });
    } catch (error: unknown) {
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

        <input className="border px-2 py-1 bg-gray-50" placeholder="지시번호" value={workOrderNumber} readOnly />
        <input className="border px-2 py-1 bg-gray-50" placeholder="작업지시ID(UUID)" value={workOrderId} readOnly required />
        <input className="border px-2 py-1 bg-gray-50" placeholder="품목ID" value={itemId} readOnly required />
        <input className="border px-2 py-1 bg-gray-50" placeholder="공정ID" value={processId} readOnly required />
        <input className="border px-2 py-1 bg-gray-50" placeholder="설비ID" value={equipmentId} readOnly required />

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
          <div>
            <label className="text-sm text-gray-600">날짜</label>
            <input className="border px-2 py-1 w-full" type="date"
                   value={date} onChange={(e) => setDate(e.target.value)} />
          </div>
          <div>
            <label className="text-sm text-gray-600">시작</label>
            <input className="border px-2 py-1 w-full" type="time"
                   value={startTime} onChange={(e) => setStart(e.target.value)} />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2">
          <div>
            <label className="text-sm text-gray-600">종료</label>
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