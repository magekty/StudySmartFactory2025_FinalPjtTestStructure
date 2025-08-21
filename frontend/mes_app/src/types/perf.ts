export type PerfCreateReq = {
  workOrderId: string;
  itemId: string;
  processId: string;
  equipmentId: string;
  producedQty: number;
  defectQty: number;
  startTime: string; // ISO UTC ("2025-08-10T09:00:00Z")
  endTime: string;   // ISO UTC
  requestId?: string; // 멱등키
};

export type PerfCreateRes = { performanceId: number; goodQty: number };