// src/lib/api.ts
import axios, { type AxiosInstance } from "axios";
import { useAuthStore } from "../store/auth";
import { isAxiosError } from "axios";

const baseURL = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";
export const api: AxiosInstance = axios.create({ baseURL });

// 요청 시 토큰 첨부
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// 401 처리: Authorization 달린 요청에서만 로그아웃
api.interceptors.response.use(
  (res) => res,
  (err) => {
    // axios 에러 타입 가드
    if (isAxiosError(err)) {
      const status = err.response?.status;
      const hadAuth = !!err.config?.headers?.Authorization; // 이 요청에 토큰이 실렸는지
      const hasToken = !!useAuthStore.getState().token;

      // 조건: 401 + 토큰 실린 요청 + 현재 스토어에 토큰 존재 → 로그아웃
      if (status === 401 && hadAuth && hasToken) {
        useAuthStore.getState().logout();
      }
    }
    return Promise.reject(err);
  }
);