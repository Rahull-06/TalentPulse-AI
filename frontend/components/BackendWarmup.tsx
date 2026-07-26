"use client";

import { useEffect, useState } from "react";
import { getApiBase } from "@/lib/api";

/**
 * Production: nudge the gateway (and via warmup, all backends) as soon as
 * someone opens the site. Never blocks UI; ignores CORS/network failures.
 */
export function BackendWarmup() {
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const base = getApiBase();
    if (/localhost|127\.0\.0\.1/.test(base)) return;

    let cancelled = false;
    setMessage("Starting servers… first open after idle can take about a minute.");

    const hideSoon = () => {
      window.setTimeout(() => {
        if (!cancelled) setMessage(null);
      }, 12_000);
    };

    const run = async () => {
      try {
        // Gateway health first (fast once awake)
        await fetch(`${base}/actuator/health`, {
          cache: "no-store",
          signal: AbortSignal.timeout(60_000),
        }).catch(() => null);

        // Kick all backends in parallel (fire-and-forget on server)
        await fetch(`${base}/api/v1/system/warmup`, {
          cache: "no-store",
          signal: AbortSignal.timeout(30_000),
        }).catch(() => null);
      } finally {
        if (!cancelled) hideSoon();
      }
    };

    void run();
    return () => {
      cancelled = true;
    };
  }, []);

  if (!message) return null;

  return (
    <div className="tp-warmup" role="status" aria-live="polite">
      {message}
    </div>
  );
}
