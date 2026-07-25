"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/AppShell";
import { EmptyState } from "@/components/EmptyState";
import { SkeletonBlock } from "@/components/SkeletonBlock";
import { useAuth } from "@/components/AuthProvider";
import { StatusBadge } from "@/components/StatusBadge";
import { api, ApiError } from "@/lib/api";
import type { Application, Job, PageResponse, ScoreResult } from "@/lib/types";

export default function MyApplicationsPage() {
  const { user, token, ready } = useAuth();
  const router = useRouter();
  const [apps, setApps] = useState<Application[]>([]);
  const [jobs, setJobs] = useState<Record<string, Job>>({});
  const [scores, setScores] = useState<Record<string, ScoreResult>>({});
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!ready) return;
    if (!token || user?.role !== "CANDIDATE") {
      router.replace("/login");
      return;
    }
    const load = async () => {
      try {
        const page = await api<PageResponse<Application>>("/api/v1/applications/me?page=0&size=50", {
          token,
        });
        setApps(page.content);

        const nextJobs: Record<string, Job> = {};
        const nextScores: Record<string, ScoreResult> = {};
        await Promise.all(
          page.content.map(async (app) => {
            try {
              nextJobs[app.jobId] = await api<Job>(`/api/v1/jobs/${app.jobId}`);
            } catch {
              // job may be unavailable
            }
            try {
              nextScores[app.id] = await api<ScoreResult>(`/api/v1/scoring/applications/${app.id}`, {
                token,
              });
            } catch {
              // score may not exist yet
            }
          })
        );
        setJobs(nextJobs);
        setScores(nextScores);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Could not load applications");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [ready, token, user, router]);

  return (
    <AppShell>
      <div className="tp-fade-up">
          <header className="tp-page-head">
          <p className="tp-eyebrow">Candidate</p>
          <h1 className="tp-display text-[clamp(2rem,5vw,2.75rem)]">Applications</h1>
          <p className="tp-muted mt-2.5 max-w-xl text-sm leading-relaxed">
            Track every role you applied to — status, fit score, and skill feedback in one place.
          </p>
        </header>

        {error ? (
          /session expired/i.test(error) ? (
            <p className="tp-notice mb-4">{error}</p>
          ) : (
            <p className="tp-alert !mt-0 mb-4">{error}</p>
          )
        ) : null}

        {loading ? (
          <div className="grid gap-3">
            <SkeletonBlock lines={3} />
            <SkeletonBlock lines={2} />
          </div>
        ) : apps.length === 0 ? (
          <EmptyState
            title="No applications yet"
            body="Browse open roles and apply when your profile and resume are ready."
            action={
              <Link href="/jobs" className="tp-btn tp-btn-primary">
                Browse jobs
              </Link>
            }
          />
        ) : (
          <ul className="tp-list">
            {apps.map((app) => {
              const job = jobs[app.jobId];
              const score = scores[app.id];
              const rejectNote = [...(app.statusHistory ?? [])]
                .reverse()
                .find((h) => h.toStatus === "REJECTED")?.note;

              return (
                <li key={app.id} className="tp-list-item">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2.5">
                        <h2 className="text-[1.05rem] font-semibold tracking-tight text-[var(--ink)]">
                          {job?.title ?? `Role ${app.jobId.slice(0, 8)}`}
                        </h2>
                        <StatusBadge status={app.status} />
                      </div>
                      <div className="tp-meta-row mt-1.5">
                        <span>{job?.location ?? "Location unavailable"}</span>
                        <span aria-hidden>·</span>
                        <span>Applied {new Date(app.appliedAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                    {job ? (
                      <Link href={`/jobs/${job.id}`} className="tp-btn tp-btn-ghost tp-btn-compact">
                        View role
                      </Link>
                    ) : null}
                  </div>

                  {score ? (
                    <div className="tp-score-block">
                      <div className="tp-score-split">
                        <div className="tp-score-dial">
                          <span className="tp-score-dial-value">{score.fitScore}</span>
                          <span className="tp-score-dial-label">Fit</span>
                        </div>

                        <div className="tp-score-body">
                          <span className="tp-muted text-[0.7rem] font-semibold uppercase tracking-[0.1em]">
                            {score.scoringMode === "AI" ? "AI analysis" : "Rule based"}
                          </span>

                          {score.matchedSkills?.length ? (
                            <div className="tp-chip-scroll">
                              {score.matchedSkills.map((s) => (
                                <span key={s} className="tp-skill-pill tp-skill-pill-ok">
                                  {s}
                                </span>
                              ))}
                            </div>
                          ) : null}
                          {score.missingSkills?.length ? (
                            <div className="tp-chip-scroll">
                              {score.missingSkills.map((s) => (
                                <span key={s} className="tp-skill-pill tp-skill-pill-gap">
                                  Gap · {s}
                                </span>
                              ))}
                            </div>
                          ) : null}
                        </div>
                      </div>

                      {score.explanation ? (
                        <p className="tp-muted mt-2.5 text-xs leading-relaxed">{score.explanation}</p>
                      ) : null}
                    </div>
                  ) : null}

                  {app.status === "REJECTED" ? (
                    <div className="tp-notice mt-3 !border-[rgba(139,58,58,0.2)] !bg-[var(--danger-soft)] !text-[var(--danger)]">
                      {rejectNote
                        ? rejectNote
                        : "This application was not selected for the next round."}
                      {score?.missingSkills?.length
                        ? ` Skill gaps: ${score.missingSkills.join(", ")}.`
                        : ""}
                    </div>
                  ) : null}
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </AppShell>
  );
}
