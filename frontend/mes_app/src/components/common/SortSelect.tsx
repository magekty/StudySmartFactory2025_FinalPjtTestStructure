type Option = { label: string; value: string };

type Props = {
  value: string;                 // 예: "createdAt,desc"
  options: Option[];
  onChange: (v: string) => void;
};

export default function SortSelect({ value, options, onChange }: Props) {
  return (
    <select
      className="border px-2 py-1"
      value={value}
      onChange={(e) => onChange(e.target.value)}
    >
      {options.map((o) => (
        <option key={o.value} value={o.value}>{o.label}</option>
      ))}
    </select>
  );
}