import { BrowserRouter, Routes, Route } from "react-router-dom";
import ProtectedRoute from "./routes/ProtectedRoute";
import AppLayout from "./components/layout/AppLayout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import WorkOrdersList from "./pages/workorders/WorkOrdersList";
import PerformancesList from "./pages/performances/PerformancesList";
import EquipStatusPage from "./pages/equipstatus/EquipStatusPage";
import KpiPage from "./pages/kpi/KpiPage";
import WorkOrderCreate from "./pages/workorders/WorkOrderCreate";
import PerformanceCreate from "./pages/performances/PerformanceCreate";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login/>} />
        <Route path="/" element={
          <ProtectedRoute><AppLayout/></ProtectedRoute>
        }>
          <Route index element={<Dashboard/>}/>
          <Route path="work-orders" element={<WorkOrdersList/>}/>
          <Route path="performances" element={<PerformancesList/>}/>
          <Route path="performances/new" element={<PerformanceCreate/>}/>

          <Route path="equip-status" element={<EquipStatusPage/>}/>
          <Route path="kpi" element={<KpiPage/>}/>
          <Route path="work-orders" element={<WorkOrdersList/>}/>
          <Route path="work-orders/new" element={<WorkOrderCreate/>}/>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}