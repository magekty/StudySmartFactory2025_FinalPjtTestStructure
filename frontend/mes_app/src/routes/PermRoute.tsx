// src/routes/PermRoute.tsx
import { Navigate, useLocation } from "react-router-dom";
import { useMenusStore } from "../store/menus";
import type { MenuPerms } from "../store/menus";

type PermKind = keyof MenuPerms;
type Props = { require: PermKind | PermKind[]; children: JSX.Element };

export default function PermRoute({ require, children }: Props) {
  const loc = useLocation();
  const perms = useMenusStore((s) => s.getPerms(loc.pathname));
  const needs: PermKind[] = Array.isArray(require) ? require : [require];
  const ok = Boolean(perms && needs.every((k) => perms[k] === true));
  if (!ok) return <Navigate to="/403" replace state={{ from: loc.pathname }} />;
  return children;
}