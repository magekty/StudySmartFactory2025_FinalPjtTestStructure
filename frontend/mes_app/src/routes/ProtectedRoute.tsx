import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore } from "../store/auth";

export default function ProtectedRoute({ children }: { children: JSX.Element }) {
  const token = useAuthStore(s => s.token);
  const ready = useAuthStore(s => s.ready);
  const loc = useLocation();

  if (!ready) return null;
  if (!token) return <Navigate to="/login" replace state={{ from: loc.pathname }} />;
  return children;
}