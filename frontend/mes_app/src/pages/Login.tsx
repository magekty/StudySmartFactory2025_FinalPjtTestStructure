import { useState } from "react";
import { api } from "../lib/api";
import { useAuthStore } from "../store/auth";
import { useNavigate, useLocation } from "react-router-dom";
import { isAxiosError } from "axios";

type LoginRes = { token: string; user: { userId: string } };

export default function Login() {
  const [username, setU] = useState("");
  const [password, setP] = useState("");
  const setAuth = useAuthStore((s) => s.setAuth);
  const nav = useNavigate();
  const loc = useLocation();
  const from = (loc.state as { from?: string } | null)?.from ?? "/";

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const { data } = await api.post<LoginRes>("/auth/login", { username, password });
      setAuth(data.token, data.user.userId);
      nav(from, { replace: true });
    } catch (err: unknown) {
      const msg = isAxiosError<{ message?: string }>(err)
        ? err.response?.data?.message ?? "로그인 실패"
        : "로그인 실패";
      alert(msg);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center">
      <form onSubmit={submit} className="w-80 border rounded p-4 space-y-3">
        <div className="text-lg font-bold">로그인</div>
        <input className="w-full border px-2 py-1" placeholder="아이디"
               value={username} onChange={(e) => setU(e.target.value)} />
        <input className="w-full border px-2 py-1" type="password" placeholder="비밀번호"
               value={password} onChange={(e) => setP(e.target.value)} />
        <button className="w-full bg-black text-white py-2 rounded">로그인</button>
      </form>
    </div>
  );
}