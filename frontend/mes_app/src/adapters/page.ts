import type { PageResponse } from "../types/api";

export function toPage<T>(resp: PageResponse<T>) {
  return {
    items: resp.content,
    page: resp.page,
    size: resp.size,
    total: resp.totalElements,
  };
}