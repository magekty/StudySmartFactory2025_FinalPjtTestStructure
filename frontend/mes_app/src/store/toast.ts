import { create } from "zustand";

export type ToastItem = { id: number; text: string; kind?: "info"|"success"|"error" };

type ToastState = {
  items: ToastItem[];
  push: (text: string, kind?: ToastItem["kind"]) => void;
  remove: (id: number) => void;
};

export const useToast = create<ToastState>((set, get) => ({
  items: [],
  push: (text, kind = "info") => {
    const id = Date.now() + Math.random();
    set({ items: [...get().items, { id, text, kind }] });
    setTimeout(() => get().remove(id), 2500);
  },
  remove: (id) => set({ items: get().items.filter(i => i.id !== id) }),
}));