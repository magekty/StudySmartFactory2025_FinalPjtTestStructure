import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { isTokenExpired } from "../lib/jwt";

type AuthState = {
  token: string | null;
  userId: string | null;
  ready: boolean;
  setAuth: (t: string, u: string) => void;
  logout: () => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      userId: null,
      ready: false,
      setAuth: (token, userId) => set({ token, userId }),
      logout: () => {
        if (get().token === null && get().userId === null) return;
        set({ token: null, userId: null });
      },
    }),
    {
      name: "auth",
      storage: createJSONStorage(() => localStorage),
      onRehydrateStorage: () => () => {
        queueMicrotask(() => {
          const { token } = useAuthStore.getState();
          if (token && isTokenExpired(token)) {
            useAuthStore.setState({ token: null, userId: null });
          }
          if (!useAuthStore.getState().ready) {
            useAuthStore.setState({ ready: true });
          }
        });
      },
    }
  )
);