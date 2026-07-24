"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { PasswordInput } from "@/components/PasswordInput";
import { useAuth } from "@/components/AuthProvider";
import { formatApiError } from "@/lib/api";
import { consumeAuthNotice } from "@/lib/auth-storage";

export default function LoginPage() {
  const { login } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [notice, setNotice] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const msg = consumeAuthNotice();
    if (msg) setNotice(msg);
  }, []);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setNotice(null);
    setLoading(true);
    try {
      const auth = await login(email.trim(), password);
      if (auth.user.role === "RECRUITER") router.push("/recruiter/jobs");
      else if (auth.user.role === "ADMIN") router.push("/admin");
      else router.push("/jobs");
    } catch (err) {
      const message = formatApiError(err, "Unable to sign in");
      if (/session expired/i.test(message)) {
        setNotice("Your session ended. Sign in again to continue.");
        setError(null);
      } else {
        setError(message);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell>
      <div className="tp-container-narrow tp-fade-up">
        <p className="tp-eyebrow">Welcome back</p>
        <h1 className="tp-display mt-2 text-[clamp(2rem,6vw,2.6rem)]">Sign in</h1>
        <p className="tp-muted mt-3 text-sm">
          New here?{" "}
          <Link href="/register" className="font-semibold text-[var(--accent)] hover:underline">
            Create an account
          </Link>
        </p>

        <form onSubmit={onSubmit} className="tp-panel mt-8 grid gap-4 p-5 sm:p-8">
          {notice ? <p className="tp-notice">{notice}</p> : null}
          <label className="tp-field">
            <span className="tp-label">Email</span>
            <input
              className="tp-input"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>
          <PasswordInput
            value={password}
            onChange={setPassword}
            autoComplete="current-password"
          />
          <p className="text-sm">
            <Link href="/forgot-password" className="font-semibold text-[var(--accent)] hover:underline">
              Forgot password?
            </Link>
          </p>
          {error ? <p className="tp-alert !mt-0">{error}</p> : null}
          <button className="tp-btn tp-btn-primary mt-1 w-full" type="submit" disabled={loading}>
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>
      </div>
    </AppShell>
  );
}
