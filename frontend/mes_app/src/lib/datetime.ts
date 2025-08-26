// 서버가 Z/오프셋 포함 ISO를 보낸다는 전제라면 그냥 Date(raw)면 충분.
// 무TZ를 받을 일 없도록 서버를 고정했으므로 보정 분기 제거 가능.
export function parseServerDate(raw?: string | null): Date | null {
  if (!raw) return null;
  return new Date(raw);
}


// HH:mm (24h) 형식 검증
export function isValidTimeStr(t?: string | null): t is string {
  if (!t) return false;
  return /^([01]\d|2[0-3]):([0-5]\d)$/.test(t);
}

// 기준 날짜(UTC) 문자열: YYYY-MM-DD 반환
export function toBaseDateStrFromIso(iso?: string | null): string {
  // iso가 있으면 그 날짜(UTC), 없으면 오늘(UTC)
  const d = iso ? new Date(iso) : new Date();
  // 항상 UTC 날짜로 잘라서 씀
  return d.toISOString().slice(0, 10); // YYYY-MM-DD
}

// HH:mm + 기준 날짜(YYYY-MM-DD) → UTC ISO(Z)
// 유효하지 않으면 null 반환
export function toUtcIsoFromTime(timeStr?: string | null, baseDateStr?: string | null): string | null {
  if (!isValidTimeStr(timeStr)) return null;
  const dateStr = baseDateStr && /^\d{4}-\d{2}-\d{2}$/.test(baseDateStr)
    ? baseDateStr
    : new Date().toISOString().slice(0, 10);

  // UTC 기준 조합 (항상 Z 부착)
  const ms = Date.parse(`${dateStr}T${timeStr}:00Z`);
  if (Number.isNaN(ms)) return null;
  return new Date(ms).toISOString();
}