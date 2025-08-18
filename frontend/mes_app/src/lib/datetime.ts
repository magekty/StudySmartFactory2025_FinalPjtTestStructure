export function toUtcIso(date: string, time: string): string {
  // date: yyyy-MM-dd, time: HH:mm
  return new Date(`${date}T${time}:00Z`).toISOString();
}