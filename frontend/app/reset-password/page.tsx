"use client";

import Link from "next/link";
import { FormEvent, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { AppShell } from "@/components/AppShell";
import { PasswordInput } from "@/components/PasswordInput";
import { api, formatApiError } from "@/lib/api";

function ResetPasswordForm() {
  const search = useSearchParams();
  const router = useRouter();
  const token = useMemo(() => search.get("token") ?? "", [search]);
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!token) {
      setError("Missing reset token. Open the link from your email / Auth logs.");
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await api("/api/v1/auth/reset-password", {
        method: "POST",
        body: { token, newPassword: password },
      });
      router.push("/login");
    } catch (err) {
      setError(formatApiError(err, "Could not reset password"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="tp-container-narrow tp-fade-up">
      <p className="tp-eyebrow">Account</p>
      <h1 className="tp-display mt-2 text-[clamp(2rem,6vw,2.6rem)]">Reset password</h1>
      <p className="tp-muted mt-3 text-sm">Choose a new password. The reset link works once and expires in 30 minutes.</p>

      <form onSubmit={onSubmit} className="tp-panel mt-8 grid gap-4 p-5 sm:p-8">
        <PasswordInput
          label="New password"
          value={password}
          onChange={setPassword}
          autoComplete="new-password"
          minLength={8}
        />
        {error ? <p className="tp-alert">{error}</p> : null}
        <button className="tp-btn tp-btn-primary w-full sm:w-auto" type="submit" disabled={loading || !token}>
          {loading ? "Saving…" : "Update password"}
        </button>
        <Link href="/login" className="text-sm font-semibold text-[var(--accent)] hover:underline">
          Back to sign in
        </Link>
      </form>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <AppShell>
      <Suspense fallback={<p className="tp-muted p-8 text-sm">Loading…</p>}>
        <ResetPasswordForm />
      </Suspense>
    </AppShell>
  );
}
