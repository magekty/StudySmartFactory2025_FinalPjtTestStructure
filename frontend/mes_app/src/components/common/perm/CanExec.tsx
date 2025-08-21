// src/components/common/perm/CanExec.tsx
import { usePerms } from "../../../hooks/usePerms";

export default function CanExec({ children }: { children: JSX.Element }) {
  const { canExec } = usePerms();
  return canExec ? children : null;
}