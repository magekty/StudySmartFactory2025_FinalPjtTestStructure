import { StatusColor } from "../../lib/kpi";

type Props = {
  title: string;
  targetLabel?: string;
  target?: string;
  actualLabel?: string;
  actual?: string;
  status: StatusColor;
};

export default function KpiCard({ title, targetLabel, target, actualLabel, actual, status }: Props) {
  const color =
    status === "ok" ? "border-green-600 text-green-700 bg-green-50"
    : status === "warn" ? "border-amber-500 text-amber-700 bg-amber-50"
    : status === "bad" ? "border-red-600 text-red-700 bg-red-50"
    : "border-gray-300 text-gray-700 bg-white";

  return (
    <div className={`border rounded p-3 ${color}`}>
      <div className="text-sm text-gray-600">{title}</div>
      <div className="mt-2 flex items-baseline gap-3">
        <div className="text-2xl font-semibold">{actual ?? "-"}</div>
        {target != null && (
          <div className="text-sm text-gray-600">
            {targetLabel ?? "목표"}: <span className="font-medium">{target}</span>
          </div>
        )}
      </div>
      {actualLabel && <div className="text-xs text-gray-500 mt-1">{actualLabel}</div>}
    </div>
  );
}