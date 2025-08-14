export function isTokenExpired(token: string): boolean {
  try {
    const [, payload] = token.split(".");
    const json = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    if (!json.exp) return false;
    return Date.now() >= json.exp * 1000;
  } catch {
    return false;
  }
}