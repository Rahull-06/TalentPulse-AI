"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/AppShell";
import { useAuth } from "@/components/AuthProvider";

export default function AdminPage() {
  const { user, ready } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!ready) return;
    if (!user) router.replace("/login");
  }, [ready, user, router]);

  return (
    <AppShell>
      <div className="tp-fade-up mx-auto max-w-2xl">
        <p className="tp-eyebrow">Admin</p>
        <h1 className="tp-display mt-2 text-4xl">Basics</h1>
        <p className="tp-muted mt-4 text-sm leading-relaxed">
          Organization and user admin APIs land in a later phase. For now, use Auth and service
          Swagger for operational checks.
        </p>
        <div className="tp-panel mt-8 p-6">
          <p className="text-sm text-[var(--ink-soft)]">
            Signed in as <strong>{user?.fullName}</strong> ({user?.role}).
          </p>
        </div>
      </div>
    </AppShell>
  );
}
