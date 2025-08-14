import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createWorkOrder } from "../../lib/wo";
import { isAxiosError } from "axios";

export default function WorkOrderCreate() {
  const [workOrderNumber, setNo] = useState("");
  const [itemId, setItem] = useState("I-0001");
  const [processId, setProc] = useState("P-0001");
  const [equipmentId, setEqp] = useState("E-0001");
  const [orderQty, setQty] = useState<number>(100);
  const [err, setErr] = useState("");
  const nav = useNavigate();

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr("");
    try {
      await createWorkOrder({ workOrderNumber, itemId, processId, equipmentId, orderQty });
      nav("/work-orders", { replace: true });
    } catch (error: unknown) {
      const msg = isAxiosError<{ message?: string }>(error)
        ? error.response?.data?.message ?? "생성 실패"
        : "생성 실패";
      setErr(msg);
    }
  }

  return (
    <div>
      <h1 className="text-lg font-semibold mb-3">작업지시 생성</h1>
      <form onSubmit={submit} className="grid gap-3 max-w-md">
        {err && <div className="text-red-600">{err}</div>}
        <input className="border px-2 py-1" placeholder="지시번호" value={workOrderNumber} onChange={(e)=>setNo(e.target.value)} required />
        <input className="border px-2 py-1" placeholder="품목ID" value={itemId} onChange={(e)=>setItem(e.target.value)} required />
        <input className="border px-2 py-1" placeholder="공정ID" value={processId} onChange={(e)=>setProc(e.target.value)} required />
        <input className="border px-2 py-1" placeholder="설비ID" value={equipmentId} onChange={(e)=>setEqp(e.target.value)} required />
        <input className="border px-2 py-1" type="number" step="1" min="0" placeholder="지시수량" value={orderQty} onChange={(e)=>setQty(Number(e.target.value))} required />
        <div className="flex gap-2">
          <button className="bg-black text-white px-3 py-1 rounded" type="submit">생성</button>
          <button className="border px-3 py-1 rounded" type="button" onClick={()=>nav(-1)}>취소</button>
        </div>
      </form>
    </div>
  );
}