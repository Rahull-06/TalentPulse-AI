"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { useAuth } from "@/components/AuthProvider";
import { StatusBadge } from "@/components/StatusBadge";
import { api, ApiError } from "@/lib/api";
import type { Application, Job } from "@/lib/types";

export default function JobDetailPage() {
  const params = useParams<{ id: string }>();
  const { user, token, ready } = useAuth();
  const router = useRouter();
  const [job, setJob] = useState<Job | null>(null);
  const [coverLetter, setCoverLetter] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [applying, setApplying] = useState(false);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const data = await api<Job>(`/api/v1/jobs/${params.id}`);
        setJob(data);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Job not found");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [params.id]);

  const onApply = async (e: FormEvent) => {
    e.preventDefault();
    if (!token || !job) return;
    if (!user || user.role !== "CANDIDATE") {
      router.push("/login");
      return;
    }
    setApplying(true);
    setError(null);
    setMessage(null);
    try {
      await api<Application>("/api/v1/applications", {
        method: "POST",
        token,
        body: {
          jobId: job.id,
          organizationId: job.organizationId,
          coverLetter: coverLetter.trim() || undefined,
        },
      });
      setMessage("Application submitted.");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not apply");
    } finally {
      setApplying(false);
    }
  };

  return (
    <AppShell>
      <div className="tp-fade-up mx-auto max-w-3xl">
        {loading ? (
          <p className="tp-muted text-sm">Loading…</p>
        ) : !job ? (
          <p className="tp-error">{error ?? "Not found"}</p>
        ) : (
          <>
            <Link href="/jobs" className="text-sm font-semibold text-[var(--muted)] hover:text-[var(--ink)]">
              ← All roles
            </Link>
            <div className="mt-5 flex flex-wrap items-start justify-between gap-3">
              <h1 className="tp-display text-4xl sm:text-5xl">{job.title}</h1>
              <StatusBadge status={job.status} />
            </div>
            <p className="tp-muted mt-3 text-sm">
              {[job.location, job.employmentType?.replaceAll("_", " ")].filter(Boolean).join(" · ")}
            </p>

            <div className="prose-none mt-8 whitespace-pre-wrap text-[0.98rem] leading-relaxed text-[var(--ink-soft)]">
              {job.description}
            </div>

            {job.skills && job.skills.length > 0 ? (
              <div className="mt-10">
                <h2 className="tp-display text-xl">Skills</h2>
                <ul className="mt-4 flex flex-wrap gap-2">
                  {job.skills.map((s) => (
                    <li key={`${s.skillName}-${s.skillType}`} className="tp-badge tp-badge-neutral">
                      {s.skillName}
                      {s.skillType === "REQUIRED" ? " · required" : ""}
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}

            <hr className="tp-divider mt-10" />

            {ready && user?.role === "CANDIDATE" && job.status === "PUBLISHED" ? (
              <form onSubmit={onApply} className="mt-8 grid gap-4">
                <h2 className="tp-display text-2xl">Apply</h2>
                <label className="tp-field">
                  <span className="tp-label">Cover letter (optional)</span>
                  <textarea
                    className="tp-textarea"
                    value={coverLetter}
                    onChange={(e) => setCoverLetter(e.target.value)}
                    placeholder="A short note on why you’re a fit"
                  />
                </label>
                {error ? <p className="tp-error">{error}</p> : null}
                {message ? <p className="text-sm font-semibold text-[var(--success)]">{message}</p> : null}
                <button className="tp-btn tp-btn-primary w-fit" disabled={applying}>
                  {applying ? "Submitting…" : "Submit application"}
                </button>
                <p className="tp-muted text-xs">
                  Tip: upload a primary resume on your{" "}
                  <Link href="/candidate/profile" className="font-semibold text-[var(--accent)]">
                    profile
                  </Link>{" "}
                  before applying.
                </p>
              </form>
            ) : job.status === "PUBLISHED" ? (
              <div className="mt-8">
                <p className="tp-muted text-sm">Sign in as a candidate to apply.</p>
                <Link href="/login" className="tp-btn tp-btn-primary mt-4 inline-flex">
                  Sign in
                </Link>
              </div>
            ) : null}
          </>
        )}
      </div>
    </AppShell>
  );
}
