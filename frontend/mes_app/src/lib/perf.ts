import { api } from "./api";
import type { PerfCreateReq, PerfCreateRes } from "../types/perf";

export async function createPerformance(dto: PerfCreateReq) {
  const res = await api.post<PerfCreateRes>("/performances", dto);
  return res.data;
}