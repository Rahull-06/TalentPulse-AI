"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { useAuth } from "@/components/AuthProvider";

export function SiteHeader() {
  const { user, ready, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const [accountOpen, setAccountOpen] = useState(false);
  const wrapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setAccountOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (!accountOpen) return;
    const onPointerDown = (e: MouseEvent | TouchEvent) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target as Node)) {
        setAccountOpen(false);
      }
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setAccountOpen(false);
    };
    document.addEventListener("mousedown", onPointerDown);
    document.addEventListener("touchstart", onPointerDown);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onPointerDown);
      document.removeEventListener("touchstart", onPointerDown);
      document.removeEventListener("keydown", onKey);
    };
  }, [accountOpen]);

  const onLogout = async () => {
    setAccountOpen(false);
    await logout();
    router.push("/");
  };

  const isActive = (href: string) =>
    pathname === href || pathname.startsWith(href + "/");

  const links = [
    { href: "/jobs", label: "Jobs", show: true },
    {
      href: "/candidate/applications",
      label: "Applications",
      show: user?.role === "CANDIDATE",
    },
    {
      href: "/candidate/profile",
      label: "Profile",
      show: user?.role === "CANDIDATE",
    },
    {
      href: "/recruiter/jobs",
      label: "Pipeline",
      show: user?.role === "RECRUITER",
    },
    { href: "/notifications", label: "Inbox", show: Boolean(user) },
  ].filter((l) => l.show);

  const initials = user
    ? user.fullName
        .split(" ")
        .filter(Boolean)
        .slice(0, 2)
        .map((p) => p[0]?.toUpperCase() ?? "")
        .join("")
    : "";

  return (
    <header className="tp-header">
      <div className="tp-container tp-header-inner">
        <Link href="/" className="tp-brand">
          TalentPulse
        </Link>

        <nav className="tp-nav-center" aria-label="Main">
          {links.map((l) => (
            <Link
              key={l.href}
              href={l.href}
              className={`tp-nav-link${isActive(l.href) ? " tp-nav-link-active" : ""}`}
            >
              {l.label}
            </Link>
          ))}
        </nav>

        <div className="tp-header-actions" ref={wrapRef}>
          {!ready ? null : user ? (
            <>
              <span className="tp-header-user">{user.fullName.split(" ")[0]}</span>
              <button
                type="button"
                className="tp-btn tp-btn-ghost tp-btn-compact tp-desktop-only"
                onClick={onLogout}
              >
                Sign out
              </button>

              <button
                type="button"
                className="tp-avatar-btn"
                aria-label="Account"
                aria-expanded={accountOpen}
                onClick={() => setAccountOpen((v) => !v)}
              >
                {initials || "?"}
              </button>

              {accountOpen ? (
                <div className="tp-account-sheet" role="menu">
                  <p className="tp-account-name">{user.fullName}</p>
                  <p className="tp-account-role">{user.role.toLowerCase()}</p>
                  <button
                    type="button"
                    className="tp-btn tp-btn-ghost w-full"
                    onClick={onLogout}
                  >
                    Sign out
                  </button>
                </div>
              ) : null}
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="tp-btn tp-btn-ghost tp-btn-compact tp-desktop-only"
              >
                Sign in
              </Link>
              <Link href="/register" className="tp-btn tp-btn-primary tp-btn-compact">
                Get started
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
