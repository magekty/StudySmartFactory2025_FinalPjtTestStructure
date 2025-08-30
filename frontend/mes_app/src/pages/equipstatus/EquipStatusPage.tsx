// src/pages/equip/EquipStatusPage.tsx
import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { toPage } from "../../adapters/page";
import type { PageResult } from "../../types/api";
import type { EquipStatusItem } from "../../types/equip";
import { fetchEquipStatus, createEquipStatus } from "../../lib/equip";
import { isAxiosError } from "axios";
import CanWrite from "../../components/common/perm/CanWrite";

export default function EquipStatusPage() {
  const qc = useQueryClient();

  // 필터 상태
  const [equipmentId, setEqp] = useState<string>("E-0001");
  const today = new Date().toISOString().slice(0, 10); // yyyy-MM-dd (UTC 기준 잘라 사용)
  const [date, setDate] = useState<string>(today);     // From(시작 날짜)
  const [toDate, setToDate] = useState<string>(today); // To(끝 날짜)

  // 생성 폼 상태
  const [statusCode, setStatus] = useState<"RUN" | "IDLE" | "DOWN">("RUN");
  const [time, setTime] = useState<string>("09:00"); // HH:mm
  const [err, setErr] = useState<string>("");

  // 목록 로드 (from/to 둘 다 적용)
  const { data, isLoading, error } = useQuery<PageResult<EquipStatusItem>>({
    queryKey: ["equip-status", equipmentId, date, toDate, 0, 20],
    queryFn: async () => {
      // from: 해당 날짜 00:00:00Z
      const fromMs = Date.parse(`${date}T00:00:00Z`);
      // to: 끝 날짜 23:59:59Z (끝 날짜가 시작보다 빠르면 시작 날짜로 보정)
      const rawToMs = Date.parse(`${toDate}T23:59:59Z`);
      const toMs = Number.isFinite(rawToMs)
        ? Math.max(fromMs, rawToMs)
        : fromMs; // 파싱 실패 시 from으로 보정

      const fromIso = new Date(fromMs).toISOString();
      const toIso = new Date(toMs).toISOString();

      const res = await fetchEquipStatus({
        equipmentId,
        from: fromIso,
        to: toIso,
        page: 0,
        size: 20,
        sort: "startTime,desc",
      });
      return toPage(res);
    },
  });

  // 생성 제출
  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");

    // 입력 date+time → UTC ISO(Z)로 조합
    // type="time"은 HH:mm을 보장하므로 바로 합쳐도 안전
    const startIso = new Date(`${date}T${time}:00Z`).toISOString();

    try {
      await createEquipStatus({
        equipmentId,
        statusCode,
        startTimeUtc: startIso,
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

      {/* 검색 바 */}
      <form onSubmit={submit} className="flex flex-wrap items-end gap-2 mb-4">
        <div className="flex flex-col">
          <label className="text-sm text-gray-600">설비</label>
          <input
            className="border px-2 py-1"
            value={equipmentId}
            onChange={(e) => setEqp(e.target.value)}
          />
        </div>

        <div className="flex flex-col">
          <label className="text-sm text-gray-600">시작 날짜(From)</label>
          <input
            className="border px-2 py-1"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
        </div>

        <div className="flex flex-col">
          <label className="text-sm text-gray-600">끝 날짜(To)</label>
          <input
            className="border px-2 py-1"
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
          />
        </div>
      </form>

      {/* 등록 바 */}
      <form onSubmit={submit} className="flex flex-wrap items-end gap-2 mb-4">
        <div className="flex flex-col">
          <label className="text-sm text-gray-600">등록 날짜</label>
          <input
            className="border px-2 py-1"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
        </div>
        <div className="flex flex-col">
          <label className="text-sm text-gray-600">등록 시각(UTC)</label>
          <input
            className="border px-2 py-1"
            type="time"
            value={time}
            onChange={(e) => setTime(e.target.value)}
          />
        </div>
        <div className="flex flex-col">
          <label className="text-sm text-gray-600">상태</label>
          <select
            className="border px-2 py-1"
            value={statusCode}
            onChange={(e) => setStatus(e.target.value as "RUN" | "IDLE" | "DOWN")}
          >
            <option value="RUN">RUN</option>
            <option value="IDLE">IDLE</option>
            <option value="DOWN">DOWN</option>
          </select>
        </div>

        <CanWrite>
          <button className="bg-green-500 text-white px-3 py-2 rounded" type="submit">
            RUN 등록
          </button>
        </CanWrite>
        {err && <span className="text-red-600 ml-2">{err}</span>}
      </form>

      {/* 목록 */}
      <div>
        <h2 className="font-semibold mb-2">최근 상태</h2>
        {isLoading ? (
          <div>로딩...</div>
        ) : error ? (
          <div>오류</div>
        ) : !data ? null : (
          <table className="w-full border">
            <thead>
              <tr className="bg-gray-100">
                <th className="p-2">ID</th>
                <th className="p-2">설비</th>
                <th className="p-2">상태</th>
                <th className="p-2">시작(KST)</th>
                <th className="p-2">종료(KST)</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((it: EquipStatusItem) => (
                <tr key={it.logId} className="border-t">
                  <td className="p-2">{it.logId}</td>
                  <td className="p-2">{it.equipmentId}</td>
                  <td className="p-2">{it.status ?? "-"}</td>
                  <td className="p-2">
                    {new Date(it.startTime).toLocaleString("ko-KR", {
                      timeZone: "Asia/Seoul",
                    })}
                  </td>
                  <td className="p-2">
                    {it.endTime
                      ? new Date(it.endTime).toLocaleString("ko-KR", {
                          timeZone: "Asia/Seoul",
                        })
                      : "-"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}