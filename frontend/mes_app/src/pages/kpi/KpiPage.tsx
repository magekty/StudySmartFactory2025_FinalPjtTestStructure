import { useState } from "react";
import { api } from "../../lib/api";
import { getErrorMessage } from "../../lib/error";

type KpiRes = {
  kpiDate: string;
  equipmentId: string;
  targetOee?: number;
  targetProductivity?: number;
  targetYield?: number;
  actualOutput?: number;
  actualYield?: number | null;
};

export default function KpiPage() {
  const [date, setDate] = useState<string>(new Date().toISOString().slice(0, 10)); // yyyy-MM-dd
  const [eqp, setEqp] = useState<string>("E-0001");
  const [data, setData] = useState<KpiRes | null>(null);
  const [err, setErr] = useState<string>("");

  const load = async () => {
    try {
      setErr("");
      const res = await api.get<KpiRes>("/kpi/actuals", { params: { kpiDate: date, equipmentId: eqp } });
      setData(res.data);
    } catch (err: unknown) {
      setData(null);
      setErr(getErrorMessage(err, "조회 실패"));
    }
  };

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
        <div className="grid grid-cols-2 gap-3">
          <div className="border p-3 rounded">목표 OEE: {data.targetOee ?? "-"}</div>
          <div className="border p-3 rounded">목표 생산성: {data.targetProductivity ?? "-"}</div>
          <div className="border p-3 rounded">목표 수율: {data.targetYield ?? "-"}</div>
          <div className="border p-3 rounded">실제 출력: {data.actualOutput ?? "-"}</div>
          <div className="border p-3 rounded">실제 수율: {data.actualYield ?? "-"}</div>
        </div>
      )}
    </div>
  );
}