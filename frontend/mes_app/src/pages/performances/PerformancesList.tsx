import { useQuery } from "@tanstack/react-query";
import { api } from "../../lib/api";
import { toPage } from "../../adapters/page";
import type { PageResponse, PageResult } from "../../types/api";
import Pagination from "../../components/common/Pagination";
import SortSelect from "../../components/common/SortSelect";
import { useState } from "react";
// import { Link } from "react-router-dom";
// import CanWrite from "../../components/common/perm/CanWrite";

type PerfItem = {
  performanceId: number;
  workOrderId: string; itemId: string; processId: string; equipmentId: string;
  producedQty: number; defectQty: number; startTime: string; endTime: string;
};

const sortOptions = [
  { label: "시작시각↓", value: "startTime,desc" },
  { label: "시작시각↑", value: "startTime,asc" },
  { label: "생산량↓", value: "producedQty,desc" },
  { label: "생산량↑", value: "producedQty,asc" }
];

export default function PerformancesList(){
  const [page, setPage] = useState<number>(0);
  const [size] = useState<number>(20);
  const [sort, setSort] = useState<string>("startTime,desc");
  const [equipmentId, setEqp] = useState<string>("");
  const [from, setFrom] = useState<string>("");
  const [to, setTo] = useState<string>("");

  const { data, isLoading, error } = useQuery<PageResult<PerfItem>>({
  queryKey: ["performances", page, size, sort, equipmentId, from, to],
  queryFn: async () => {
    const params: Record<string,string|number> = { page, size, sort };
    if (equipmentId) params.equipmentId = equipmentId;
    if (from) params.from = new Date(`${from}T00:00:00Z`).toISOString();
    if (to)   params.to   = new Date(`${to}T23:59:59Z`).toISOString();
    const res = await api.get<PageResponse<PerfItem>>("/performances", { params });
    return toPage(res.data);
  }
  });

  return (
    <div>
      // 렌더 내 검색바
      <div className="flex flex-wrap gap-2 mb-2">
        <input className="border px-2 py-1" placeholder="설비ID"
              value={equipmentId} onChange={(e)=>{ setPage(0); setEqp(e.target.value); }} />
        <input className="border px-2 py-1" type="date"
              value={from} onChange={(e)=>{ setPage(0); setFrom(e.target.value); }} />
        <input className="border px-2 py-1" type="date"
              value={to} onChange={(e)=>{ setPage(0); setTo(e.target.value); }} />
      </div>
      <div className="flex items-center justify-between mb-3">
        <h1 className="text-lg font-semibold">실적</h1>
        <div className="flex items-center gap-2">
          <SortSelect value={sort} options={sortOptions} onChange={(v)=>{ setPage(0); setSort(v); }} />
          {/* <CanWrite>
            <Link className="border px-3 py-1 rounded" to="/performances/new">+ 새 1실적</Link>
          </CanWrite> */}
        </div>
      </div>

      {isLoading && <div>로딩...</div>}
      {error && <div>오류</div>}
      {data && (
        <>
          <table className="w-full border">
            <thead>
              <tr className="bg-gray-100">
                <th className="p-2">WO</th><th className="p-2">품목</th><th className="p-2">설비</th>
                <th className="p-2 text-right">생산</th><th className="p-2 text-right">불량</th><th className="p-2">시작</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((it: PerfItem) => (
                <tr key={it.performanceId} className="border-t">
                  <td className="p-2">{it.workOrderId}</td>
                  <td className="p-2">{it.itemId}</td>
                  <td className="p-2">{it.equipmentId}</td>
                  <td className="p-2 text-right">{it.producedQty}</td>
                  <td className="p-2 text-right">{it.defectQty}</td>
                  <td className="p-2">{new Date(it.startTime).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <Pagination
            page={page}
            size={size}
            total={data.total}
            onPageChange={(p) => setPage(p)}
          />
        </>
      )}
    </div>
  );
}