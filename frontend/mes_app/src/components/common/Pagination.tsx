import React from "react";

type Props = {
  page: number;         // 0-base
  size: number;
  total: number;
  onPageChange: (nextPage: number) => void;
};

export default function Pagination({ page, size, total, onPageChange }: Props) {
  const totalPages = Math.max(1, Math.ceil(total / Math.max(1, size)));
  const canPrev = page > 0;
  const canNext = page < totalPages - 1;

  return (
    <div className="flex items-center gap-2 mt-3">
      <button
        className="border px-2 py-1 rounded disabled:opacity-50"
        disabled={!canPrev}
        onClick={() => onPageChange(page - 1)}
      >
        이전
      </button>
      <span className="text-sm text-gray-600">
        {page + 1} / {totalPages} (총 {total}건)
      </span>
      <button
        className="border px-2 py-1 rounded disabled:opacity-50"
        disabled={!canNext}
        onClick={() => onPageChange(page + 1)}
      >
        다음
      </button>
    </div>
  );
}