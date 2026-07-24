"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { PasswordInput } from "@/components/PasswordInput";
import { useAuth } from "@/components/AuthProvider";
import { api, formatApiError } from "@/lib/api";
import type { AuthResponse } from "@/lib/types";

export default function CandidateRegisterPage() {
  const { setAuth } = useAuth();
  const router = useRouter();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const auth = await api<AuthResponse>("/api/v1/auth/register/candidate", {
        method: "POST",
        body: { fullName, email, password },
      });
      setAuth(auth);
      router.push("/candidate/profile");
    } catch (err) {
      setError(formatApiError(err, "Registration failed"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell>
      <div className="tp-container-narrow tp-fade-up">
        <p className="tp-eyebrow">Candidate</p>
        <h1 className="tp-display mt-2 text-[clamp(2rem,6vw,2.6rem)]">Create account</h1>
        <p className="tp-muted mt-3 text-sm">
          Already registered?{" "}
          <Link href="/login" className="font-semibold text-[var(--accent)] hover:underline">
            Sign in
          </Link>
        </p>

        <form onSubmit={onSubmit} className="tp-panel mt-8 grid gap-4 p-5 sm:p-8">
          <label className="tp-field">
            <span className="tp-label">Full name</span>
            <input className="tp-input" required value={fullName} onChange={(e) => setFullName(e.target.value)} />
          </label>
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
          <PasswordInput
            value={password}
            onChange={setPassword}
            autoComplete="new-password"
          />
          {error ? <p className="tp-alert">{error}</p> : null}
          <button className="tp-btn tp-btn-primary mt-1 w-full sm:w-auto" disabled={loading}>
            {loading ? "Creating…" : "Create account"}
          </button>
        </form>
      </div>
    </AppShell>
  );
}
