// src/store/menus.ts
import { create } from "zustand";

export type MenuPerms = { read: boolean; write: boolean; exec: boolean };

export type MenuNode = {
  code: string;
  name: string;
  path: string;
  perms: MenuPerms;
  children: MenuNode[];
};

type MenusState = {
  tree: MenuNode[];
  flat: Record<string, MenuPerms>;
  setMenus: (tree: MenuNode[]) => void;
  getPerms: (path: string) => MenuPerms | undefined;
};

function flatten(tree: MenuNode[], acc: Record<string, MenuPerms>) {
  for (const n of tree) {
    if (n.path) acc[n.path] = n.perms;
    if (n.children?.length) flatten(n.children, acc);
  }
}

export const useMenusStore = create<MenusState>((set, get) => ({
  tree: [],
  flat: {},
  setMenus: (tree) => {
    const flat: Record<string, MenuPerms> = {};
    flatten(tree, flat);
    set({ tree, flat });
  },
  // prefix 매칭: /a/b/c → /a/b 권한도 허용
  getPerms: (path) => {
    const flat = get().flat;
    if (flat[path]) return flat[path];
    const hit = Object.keys(flat)
      .filter((k) => path === k || path.startsWith(k + "/"))
      .sort((a, b) => b.length - a.length)[0];
    return hit ? flat[hit] : undefined;
  },
}));