import { setAuthNotice } from "@/lib/auth-storage";

function resolveApiBase(): string {
  const configured = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
  if (typeof window === "undefined") return configured;

  const host = window.location.hostname;
  const isPrivateIpv4 =
    /^10\./.test(host) ||
    /^192\.168\./.test(host) ||
    /^172\.(1[6-9]|2\d|3[01])\./.test(host);

  // Only replace localhost for LAN testing. Public hosts (Vercel) must use the
  // configured Render gateway URL baked in through NEXT_PUBLIC_API_URL.
  if (isPrivateIpv4 && /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/i.test(configured)) {
    return `${window.location.protocol}//${host}:8080`;
  }

  return configured;
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

type RequestOptions = {
  method?: string;
  body?: unknown;
  token?: string | null;
  formData?: FormData;
  /** Internal: skip refresh retry to avoid loops */
  _retried?: boolean;
  /** Internal: remaining cold-start retries for 502/503/504 */
  _coldRetries?: number;
};

type AuthLike = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: unknown;
};

let refreshInFlight: Promise<string | null> | null = null;

function friendlyNetworkMessage() {
  const base = resolveApiBase();
  const localHint = /localhost|127\.0\.0\.1|^http:\/\/10\.|^http:\/\/192\.168\.|^http:\/\/172\./.test(base)
    ? " Start Docker and the backend services, then keep Gateway on port 8080."
    : " The backend may be waking up; wait a minute and try again.";
  return `Cannot reach API at ${base}.${localHint}`;
}

function isLocalApi(): boolean {
  return /localhost|127\.0\.0\.1|^http:\/\/10\.|^http:\/\/192\.168\.|^http:\/\/172\./.test(
    resolveApiBase()
  );
}

function hintForPath(path: string): string {
  if (!isLocalApi()) {
    return "Backend is waking up (free hosting sleeps when idle). Wait about a minute and refresh.";
  }
  if (path.startsWith("/api/v1/notifications")) {
    return "Start Notification service on 8085 (and keep Gateway on 8080).";
  }
  if (path.startsWith("/api/v1/scoring")) {
    return "Start Scoring service on 8084 (and keep Gateway on 8080).";
  }
  if (
    path.startsWith("/api/v1/candidates") ||
    path.startsWith("/api/v1/applications") ||
    /\/api\/v1\/jobs\/[^/]+\/applications/.test(path)
  ) {
    return "Start Candidate service on 8083 (and keep Gateway on 8080).";
  }
  if (path.startsWith("/api/v1/jobs")) {
    return "Start Job service on 8082 (and keep Gateway on 8080).";
  }
  if (path.startsWith("/api/v1/auth")) {
    return "Start Auth service on 8081 (and keep Gateway on 8080).";
  }
  return "Check Gateway (8080) and the matching backend service.";
}

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Render free services sleep after ~15m idle and take 30-90s to cold start,
 * returning 502/503/504 (or dropping the connection) until ready. Ride that
 * out for ~2 minutes so users never see a raw error on the first visit.
 */
const COLD_START_MAX_RETRIES = 12;
const COLD_START_DELAY_MS = 9_000;

function shouldRetryColdStart(status: number, options: RequestOptions): boolean {
  if (options._coldRetries !== undefined && options._coldRetries <= 0) return false;
  return status === 502 || status === 503 || status === 504;
}

function emitWaking(waking: boolean) {
  if (typeof window === "undefined") return;
  window.dispatchEvent(new CustomEvent(waking ? "tp:waking" : "tp:awake"));
}

async function refreshAccessToken(): Promise<string | null> {
  if (typeof window === "undefined") return null;
  const refreshToken = localStorage.getItem("tp_refresh");
  if (!refreshToken) return null;

  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      try {
        const res = await fetch(`${resolveApiBase()}/api/v1/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken }),
          cache: "no-store",
        });
        if (!res.ok) {
          localStorage.removeItem("tp_access");
          localStorage.removeItem("tp_refresh");
          localStorage.removeItem("tp_user");
          setAuthNotice("Your session ended. Sign in again to continue.");
          return null;
        }
        const auth = (await res.json()) as AuthLike;
        localStorage.setItem("tp_access", auth.accessToken);
        localStorage.setItem("tp_refresh", auth.refreshToken);
        localStorage.setItem("tp_user", JSON.stringify(auth.user));
        return auth.accessToken;
      } catch {
        return null;
      } finally {
        refreshInFlight = null;
      }
    })();
  }

  return refreshInFlight;
}

function extractMessage(data: unknown, status: number): string {
  const payload = data as {
    message?: string;
    error?: string;
    errors?: { message?: string }[];
  } | null;

  return (
    payload?.message ||
    (Array.isArray(payload?.errors) && payload.errors[0]?.message) ||
    (typeof payload?.error === "string" ? payload.error : null) ||
    `Request failed (${status})`
  );
}

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = {};
  if (options.token) {
    headers.Authorization = `Bearer ${options.token}`;
  }

  let body: BodyInit | undefined;
  if (options.formData) {
    body = options.formData;
  } else if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(options.body);
  }

  let res: Response;
  try {
    res = await fetch(`${resolveApiBase()}${path}`, {
      method: options.method ?? (options.body || options.formData ? "POST" : "GET"),
      headers,
      body,
      cache: "no-store",
    });
  } catch {
    // Cold start often drops the connection before the app is listening.
    if (!isLocalApi() && !options.formData) {
      const left = options._coldRetries ?? COLD_START_MAX_RETRIES;
      if (left > 0) {
        emitWaking(true);
        await sleep(COLD_START_DELAY_MS);
        return api<T>(path, { ...options, _coldRetries: left - 1 });
      }
    }
    emitWaking(false);
    throw new ApiError(0, friendlyNetworkMessage());
  }

  if (
    res.status === 401 &&
    options.token &&
    !options._retried &&
    !path.startsWith("/api/v1/auth/login") &&
    !path.startsWith("/api/v1/auth/refresh")
  ) {
    const nextToken = await refreshAccessToken();
    if (nextToken) {
      return api<T>(path, { ...options, token: nextToken, _retried: true });
    }
    throw new ApiError(401, "Session expired. Please sign in again.");
  }

  if (res.status === 204) {
    return undefined as T;
  }

  const text = await res.text();
  let data: unknown = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (res.ok) {
    emitWaking(false);
  }

  if (!res.ok) {
    if (shouldRetryColdStart(res.status, options) && !options.formData) {
      const left = options._coldRetries ?? COLD_START_MAX_RETRIES;
      if (left > 0) {
        emitWaking(true);
        await sleep(COLD_START_DELAY_MS);
        return api<T>(path, { ...options, _coldRetries: left - 1 });
      }
    }
    emitWaking(false);

    let message = extractMessage(data, res.status);
    if (res.status === 401) {
      if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register")) {
        message =
          message && message !== "Request failed (401)" && message !== "Unauthorized"
            ? message
            : "Invalid email or password. If this is production, register a new account first.";
      } else {
        message = "Session expired. Please sign in again.";
      }
    } else if (
      res.status === 500 ||
      res.status === 502 ||
      res.status === 503 ||
      res.status === 504
    ) {
      const generic =
        !data ||
        message === `Request failed (${res.status})` ||
        message === "Internal Server Error" ||
        message.toLowerCase().includes("connection refused");
      if (generic) {
        message = `Server error (${res.status}). ${hintForPath(path)}`;
      }
    }
    throw new ApiError(res.status, message);
  }

  return data as T;
}

export function formatApiError(err: unknown, fallback: string): string {
  if (err instanceof ApiError) return err.message;
  if (err instanceof Error && err.message) return err.message;
  return fallback;
}

export { resolveApiBase as getApiBase, refreshAccessToken };
