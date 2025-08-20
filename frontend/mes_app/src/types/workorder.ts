export type WorkOrderItem = {
  workOrderId: string;
  workOrderNumber: string;
  itemId: string;
  processId: string;
  equipmentId: string;
  orderQty: number;
  producedQty: number;
  status: string | null; // "P"|"R"|"C"|null
  startTs?: string | null;      // UTC ISO
  createdAt: string;
  modifiedAt?: string | null;
};

export type WorkOrderCreateReq = {
  workOrderNumber: string;
  itemId: string;
  processId: string;
  equipmentId: string;
  orderQty: number;
  createdBy?: string;
};

export type WorkOrderCreateRes = { workOrderId: string; status: string };
export type WorkOrderStatusReq = { toStatus: "R" | "C" };