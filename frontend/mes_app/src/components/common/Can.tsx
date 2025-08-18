import { useMenusStore } from "../../store/menus";
import { useLocation } from "react-router-dom";

type Props = { write?: boolean; exec?: boolean; children: JSX.Element };

export default function Can({ write, exec, children }: Props) {
  const loc = useLocation();
  const perms = useMenusStore(s => s.getPerms(loc.pathname));
  if (!perms) return null;
  if (write && !perms.write) return null;
  if (exec && !perms.exec) return null;
  return children;
}