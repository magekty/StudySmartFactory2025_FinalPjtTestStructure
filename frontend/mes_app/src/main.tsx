import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { isAxiosError } from "axios";
console.log("TEST MESSAGE:", import.meta.env.VITE_TEST_MESSAGE);
console.log("API BASE URL:", import.meta.env.VITE_API_BASE);

// 401은 재시도 금지, 포커스 재조회 필요 없으면 false
const qc = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        if (isAxiosError(error) && error.response?.status === 401) return false;
        // 그 외는 기본 3회 유지(원하면 false로 완전 차단)
        return failureCount < 3;
      },
      refetchOnWindowFocus: false, // 필요 시 true로 되돌릴 수 있음
    },
  },
});
ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <QueryClientProvider client={qc}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>
);