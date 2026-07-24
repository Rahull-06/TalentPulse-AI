import { setAuthNotice } from "@/lib/auth-storage";

function resolveApiBase(): string {
  const configured = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
  if (typeof window === "undefined") return configured;
  const host = window.location.hostname;
  // When opening the UI via LAN IP, call the gateway on the same host (not localhost).
  if (host !== "localhost" && host !== "127.0.0.1") {
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
};

type AuthLike = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: unknown;
};

let refreshInFlight: Promise<string | null> | null = null;

function friendlyNetworkMessage() {
  return `Cannot reach API at ${resolveApiBase()}. Start Docker, then Auth (8081), Job (8082), Candidate (8083), Scoring (8084), Notification (8085), Gateway (8080).`;
}

function hintForPath(path: string): string {
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

  if (!res.ok) {
    let message = extractMessage(data, res.status);
    if (res.status === 401) {
      message = "Session expired. Please sign in again.";
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
