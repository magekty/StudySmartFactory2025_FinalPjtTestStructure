import { useMenusStore } from "../../store/menus";
import { useLocation } from "react-router-dom";

type Props = { write?: boolean; exec?: boolean; children: JSX.Element };


/**
 * @deprecated 이 컴포넌트는 더 이상 사용되지 않습니다. 대신 새로운 PermissionsGuard 컴포넌트를 사용해주세요.
 */
export default function Can({ write, exec, children }: Props) {
  const loc = useLocation();
  const perms = useMenusStore(s => s.getPerms(loc.pathname));
  if (!perms) return null;
  if (write && !perms.write) return null;
  if (exec && !perms.exec) return null;
  return children;
}