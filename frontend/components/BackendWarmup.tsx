"use client";

import { useEffect, useState } from "react";
import { getApiBase } from "@/lib/api";

/**
 * Production: nudge the gateway (and via warmup, all backends) as soon as
 * someone opens the site, and show a friendly banner whenever any API call is
 * riding out a cold start. Never blocks the UI.
 */
export function BackendWarmup() {
  const [waking, setWaking] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const base = getApiBase();
    if (/localhost|127\.0\.0\.1/.test(base)) return;

    const onWaking = () => setWaking(true);
    const onAwake = () => setWaking(false);
    window.addEventListener("tp:waking", onWaking);
    window.addEventListener("tp:awake", onAwake);

    let cancelled = false;
    const kick = async () => {
      try {
        await fetch(`${base}/actuator/health`, {
          cache: "no-store",
          signal: AbortSignal.timeout(60_000),
        }).catch(() => null);
        await fetch(`${base}/api/v1/system/warmup`, {
          cache: "no-store",
          signal: AbortSignal.timeout(30_000),
        }).catch(() => null);
      } catch {
        /* ignore */
      }
      if (!cancelled) setWaking(false);
    };
    void kick();

    return () => {
      cancelled = true;
      window.removeEventListener("tp:waking", onWaking);
      window.removeEventListener("tp:awake", onAwake);
    };
  }, []);

  if (!waking) return null;

  return (
    <div className="tp-warmup" role="status" aria-live="polite">
      Waking servers… free hosting sleeps when idle, so the first action after a
      break can take up to a minute. Please wait — this will continue
      automatically.
    </div>
  );
}
