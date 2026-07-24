"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/components/AuthProvider";

export function SiteHeader() {
  const { user, ready, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();

  const onLogout = async () => {
    await logout();
    router.push("/");
  };

  const linkClass = (href: string) => {
    const active = pathname === href || pathname.startsWith(href + "/");
    return `tp-nav-link${active ? " tp-nav-link-active" : ""}`;
  };

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

  return (
    <header className="tp-header">
      <div className="tp-container tp-header-inner">
        <Link href="/" className="tp-brand">
          TalentPulse
        </Link>

        <nav className="tp-nav-center" aria-label="Main">
          {links.map((l) => (
            <Link key={l.href} href={l.href} className={linkClass(l.href)}>
              {l.label}
            </Link>
          ))}
        </nav>

        <div className="tp-header-actions">
          {!ready ? null : user ? (
            <>
              <span className="tp-header-user">{user.fullName.split(" ")[0]}</span>
              <button type="button" className="tp-btn tp-btn-ghost tp-btn-compact" onClick={onLogout}>
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="tp-btn tp-btn-ghost tp-btn-compact">
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
