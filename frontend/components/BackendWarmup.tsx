"use client";

import { useEffect, useState } from "react";
import { getApiBase } from "@/lib/api";

/**
 * On production, ping the gateway warmup endpoint once so free-tier Render
 * services start spinning up as soon as someone opens the site.
 */
export function BackendWarmup() {
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const base = getApiBase();
    if (/localhost|127\.0\.0\.1/.test(base)) return;

    let cancelled = false;
    const started = Date.now();

    const run = async () => {
      setMessage("Starting servers… first visit after idle can take about a minute.");
      try {
        const res = await fetch(`${base}/api/v1/system/warmup`, {
          cache: "no-store",
          signal: AbortSignal.timeout(120_000),
        });
        if (cancelled) return;
        if (res.ok) {
          const body = (await res.json()) as { ready?: boolean };
          if (body.ready) {
            setMessage(null);
            return;
          }
        }
        // Still useful — individual services may finish waking via page API retries.
        if (Date.now() - started > 15_000) {
          setMessage("Almost ready — finishing startup…");
        }
      } catch {
        if (!cancelled) {
          setMessage("Waking backend services…");
        }
      } finally {
        if (!cancelled) {
          window.setTimeout(() => {
            if (!cancelled) setMessage(null);
          }, 8_000);
        }
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
