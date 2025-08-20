// src/hooks/usePerms.ts
import { useLocation } from "react-router-dom";
import { useMenusStore } from "../store/menus";

export function usePerms() {
  const loc = useLocation();
  const perms = useMenusStore(s => s.getPerms(loc.pathname));
  return {
    canRead: perms?.read === true,
    canWrite: perms?.write === true,
    canExec: perms?.exec === true,
  };
}