export type StatusColor = "ok" | "warn" | "bad" | "none";

export function statusColor(target?: number | null, actual?: number | null): StatusColor {
  if (target == null || actual == null) return "none";
  if (actual >= target) return "ok";
  const gap = (target - actual) / target;
  return gap <= 0.05 ? "warn" : "bad";
}

export function fmtPercent(v?: number | null): string {
  if (v == null) return "-";
  return `${v.toFixed(2)}%`;
}

export function fmtNumber(v?: number | null, digits = 0): string {
  if (v == null) return "-";
  return v.toFixed(digits);
}