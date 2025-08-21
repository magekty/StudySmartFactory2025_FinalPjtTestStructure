import { useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../../lib/api";
import { toPage } from "../../adapters/page";
import type { PageResponse, PageResult } from "../../types/api";
import type { WorkOrderItem } from "../../types/workorder";
import { Link } from "react-router-dom";
import { changeWorkOrderStatus } from "../../lib/wo";
import { isAxiosError } from "axios";
import Pagination from "../../components/common/Pagination";
import SortSelect from "../../components/common/SortSelect";
import { useState } from "react";
import { useToast } from "../../store/toast";
import CanWrite from "../../components/common/perm/CanWrite";
import GuardButton from "../../components/common/perm/GuardButton";

const sortOptions = [
  { label: "최신 생성순", value: "createdAt,desc" },
  { label: "오래된 생성순", value: "createdAt,asc" },
  { label: "지시수량↓", value: "orderQty,desc" },
  { label: "지시수량↑", value: "orderQty,asc" }
];

export default function WorkOrdersList() {
  const qc = useQueryClient();
  const [page, setPage] = useState<number>(0);
  const [size] = useState<number>(20);
  const [sort, setSort] = useState<string>("createdAt,desc");
  const [equipmentId, setEqp] = useState<string>("");
  const [status, setStatus] = useState<string>(""); // "", "P", "R", "C"
  const [from, setFrom] = useState<string>("");     // ISO yyyy-MM-dd
  const [to, setTo] = useState<string>("");

  const { data, isLoading, error } = useQuery<PageResult<WorkOrderItem>>({
    queryKey: ["work-orders", page, size, sort, equipmentId, status, from, to],
    queryFn: async () => {
      const params: Record<string,string|number> = { page, size, sort };
      if (equipmentId) params.equipmentId = equipmentId;
      if (status) params.status = status;
      // 날짜는 UTC ISO로 변환해서 전달(00:00 기준)
      if (from) params.from = new Date(`${from}T00:00:00Z`).toISOString();
      if (to)   params.to   = new Date(`${to}T23:59:59Z`).toISOString();
      const res = await api.get<PageResponse<WorkOrderItem>>("/work-orders", { params });
      return toPage(res.data);
    }
  });

  const toast = useToast();
  async function transition(id: string, to: "R" | "C") {
    if (!window.confirm(`상태를 ${to}로 변경할까요?`)) return;
    try {
      await changeWorkOrderStatus(id, { toStatus: to });
      await qc.invalidateQueries({ queryKey: ["work-orders"] });
      toast.push(`상태가 ${to}로 변경되었습니다.`, "success");
    } catch (err: unknown) {
      const msg = isAxiosError<{ message?: string }>(err)
        ? err.response?.data?.message ?? "상태 변경 실패"
        : "상태 변경 실패";
      toast.push(msg, "error");
    }
  }

  return (
    <div>
        // 렌더 내 검색바 추가
      <div className="flex flex-wrap gap-2 mb-2">
        <input className="border px-2 py-1" placeholder="설비ID"
              value={equipmentId} onChange={(e)=>{ setPage(0); setEqp(e.target.value); }} />
        <select className="border px-2 py-1" value={status}
                onChange={(e)=>{ setPage(0); setStatus(e.target.value); }}>
          <option value="">상태(전체)</option>
          <option value="P">P</option>
          <option value="R">R</option>
          <option value="C">C</option>
        </select>
        <input className="border px-2 py-1" type="date"
              value={from} onChange={(e)=>{ setPage(0); setFrom(e.target.value); }} />
        <input className="border px-2 py-1" type="date"
              value={to} onChange={(e)=>{ setPage(0); setTo(e.target.value); }} />
      </div>
      <div className="flex items-center justify-between mb-3">
        <h1 className="text-lg font-semibold">작업지시</h1>
        <div className="flex items-center gap-2">
          <SortSelect value={sort} options={sortOptions} onChange={(v) => { setPage(0); setSort(v); }} />
          <CanWrite>
            <Link className="border px-3 py-1 rounded" to="/work-orders/new">+ 새 지시</Link>
          </CanWrite>
        </div>
      </div>

      {isLoading && <div>로딩...</div>}
      {error && <div>오류</div>}
      {data && (
        <>
          <table className="w-full border">
            <thead>
              <tr className="bg-gray-100">
                <th className="p-2 text-left">번호</th>
                <th className="p-2 text-left">품목</th>
                <th className="p-2 text-left">설비</th>
                <th className="p-2 text-right">지시</th>
                <th className="p-2 text-right">누적</th>
                <th className="p-2 text-left">상태</th>
                <th className="p-2 text-center">액션</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((it: WorkOrderItem) => {
                const canToR = it.status === "P";
                const canToC = it.status === "R";
                return (
                  <tr key={it.workOrderId} className="border-t">
                    <td className="p-2">{it.workOrderNumber}</td>
                    <td className="p-2">{it.itemId}</td>
                    <td className="p-2">{it.equipmentId}</td>
                    <td className="p-2 text-right">{it.orderQty}</td>
                    <td className="p-2 text-right">{it.producedQty}</td>
                    <td className="p-2">{it.status ?? "-"}</td>

                    <td className="p-2 text-center space-x-2">
                      <div className="flex gap-2 justify-center">
                        <GuardButton require="write"
                          className={`px-2 py-1 rounded ${canToR ? "bg-blue-600 text-white" : "bg-gray-300 text-gray-600"}`}
                          onClick={() => canToR && transition(it.workOrderId, "R")}
                          renderDisabled={!canToR}
                        >
                          P→R
                        </GuardButton>

                        <GuardButton require="write"
                          className={`px-2 py-1 rounded ${canToC ? "bg-green-600 text-white" : "bg-gray-300 text-gray-600"}`}
                          onClick={() => canToC && transition(it.workOrderId, "C")}
                          renderDisabled={!canToC}
                        >
                          R→C
                        </GuardButton>

                        {/* 실적 등록 링크(쓰기 권한일 때만 노출) */}
                        {it.status === "R" && (
                        <CanWrite>
                          <Link
                            className="px-2 py-1 rounded border"
                            to={`/performances/new?woId=${it.workOrderId}&woNumber=${it.workOrderNumber}&itemId=${it.itemId}&processId=${it.processId}&equipmentId=${it.equipmentId}&status=${it.status ?? ""}`}
                          >
                            실적 등록
                          </Link>
                        </CanWrite>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
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