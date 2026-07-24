import Link from "next/link";
import { AppShell } from "@/components/AppShell";

export default function RegisterIndexPage() {
  return (
    <AppShell>
      <div className="mx-auto max-w-3xl tp-fade-up">
        <p className="tp-eyebrow">Get started</p>
        <h1 className="tp-display mt-2 text-[clamp(2rem,6vw,3rem)]">Choose how you join</h1>
        <p className="tp-muted mt-4 max-w-xl text-sm leading-relaxed">
          One product, two calm paths — candidates find fit, recruiters run the pipeline.
        </p>

        <div className="mt-8 grid gap-4 sm:mt-10 sm:grid-cols-2 sm:gap-5">
          <Link
            href="/register/candidate"
            className="tp-panel group block p-6 transition-[transform,border-color,box-shadow] duration-200 hover:-translate-y-0.5 hover:border-[var(--line-strong)] hover:shadow-[var(--shadow-soft)] sm:p-7"
          >
            <h2 className="tp-display text-2xl">Candidate</h2>
            <p className="tp-muted mt-3 text-sm leading-relaxed">
              Build a profile, apply to roles, and track status without the noise.
            </p>
            <span className="mt-6 inline-block text-sm font-semibold text-[var(--accent)] transition-transform group-hover:translate-x-0.5">
              Continue →
            </span>
          </Link>
          <Link
            href="/register/recruiter"
            className="tp-panel group block p-6 transition-[transform,border-color,box-shadow] duration-200 hover:-translate-y-0.5 hover:border-[var(--line-strong)] hover:shadow-[var(--shadow-soft)] sm:p-7"
          >
            <h2 className="tp-display text-2xl">Recruiter</h2>
            <p className="tp-muted mt-3 text-sm leading-relaxed">
              Create your organization, publish jobs, and review ranked applicants.
            </p>
            <span className="mt-6 inline-block text-sm font-semibold text-[var(--accent)] transition-transform group-hover:translate-x-0.5">
              Continue →
            </span>
          </Link>
        </div>
      </div>
    </AppShell>
  );
}
