export type ErrorResponse = {
  code: string;
  message: string;
  details?: Record<string, unknown> | null;
  traceId?: string;
  timestamp?: string;
  path?: string;
  method?: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sort: string;
};

export type PageResult<T> = {
  items: T[];
  page: number;
  size: number;
  total: number;
};