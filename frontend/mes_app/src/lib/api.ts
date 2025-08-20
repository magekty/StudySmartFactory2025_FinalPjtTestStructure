// src/lib/api.ts
import axios, { type AxiosInstance } from "axios";
import { useAuthStore } from "../store/auth";
import { isAxiosError } from "axios";

const baseURL = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";
export const api: AxiosInstance = axios.create({ baseURL });

// 요청: 토큰 부착
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// 응답: 401 가드(토큰 달고 보낸 요청에서만 logout)
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (isAxiosError(err)) {
      const status = err.response?.status;
      const hadAuth = !!err.config?.headers?.Authorization;
      const hasToken = !!useAuthStore.getState().token;
      if (status === 401 && hadAuth && hasToken) {
        useAuthStore.getState().logout();
      }
    }
    return Promise.reject(err);
  }
);