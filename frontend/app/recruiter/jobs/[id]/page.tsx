"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { EmptyState } from "@/components/EmptyState";
import { SkeletonBlock } from "@/components/SkeletonBlock";
import { useAuth } from "@/components/AuthProvider";
import { StatusBadge } from "@/components/StatusBadge";
import { api, ApiError } from "@/lib/api";
import type { Application, InterviewQuestions, Job, PageResponse, ScoreResult } from "@/lib/types";

export default function RecruiterJobDetailPage() {
  const params = useParams<{ id: string }>();
  const { user, token, ready } = useAuth();
  const router = useRouter();
  const [job, setJob] = useState<Job | null>(null);
  const [apps, setApps] = useState<Application[]>([]);
  const [scores, setScores] = useState<Record<string, ScoreResult>>({});
  const [questions, setQuestions] = useState<Record<string, InterviewQuestions>>({});
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<string | null>(null);

  const load = async (access: string) => {
    const jobData = await api<Job>(`/api/v1/jobs/${params.id}`, { token: access });
    setJob(jobData);
    const page = await api<PageResponse<Application>>(
      `/api/v1/jobs/${params.id}/applications?page=0&size=50`,
      { token: access }
    );
    setApps(page.content);

    const nextScores: Record<string, ScoreResult> = {};
    const nextQuestions: Record<string, InterviewQuestions> = {};
    await Promise.all(
      page.content.map(async (app) => {
        try {
          nextScores[app.id] = await api<ScoreResult>(`/api/v1/scoring/applications/${app.id}`, {
            token: access,
          });
        } catch {
          // score may not exist yet
        }
        try {
          nextQuestions[app.id] = await api<InterviewQuestions>(
            `/api/v1/scoring/applications/${app.id}/interview-questions`,
            { token: access }
          );
        } catch {
          // questions may not exist yet
        }
      })
    );
    setScores(nextScores);
    setQuestions(nextQuestions);
  };

  useEffect(() => {
    if (!ready) return;
    if (!token || user?.role !== "RECRUITER") {
      router.replace("/login");
      return;
    }
    const run = async () => {
      try {
        await load(token);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Could not load pipeline");
      } finally {
        setLoading(false);
      }
    };
    void run();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ready, token, user, router, params.id]);

  const act = async (applicationId: string, action: "shortlist" | "interview" | "select" | "reject") => {
    if (!token) return;
    setError(null);
    setBusyId(applicationId);
    try {
      let body: Record<string, string> = { note: "Updated from TalentPulse UI" };
      if (action === "reject") {
        const reason =
          window.prompt(
            "Rejection reason (shown to the candidate)",
            "Skills do not sufficiently match this role"
          ) ?? "";
        if (!reason.trim()) return;
        body = { reason: reason.trim() };
      }
      await api(`/api/v1/applications/${applicationId}/${action}`, {
        method: "POST",
        token,
        body,
      });
      await load(token);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Action failed");
    } finally {
      setBusyId(null);
    }
  };

  const generateQuestions = async (app: Application) => {
    if (!token || !job) return;
    setError(null);
    setBusyId(app.id);
    try {
      const score = scores[app.id];
      const data = await api<InterviewQuestions>(
        `/api/v1/scoring/applications/${app.id}/interview-questions`,
        {
          method: "POST",
          token,
          body: {
            jobId: job.id,
            jobTitle: job.title,
            focusSkills: score?.matchedSkills ?? [],
            missingSkills: score?.missingSkills ?? [],
          },
        }
      );
      setQuestions((prev) => ({ ...prev, [app.id]: data }));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not generate questions");
    } finally {
      setBusyId(null);
    }
  };

  return (
    <AppShell>
      <div className="tp-fade-up">
        <Link
          href="/recruiter/jobs"
          className="text-sm font-semibold text-[var(--muted)] transition-colors hover:text-[var(--ink)]"
        >
          ← Pipeline
        </Link>

        {loading ? (
          <div className="mt-6 grid gap-3">
            <SkeletonBlock lines={2} />
            <SkeletonBlock lines={4} />
          </div>
        ) : !job ? (
          <p className="tp-alert mt-6">{error ?? "Not found"}</p>
        ) : (
          <>
            <header className="mt-5 flex flex-wrap items-start justify-between gap-4">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2.5">
                  <h1 className="tp-display text-[clamp(1.85rem,4vw,2.6rem)] capitalize tracking-tight">
                    {job.title}
                  </h1>
                  <StatusBadge status={job.status} />
                </div>
                <p className="tp-muted mt-2 text-sm">{job.location}</p>
                <div className="tp-meta-row mt-3">
                  <span className="tp-meta-chip">
                    {job.openings ? `${job.openings} opening${job.openings === 1 ? "" : "s"}` : "Openings unset"}
                  </span>
                  <span className="tp-meta-chip">
                    {job.maxApplicants ? `Max ${job.maxApplicants} applicants` : "Unlimited applicants"}
                  </span>
                  <span className="tp-meta-chip">{apps.length} applied</span>
                </div>
              </div>
            </header>

            {error ? <p className="tp-alert">{error}</p> : null}

            <section className="mt-10">
              <div className="mb-4 flex items-end justify-between gap-3">
                <h2 className="tp-section-title">Applicants</h2>
                <p className="tp-muted text-xs">{apps.length} total</p>
              </div>

              {apps.length === 0 ? (
                <EmptyState
                  title="No applicants yet"
                  body="Share the published role to start receiving applications."
                />
              ) : (
                <ul className="tp-list">
                  {apps.map((app, index) => {
                    const score = scores[app.id];
                    const q = questions[app.id];
                    const busy = busyId === app.id;
                    return (
                      <li key={app.id} className="tp-list-item">
                        <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-start">
                          <div className="min-w-0">
                            <div className="flex flex-wrap items-center gap-2.5">
                              <h3 className="text-[1.02rem] font-semibold tracking-tight">
                                Applicant {String(index + 1).padStart(2, "0")}
                              </h3>
                              <StatusBadge status={app.status} />
                            </div>
                            <p className="tp-muted mt-1 text-xs">
                              Applied {new Date(app.appliedAt).toLocaleDateString()}
                            </p>

                            {score ? (
                              <div className="tp-score-block">
                                <div className="flex flex-wrap items-end gap-x-3 gap-y-1">
                                  <span className="tp-score-value">{score.fitScore}</span>
                                  <span className="tp-muted pb-0.5 text-xs font-medium uppercase tracking-[0.08em]">
                                    Fit · {score.scoringMode}
                                  </span>
                                </div>
                                {score.matchedSkills?.length ? (
                                  <div className="tp-skill-row">
                                    {score.matchedSkills.map((s) => (
                                      <span key={s} className="tp-skill-pill tp-skill-pill-ok">
                                        {s}
                                      </span>
                                    ))}
                                  </div>
                                ) : null}
                                {score.missingSkills?.length ? (
                                  <div className="tp-skill-row">
                                    {score.missingSkills.map((s) => (
                                      <span key={s} className="tp-skill-pill tp-skill-pill-gap">
                                        Gap · {s}
                                      </span>
                                    ))}
                                  </div>
                                ) : null}
                                {score.explanation ? (
                                  <p className="tp-muted mt-2 text-xs leading-relaxed">{score.explanation}</p>
                                ) : null}
                              </div>
                            ) : (
                              <p className="tp-muted mt-3 text-sm">
                                Fit score pending. It appears after scoring finishes for a new application.
                              </p>
                            )}

                            {q?.questions?.length ? (
                              <div className="mt-4">
                                <p className="text-xs font-semibold uppercase tracking-[0.08em] text-[var(--muted)]">
                                  Interview questions
                                </p>
                                <ol className="mt-2 space-y-1.5 pl-4 text-sm leading-relaxed text-[var(--ink-soft)]">
                                  {q.questions.map((item) => (
                                    <li key={item} className="list-decimal">
                                      {item}
                                    </li>
                                  ))}
                                </ol>
                              </div>
                            ) : (
                              <button
                                type="button"
                                className="tp-btn tp-btn-soft tp-btn-compact mt-4"
                                disabled={busy}
                                onClick={() => void generateQuestions(app)}
                              >
                                {busy ? "Working…" : "Generate interview questions"}
                              </button>
                            )}
                          </div>

                          <div className="tp-actions-stack">
                            <button
                              type="button"
                              className="tp-btn tp-btn-ghost tp-btn-compact"
                              disabled={busy}
                              onClick={() => void act(app.id, "shortlist")}
                            >
                              Shortlist
                            </button>
                            <button
                              type="button"
                              className="tp-btn tp-btn-ghost tp-btn-compact"
                              disabled={busy}
                              onClick={() => void act(app.id, "interview")}
                            >
                              Interview
                            </button>
                            <button
                              type="button"
                              className="tp-btn tp-btn-soft tp-btn-compact"
                              disabled={busy}
                              onClick={() => void act(app.id, "select")}
                            >
                              Select
                            </button>
                            <button
                              type="button"
                              className="tp-btn tp-btn-danger tp-btn-compact"
                              disabled={busy}
                              onClick={() => void act(app.id, "reject")}
                            >
                              Reject
                            </button>
                          </div>
                        </div>
                      </li>
                    );
                  })}
                </ul>
              )}
            </section>
          </>
        )}
      </div>
    </AppShell>
  );
}
