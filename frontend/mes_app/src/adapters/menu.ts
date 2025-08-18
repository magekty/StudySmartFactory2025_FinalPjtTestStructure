import type { MenuItem } from "../types/menu";
import type { MenuNode } from "../store/menus";

type ServerMenu = {
  code: string; name: string; path: string;
  perms: { read: boolean; write: boolean; exec: boolean };
  children: ServerMenu[];
};

export function toMenuItems(serverMenus: ServerMenu[]): MenuItem[] {
  return serverMenus.map((m) => ({
    key: m.code,
    title: m.name,
    path: m.path,
    perms: m.perms,
    children: m.children?.length ? toMenuItems(m.children) : [],
  }));
}

export function toMenuNodesFromItems(items: MenuItem[]): MenuNode[] {
  return items.map((m) => ({
    code: m.key,
    name: m.title,
    path: m.path,
    perms: { ...m.perms },
    children: m.children?.length ? toMenuNodesFromItems(m.children) : [],
  }));
}