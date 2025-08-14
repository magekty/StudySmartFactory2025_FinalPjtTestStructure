import { api } from "./api";
import type { PageResponse } from "../types/api";
import type { EquipStatusItem, EquipStatusCreateReq } from "../types/equip";

export async function fetchEquipStatus(params: {
  equipmentId: string;
  from?: string; to?: string;
  page: number; size: number; sort: string;
}) {
  const res = await api.get<PageResponse<EquipStatusItem>>("/equip-status", { params });
  return res.data;
}

export async function createEquipStatus(dto: EquipStatusCreateReq) {
  // POST /equip-status (등록)
  const res = await api.post<{ logId: number }>("/equip-status", dto);
  return res.data;
}