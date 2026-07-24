"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { api, refreshAccessToken } from "@/lib/api";
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  getStoredUser,
  saveSession,
  setAuthNotice,
} from "@/lib/auth-storage";
import type { AuthResponse, User } from "@/lib/types";

type AuthContextValue = {
  user: User | null;
  token: string | null;
  ready: boolean;
  login: (email: string, password: string) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  setAuth: (auth: AuthResponse) => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const boot = async () => {
      const storedUser = getStoredUser();
      const access = getAccessToken();
      const refresh = getRefreshToken();

      if (refresh) {
        const next = await refreshAccessToken();
        if (next) {
          setUser(getStoredUser());
          setToken(next);
          setReady(true);
          return;
        }
        setAuthNotice("Your session ended. Sign in again to continue.");
        clearSession();
        setUser(null);
        setToken(null);
        setReady(true);
        return;
      }

      setUser(storedUser);
      setToken(access);
      setReady(true);
    };
    void boot();
  }, []);

  const setAuth = useCallback((auth: AuthResponse) => {
    saveSession(auth);
    setUser(auth.user);
    setToken(auth.accessToken);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const auth = await api<AuthResponse>("/api/v1/auth/login", {
        method: "POST",
        body: { email, password },
      });
      setAuth(auth);
      return auth;
    },
    [setAuth]
  );

  const logout = useCallback(async () => {
    const refreshToken = getRefreshToken();
    try {
      if (refreshToken) {
        await api("/api/v1/auth/logout", {
          method: "POST",
          body: { refreshToken },
        });
      }
    } catch {
      // ignore network logout failures
    } finally {
      clearSession();
      setUser(null);
      setToken(null);
    }
  }, []);

  const value = useMemo(
    () => ({ user, token, ready, login, logout, setAuth }),
    [user, token, ready, login, logout, setAuth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
