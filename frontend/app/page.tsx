"use client";

import Link from "next/link";
import { MobileTabBar } from "@/components/MobileTabBar";
import { SiteHeader } from "@/components/SiteHeader";

export default function HomePage() {
  return (
    <div className="tp-app">
      <SiteHeader />

      <section className="tp-hero-viewport relative overflow-hidden">
        <div
          aria-hidden
          className="absolute inset-0 bg-[linear-gradient(125deg,#152722_0%,#1f3832_36%,#263532_70%,#171e1c_100%)]"
        />
        <div
          aria-hidden
          className="absolute inset-0 opacity-[0.18]"
          style={{
            backgroundImage:
              "radial-gradient(circle at 20% 20%, rgba(255,255,255,0.28) 0 1px, transparent 1.5px), radial-gradient(circle at 80% 40%, rgba(255,255,255,0.12) 0 1px, transparent 1.5px)",
            backgroundSize: "28px 28px, 42px 42px",
          }}
        />
        <div
          aria-hidden
          className="pointer-events-none absolute -right-16 top-10 h-64 w-64 rounded-full bg-[rgba(232,238,235,0.06)] blur-3xl sm:h-96 sm:w-96"
        />
        <svg
          aria-hidden
          className="tp-pulse-line pointer-events-none absolute inset-x-0 top-[48%] h-28 w-[150%] -translate-x-[15%] opacity-60 sm:h-40 sm:w-[140%] sm:-translate-x-[12%]"
          viewBox="0 0 1200 160"
          fill="none"
        >
          <path
            d="M0 90 C120 90 140 40 220 40 C300 40 320 120 400 120 C480 120 500 55 580 55 C660 55 680 110 760 110 C840 110 860 70 940 70 C1020 70 1040 100 1120 100 L1200 100"
            stroke="rgba(232,238,235,0.55)"
            strokeWidth="1.5"
          />
          <path
            d="M0 100 C130 100 150 60 230 60 C310 60 330 130 410 130 C490 130 510 75 590 75 C670 75 690 118 770 118 C850 118 870 82 950 82 C1030 82 1050 108 1130 108 L1200 108"
            stroke="rgba(232,238,235,0.28)"
            strokeWidth="1"
          />
        </svg>

        <div className="tp-hero-viewport tp-container relative z-10 flex flex-col justify-center py-16 sm:pb-24 sm:pt-10">
          <p className="tp-fade-up tp-eyebrow text-[rgba(232,238,235,0.72)]">TalentPulse</p>
          <h1 className="tp-fade-up-delay tp-display mt-3 max-w-3xl text-[clamp(2.4rem,9vw,5.4rem)] text-[#f4f6f4]">
            Hire with signal,
            <br />
            not noise.
          </h1>
          <p className="tp-fade-up-delay-2 mt-4 max-w-xl text-[0.98rem] leading-relaxed text-[rgba(232,238,235,0.72)] sm:mt-5 sm:text-[1.05rem]">
            Fit scores, skill gaps, and interview questions — AI assists, recruiters decide.
          </p>
          <div className="tp-fade-up-delay-2 mt-8 flex w-full max-w-sm flex-col gap-3 sm:mt-9 sm:max-w-none sm:w-auto sm:flex-row">
            <Link href="/register" className="tp-btn tp-btn-on-dark w-full sm:w-auto">
              Start free
            </Link>
            <Link href="/jobs" className="tp-btn tp-btn-ghost-on-dark w-full sm:w-auto">
              Browse roles
            </Link>
          </div>
        </div>
      </section>

      <section className="tp-container tp-page-tail pt-16 sm:pt-24">
        <div className="max-w-2xl">
          <p className="tp-eyebrow">What it does</p>
          <h2 className="tp-display mt-3 text-[clamp(2rem,5vw,3rem)]">
            One calm workspace for serious hiring.
          </h2>
          <p className="tp-muted mt-4 text-sm leading-relaxed sm:text-base">
            Candidates apply with clarity. Recruiters see ranked fit and gaps — never an auto-hire.
          </p>
        </div>

        <div className="mt-10 grid gap-8 border-t border-[var(--line)] pt-10 sm:mt-14 sm:gap-10 md:grid-cols-3">
          {[
            {
              title: "Signal first",
              body: "Rule-based and AI scoring explain matched and missing skills in plain language.",
            },
            {
              title: "Human control",
              body: "Shortlist, interview, select, or reject — every status change is yours.",
            },
            {
              title: "Quiet inbox",
              body: "In-app notifications keep candidates and recruiters aligned without clutter.",
            },
          ].map((item) => (
            <div key={item.title} className="min-w-0">
              <h3 className="tp-display text-xl">{item.title}</h3>
              <p className="tp-muted mt-3 text-sm leading-relaxed">{item.body}</p>
            </div>
          ))}
        </div>
      </section>

      <MobileTabBar />
    </div>
  );
}
