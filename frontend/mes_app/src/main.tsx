// src/main.tsx  (React Query 기본 옵션)
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { isAxiosError } from "axios";

const qc = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (count, error) => {
        if (isAxiosError(error) && error.response?.status === 401) return false;
        return count < 3;
      },
      refetchOnWindowFocus: false,
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