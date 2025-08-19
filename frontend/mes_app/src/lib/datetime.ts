// 서버가 Z/오프셋 포함 ISO를 보낸다는 전제라면 그냥 Date(raw)면 충분.
// 무TZ를 받을 일 없도록 서버를 고정했으므로 보정 분기 제거 가능.
export function parseServerDate(raw?: string | null): Date | null {
  if (!raw) return null;
  return new Date(raw);
}

// 로컬 입력(yyyy-MM-dd + HH:mm)을 UTC ISO로 변환해 전송
export function toUtcIso(date: string, time: string): string {
  // 로컬 시각으로 생성 → toISOString()으로 UTC 변환
  return new Date(`${date}T${time}:00`).toISOString();
}