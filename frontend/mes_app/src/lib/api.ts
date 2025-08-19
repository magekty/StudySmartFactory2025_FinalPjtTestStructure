import axios, { type AxiosInstance } from "axios";
import { useAuthStore } from "../store/auth";

const baseURL = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";
export const api: AxiosInstance = axios.create({ baseURL });

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err?.response?.status as number | undefined;
    if (status === 401 && useAuthStore.getState().token) {
      useAuthStore.getState().logout();
    }
    return Promise.reject(err);
  }
);