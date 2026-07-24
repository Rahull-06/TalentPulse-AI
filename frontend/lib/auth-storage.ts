"use client";

import type { AuthResponse, User } from "@/lib/types";

const ACCESS = "tp_access";
const REFRESH = "tp_refresh";
const USER = "tp_user";
const NOTICE = "tp_auth_notice";

export function saveSession(auth: AuthResponse) {
  localStorage.setItem(ACCESS, auth.accessToken);
  localStorage.setItem(REFRESH, auth.refreshToken);
  localStorage.setItem(USER, JSON.stringify(auth.user));
  sessionStorage.removeItem(NOTICE);
}

export function clearSession() {
  localStorage.removeItem(ACCESS);
  localStorage.removeItem(REFRESH);
  localStorage.removeItem(USER);
}

export function setAuthNotice(message: string) {
  if (typeof window === "undefined") return;
  sessionStorage.setItem(NOTICE, message);
}

export function consumeAuthNotice(): string | null {
  if (typeof window === "undefined") return null;
  const msg = sessionStorage.getItem(NOTICE);
  if (msg) sessionStorage.removeItem(NOTICE);
  return msg;
}

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS);
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(REFRESH);
}

export function getStoredUser(): User | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(USER);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
}
