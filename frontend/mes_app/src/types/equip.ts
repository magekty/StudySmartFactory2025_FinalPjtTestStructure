export type EquipStatusItem = {
  logId: number;
  equipmentId: string;
  status: string | null;        // RUN/IDLE/DOWN
  startTime: string;            // ISO
  endTime: string | null;
};

export type EquipStatusCreateReq = {
  equipmentId: string;
  statusCode: "RUN" | "IDLE" | "DOWN";
  startTimeUtc: string;         // "2025-08-10T09:00:00Z"
  endTimeUtc?: string;
};