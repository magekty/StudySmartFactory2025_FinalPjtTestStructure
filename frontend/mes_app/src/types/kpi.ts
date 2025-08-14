export type KpiRes = {
  kpiDate: string;
  equipmentId: string;
  targetOee?: number | null;
  targetProductivity?: number | null;
  targetYield?: number | null;
  actualOutput?: number | null;
  actualYield?: number | null;
};