import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import ToastHost from "../common/ToastHost";

export default function AppLayout() {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <main className="flex-1 p-4">
        <Outlet />
      </main>
      <ToastHost />
    </div>
  );
}