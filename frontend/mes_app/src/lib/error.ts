import { isAxiosError } from "axios";
import type { ErrorResponse } from "../types/api";

export function getErrorMessage(err: unknown, fallback = "요청 실패"): string {
  if (isAxiosError<ErrorResponse>(err)) {
    return err.response?.data?.message ?? fallback;
  }
  if (err instanceof Error) return err.message;
  return fallback;
}