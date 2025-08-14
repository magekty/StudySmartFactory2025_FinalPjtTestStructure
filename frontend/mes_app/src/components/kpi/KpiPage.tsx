import { useState } from "react";
import { api } from "../../lib/api";
import type { KpiRes } from "../../types/kpi";
import KpiCard from "../../components/kpi/KpiCard";
import { statusColor, fmtPercent, fmtNumber } from "../../lib/kpi";
import { isAxiosError } from "axios";

export default function KpiPage() {
  const [date, setDate] = useState<string>(new Date().toISOString().slice(0, 10)); // yyyy-MM-dd
  const [eqp, setEqp] = useState<string>("E-0001");
  const [data, setData] = useState<KpiRes | null>(null);
  const [err, setErr] = useState<string>("");

  async function load() {
    try {
      setErr("");
      const res = await api.get<KpiRes>("/kpi/actuals", { params: { kpiDate: date, equipmentId: eqp }});
      setData(res.data);
    } catch (error: unknown) {
      const msg = isAxiosError<{ message?: string }>(error)
        ? error.response?.data?.message ?? "조회 실패"
        : "조회 실패";
      setErr(msg);
      setData(null);
    }
  }

  const actualOutput = data?.actualOutput ?? null; // 생산량(개/수치)
  const targetProd   = data?.targetProductivity ?? null; // 목표 생산성(참고)
  const actualYield  = data?.actualYield ?? null; // %
  const targetYield  = data?.targetYield ?? null; // %
  const targetOee    = data?.targetOee ?? null;   // %

  return (
    <div>
      <h1 className="text-lg font-semibold mb-3">KPI</h1>

      <div className="flex gap-2 mb-3">
        <input className="border px-2 py-1" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        <input className="border px-2 py-1" placeholder="설비ID" value={eqp} onChange={(e) => setEqp(e.target.value)} />
        <button className="bg-black text-white px-3 py-1 rounded" onClick={load}>조회</button>
      </div>

      {err && <div className="text-red-600 mb-2">{err}</div>}

      {data && (
        <div className="grid md:grid-cols-3 gap-3">
          {/* 1) 실제 출력 vs 목표 생산성(참고 비교) */}
          <KpiCard
            title="출력"
            targetLabel="목표 생산성"
            target={targetProd != null ? fmtNumber(targetProd, 0) : undefined}
            actualLabel=""
            actual={actualOutput != null ? fmtNumber(actualOutput, 0) : "-"}
            status={statusColor(targetProd, actualOutput)}
          />

          {/* 2) 실제 수율 vs 목표 수율 */}
          <KpiCard
            title="수율"
            targetLabel="목표 수율"
            target={targetYield != null ? fmtPercent(targetYield) : undefined}
            actualLabel=""
            actual={actualYield != null ? fmtPercent(actualYield) : "-"}
            status={statusColor(targetYield, actualYield)}
          />

          {/* 3) 목표 OEE (참고 지표, 비교 없음) */}
          <KpiCard
            title="목표 OEE(참고)"
            targetLabel=""
            target={undefined}
            actual={targetOee != null ? fmtPercent(targetOee) : "-"}
            actualLabel="가동률은 후속 확장 예정"
            status={"none"}
          />
        </div>
      )}
    </div>
  );
}