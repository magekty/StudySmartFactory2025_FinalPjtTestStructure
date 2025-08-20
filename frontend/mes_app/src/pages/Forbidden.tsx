// src/pages/Forbidden.tsx
export default function Forbidden() {
  return (
    <div className="p-6">
      <div className="text-xl font-semibold mb-1">접근 권한이 없습니다</div>
      <div className="text-gray-600 text-sm">권한이 필요한 페이지입니다.</div>
    </div>
  );
}