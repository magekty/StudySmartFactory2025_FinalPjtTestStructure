import { useEffect, useState } from "react";
import { api } from "../../lib/api";
import { toMenuItems, toMenuNodesFromItems } from "../../adapters/menu";
import type { MenuItem } from "../../types/menu";
import { useMenusStore } from "../../store/menus";
import { Link, useLocation } from "react-router-dom";

export default function Sidebar() {
  const [menus, setMenus] = useState<MenuItem[]>([]);
  const setMenusGlobal = useMenusStore((s) => s.setMenus);
  const loc = useLocation();

  useEffect(() => {
    api.get("/menus/my")
      .then((res) => {
        const uiTree = toMenuItems(res.data.menus);           // 서버 → UI
        setMenus(uiTree);
        const nodes = toMenuNodesFromItems(uiTree);           // UI → 전역(MenuNode)
        setMenusGlobal(nodes);                                // any 없이 안전
      })
      .catch(() => {
        setMenus([]);
        setMenusGlobal([]);                                   // 전역도 비우기
      });
  }, [setMenusGlobal]);

  return (
    <aside className="w-64 border-r p-3">
      <div className="font-bold mb-3">GlobalMed MES</div>
      <nav className="flex flex-col gap-2">
        {menus.map((m) => (
          <Link key={m.key}
                className={`px-2 py-1 rounded ${loc.pathname.startsWith(m.path) 
                  ? "bg-gray-200" : ""}`}
                to={m.path}>
            {m.title}
          </Link>
        ))}
      </nav>
    </aside>
  );
}