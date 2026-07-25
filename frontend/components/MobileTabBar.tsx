"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/components/AuthProvider";
import {
  BellIcon,
  BriefcaseIcon,
  FileCheckIcon,
  HomeIcon,
  LayersIcon,
  LoginIcon,
  UserIcon,
} from "@/components/NavIcons";

type Tab = {
  href: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  exact?: boolean;
};

/**
 * App-style bottom navigation. Mobile only — hidden from 860px up,
 * where the header nav takes over.
 */
export function MobileTabBar() {
  const { user, ready } = useAuth();
  const pathname = usePathname();

  if (!ready) return null;

  let tabs: Tab[];

  if (!user) {
    tabs = [
      { href: "/", label: "Home", icon: HomeIcon, exact: true },
      { href: "/jobs", label: "Jobs", icon: BriefcaseIcon },
      { href: "/login", label: "Sign in", icon: LoginIcon },
    ];
  } else if (user.role === "CANDIDATE") {
    tabs = [
      { href: "/jobs", label: "Jobs", icon: BriefcaseIcon },
      { href: "/candidate/applications", label: "Applied", icon: FileCheckIcon },
      { href: "/notifications", label: "Inbox", icon: BellIcon },
      { href: "/candidate/profile", label: "Profile", icon: UserIcon },
    ];
  } else {
    tabs = [
      { href: "/jobs", label: "Jobs", icon: BriefcaseIcon },
      { href: "/recruiter/jobs", label: "Pipeline", icon: LayersIcon },
      { href: "/notifications", label: "Inbox", icon: BellIcon },
    ];
  }

  const isActive = (tab: Tab) =>
    tab.exact ? pathname === tab.href : pathname === tab.href || pathname.startsWith(tab.href + "/");

  return (
    <nav className="tp-tabbar" aria-label="Primary">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const active = isActive(tab);
        return (
          <Link
            key={tab.href}
            href={tab.href}
            className={`tp-tab${active ? " is-active" : ""}`}
            aria-current={active ? "page" : undefined}
          >
            <span className="tp-tab-icon">
              <Icon />
            </span>
            <span className="tp-tab-label">{tab.label}</span>
          </Link>
        );
      })}
    </nav>
  );
}
