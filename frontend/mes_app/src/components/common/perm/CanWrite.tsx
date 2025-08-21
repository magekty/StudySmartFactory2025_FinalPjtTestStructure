// src/components/common/perm/CanWrite.tsx
import { usePerms } from "../../../hooks/usePerms";

export default function CanWrite({ children }: { children: JSX.Element }) {
  const { canWrite } = usePerms();
  return canWrite ? children : null;
}