"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/AppShell";
import { EmptyState } from "@/components/EmptyState";
import { useAuth } from "@/components/AuthProvider";
import { api, ApiError } from "@/lib/api";
import type { NotificationItem, PageResponse } from "@/lib/types";

export default function NotificationsPage() {
  const { user, token, ready } = useAuth();
  const router = useRouter();
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [unread, setUnread] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = async (access: string) => {
    const page = await api<PageResponse<NotificationItem>>(
      "/api/v1/notifications/me?page=0&size=40",
      { token: access }
    );
    setItems(page.content);
    const count = await api<{ unreadCount: number }>("/api/v1/notifications/me/unread-count", {
      token: access,
    });
    setUnread(count.unreadCount);
  };

  useEffect(() => {
    if (!ready) return;
    if (!token || !user) {
      router.replace("/login");
      return;
    }
    const run = async () => {
      try {
        await load(token);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Could not load inbox");
      } finally {
        setLoading(false);
      }
    };
    void run();
  }, [ready, token, user, router]);

  const markOne = async (id: string) => {
    if (!token) return;
    await api(`/api/v1/notifications/${id}/read`, { method: "POST", token });
    await load(token);
  };

  const markAll = async () => {
    if (!token) return;
    await api("/api/v1/notifications/me/read-all", { method: "POST", token });
    await load(token);
  };

  return (
    <AppShell>
      <div className="tp-fade-up mx-auto max-w-2xl">
        <header className="tp-page-head flex flex-wrap items-end justify-between gap-3">
          <div>
            <p className="tp-eyebrow">Inbox</p>
            <h1 className="tp-display text-[clamp(2rem,5vw,2.75rem)]">Notifications</h1>
            <p className="tp-muted mt-2 text-sm">
              {unread > 0 ? (
                <>
                  <span className="font-semibold text-[var(--accent)]">{unread} unread</span>
                  <span> · stay on top of application updates</span>
                </>
              ) : (
                "You're all caught up"
              )}
            </p>
          </div>
          {items.length > 0 && unread > 0 ? (
            <button type="button" className="tp-btn tp-btn-soft tp-btn-compact" onClick={() => void markAll()}>
              Mark all read
            </button>
          ) : null}
        </header>

        {error ? <p className="tp-alert">{error}</p> : null}

        <div>
          {loading ? (
            <p className="tp-muted text-sm">Loading…</p>
          ) : error ? (
            <EmptyState
              title="Inbox unavailable"
              body="Start Notification service on port 8085, keep Gateway on 8080, then refresh."
            />
          ) : items.length === 0 ? (
            <EmptyState title="All quiet" body="When something important happens, it will show up here." />
          ) : (
            <ul className="tp-list">
              {items.map((n) => (
                <li
                  key={n.id}
                  className={`tp-list-item${n.read ? "" : " tp-list-item-unread"}`}
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        {!n.read ? (
                          <span className="inline-block h-1.5 w-1.5 rounded-full bg-[var(--accent)]" aria-hidden />
                        ) : null}
                        <p className="font-semibold tracking-tight">{n.title}</p>
                      </div>
                      <p className="tp-muted mt-1.5 text-sm leading-relaxed break-words">{n.message}</p>
                      <p className="tp-muted mt-2 text-xs">
                        {new Date(n.createdAt).toLocaleString()}
                      </p>
                    </div>
                    {!n.read ? (
                      <button
                        type="button"
                        className="tp-btn tp-btn-ghost tp-btn-compact shrink-0"
                        onClick={() => void markOne(n.id)}
                      >
                        Mark read
                      </button>
                    ) : null}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </AppShell>
  );
}
