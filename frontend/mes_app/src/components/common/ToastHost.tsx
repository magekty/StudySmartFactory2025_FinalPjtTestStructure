import { useToast } from "../../store/toast";

export default function ToastHost() {
  const items = useToast(s => s.items);
  return (
    <div className="fixed right-4 bottom-4 flex flex-col gap-2 z-50">
      {items.map(i => {
        const color = i.kind === "success" ? "bg-green-600"
          : i.kind === "error" ? "bg-red-600" : "bg-gray-800";
        return (
          <div key={i.id} className={`${color} text-white px-3 py-2 rounded shadow`}>
            {i.text}
          </div>
        );
      })}
    </div>
  );
}