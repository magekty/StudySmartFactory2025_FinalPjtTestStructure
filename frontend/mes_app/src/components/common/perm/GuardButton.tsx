// src/components/common/perm/GuardButton.tsx
// 권한이 없으면 아예 안 보이게(숨김). 필요 시 'renderDisabled' 옵션으로 비활성 표시 전환 가능.
import { usePerms } from "../../../hooks/usePerms";

type Props = {
  require: "write" | "exec";
  onClick?: () => void;
  className?: string;
  children: React.ReactNode;
  renderDisabled?: boolean;
};
export default function GuardButton({ require, onClick, className, children, renderDisabled }: Props) {
  const { canWrite, canExec } = usePerms();
  const ok = require === "write" ? canWrite : canExec;

  if (ok) return <button className={className} onClick={onClick}>{children}</button>;
  if (renderDisabled) {
    return <button className={`bg-gray-300 text-gray-600 ${className ?? ""}`} disabled>{children}</button>;
  }
  return null;
}