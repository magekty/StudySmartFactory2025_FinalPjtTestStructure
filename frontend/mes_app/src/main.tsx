import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
console.log("TEST MESSAGE:", import.meta.env.VITE_TEST_MESSAGE);
console.log("API BASE URL:", import.meta.env.VITE_API_BASE);

const qc = new QueryClient();
ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <QueryClientProvider client={qc}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>
);