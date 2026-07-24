"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { api, formatApiError } from "@/lib/api";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await api("/api/v1/auth/forgot-password", {
        method: "POST",
        body: { email: email.trim() },
      });
      setDone(true);
    } catch (err) {
      setError(formatApiError(err, "Could not start password reset"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell>
      <div className="tp-container-narrow tp-fade-up">
        <p className="tp-eyebrow">Account</p>
        <h1 className="tp-display mt-2 text-[clamp(2rem,6vw,2.6rem)]">Forgot password</h1>
        <p className="tp-muted mt-3 text-sm">
          We email a reset link (no OTP). In local dev, copy the link from the Auth service console log.
        </p>

        {done ? (
          <div className="tp-panel mt-8 p-6 sm:p-8">
            <p className="font-semibold">If that email exists, a reset link was created.</p>
            <p className="tp-muted mt-2 text-sm">
              Check the Auth terminal for: <code>Password reset link for …</code>
            </p>
            <Link href="/login" className="tp-btn tp-btn-primary mt-6 inline-flex">
              Back to sign in
            </Link>
          </div>
        ) : (
          <form onSubmit={onSubmit} className="tp-panel mt-8 grid gap-4 p-5 sm:p-8">
            <label className="tp-field">
              <span className="tp-label">Email</span>
              <input
                className="tp-input"
                type="email"
                required
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </label>
            {error ? <p className="tp-alert">{error}</p> : null}
            <button className="tp-btn tp-btn-primary w-full sm:w-auto" type="submit" disabled={loading}>
              {loading ? "Sending…" : "Send reset link"}
            </button>
            <Link href="/login" className="text-sm font-semibold text-[var(--accent)] hover:underline">
              Back to sign in
            </Link>
          </form>
        )}
      </div>
    </AppShell>
  );
}
