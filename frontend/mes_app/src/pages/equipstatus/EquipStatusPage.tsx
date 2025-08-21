import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { toPage } from "../../adapters/page";
import type { PageResult } from "../../types/api";
import type { EquipStatusItem } from "../../types/equip";
import { fetchEquipStatus, createEquipStatus } from "../../lib/equip";
import { isAxiosError } from "axios";
import CanWrite from "../../components/common/perm/CanWrite";

export default function EquipStatusPage(){
  const qc = useQueryClient();

  // 필터/폼 상태
  const [equipmentId, setEqp] = useState<string>("E-0001");
  const [date, setDate] = useState<string>(new Date().toISOString().slice(0,10)); // yyyy-MM-dd
  const [statusCode, setStatus] = useState<"RUN"|"IDLE"|"DOWN">("RUN");
  const [time, setTime] = useState<string>("09:00"); // HH:mm
  const [err, setErr] = useState<string>("");

  // 목록 로드
  const { data, isLoading, error } = useQuery<PageResult<EquipStatusItem>>({
    queryKey: ["equip-status", equipmentId, date, 0, 20],
    queryFn: async () => {
      const fromIso = new Date(`${date}T00:00:00Z`).toISOString();
      // const toIso   = new Date(`${date}T00:00:00Z`).toISOString(); // 서버에서 to 미전달 시 from~전체로 취급해도 OK
      const res = await fetchEquipStatus({
        equipmentId,
        from: fromIso, to: undefined, // 필요 시 toIso로 조정
        page: 0, size: 20, sort: "startTime,desc"
      });
      return toPage(res);
    }
  });

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    // UTC 조합 (입력 date+time → Z)
    const startIso = new Date(`${date}T${time}:00Z`).toISOString();
    try {
      await createEquipStatus({
        equipmentId,
        statusCode,
        startTimeUtc: startIso
      });
      await qc.invalidateQueries({ queryKey: ["equip-status"] });
      alert("등록 완료");
    } catch (errUnknown: unknown) {
      const msg = isAxiosError<{ message?: string }>(errUnknown)
        ? errUnknown.response?.data?.message ?? "등록 실패"
        : "등록 실패";
      setErr(msg);
    }
  }

  return (
    <div>
      <h1 className="text-lg font-semibold mb-3">설비 상태</h1>

      <form onSubmit={submit} className="flex flex-wrap items-end gap-2 mb-4">
        <div className="flex flex-col">
          <label className="text-sm text-gray-600">설비</label>
          <input className="border px-2 py-1" value={equipmentId} onChange={(e)=>setEqp(e.target.value)} />
        </div>
        <div className="flex flex-col">
          <label className="text-sm text-gray-600">날짜</label>
          <input className="border px-2 py-1" type="date" value={date} onChange={(e)=>setDate(e.target.value)} />
        </div>
        <div className="flex flex-col">
          <label className="text-sm text-gray-600">시각(UTC)</label>
          <input className="border px-2 py-1" type="time" value={time} onChange={(e)=>setTime(e.target.value)} />
        </div>
        <div className="flex flex-col">
          <label className="text-sm text-gray-600">상태</label>
          <select className="border px-2 py-1" value={statusCode} onChange={(e)=>setStatus(e.target.value as "RUN"|"IDLE"|"DOWN")}>
            <option value="RUN">RUN</option>
            <option value="IDLE">IDLE</option>
            <option value="DOWN">DOWN</option>
          </select>
        </div>
        <CanWrite>
          <button className="bg-black text-white px-3 py-2 rounded" type="submit">RUN 등록</button>
        </CanWrite>
        {err && <span className="text-red-600 ml-2">{err}</span>}
      </form>

      <div>
        <h2 className="font-semibold mb-2">최근 상태</h2>
        {isLoading ? <div>로딩...</div>
          : error ? <div>오류</div>
          : !data ? null
          : (
            <table className="w-full border">
              <thead><tr className="bg-gray-100">
                <th className="p-2">ID</th>
                <th className="p-2">설비</th>
                <th className="p-2">상태</th>
                <th className="p-2">시작(KST)</th>
                <th className="p-2">종료(KST)</th>
              </tr></thead>
              <tbody>
              {data.items.map((it: EquipStatusItem) => (
                <tr key={it.logId} className="border-t">
                  <td className="p-2">{it.logId}</td>
                  <td className="p-2">{it.equipmentId}</td>
                  <td className="p-2">{it.status ?? "-"}</td>
                  <td className="p-2">{new Date(it.startTime).toLocaleString()}</td>
                  <td className="p-2">{it.endTime ? new Date(it.endTime).toLocaleString() : "-"}</td>
                </tr>
              ))}
              </tbody>
            </table>
          )
        }
      </div>
    </div>
  );
}