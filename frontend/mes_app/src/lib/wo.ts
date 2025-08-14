import { api } from "./api";
import type { WorkOrderCreateReq, WorkOrderCreateRes, WorkOrderStatusReq } from "../types/workorder";
import type { PageResponse } from "../types/api";
import type { WorkOrderItem } from "../types/workorder";

export async function fetchWorkOrders(params: { page: number; size: number; sort: string }) {
  const res = await api.get<PageResponse<WorkOrderItem>>("/work-orders", { params });
  return res.data;
}

export async function createWorkOrder(dto: WorkOrderCreateReq) {
  const res = await api.post<WorkOrderCreateRes>("/work-orders", dto);
  return res.data;
}

export async function changeWorkOrderStatus(id: string, body: WorkOrderStatusReq) {
  const res = await api.put<{ workOrderId: string; status: string }>(`/work-orders/${id}/status`, body);
  return res.data;
}