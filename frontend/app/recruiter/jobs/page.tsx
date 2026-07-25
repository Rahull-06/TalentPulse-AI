"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/AppShell";
import { EmptyState } from "@/components/EmptyState";
import { useAuth } from "@/components/AuthProvider";
import { StatusBadge } from "@/components/StatusBadge";
import { api, ApiError } from "@/lib/api";
import type { Job, PageResponse } from "@/lib/types";

export default function RecruiterJobsPage() {
  const { user, token, ready } = useAuth();
  const router = useRouter();
  const [jobs, setJobs] = useState<Job[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [location, setLocation] = useState("");
  const [requiredSkills, setRequiredSkills] = useState("Java, Spring Boot");
  const [openings, setOpenings] = useState("1");
  const [maxApplicants, setMaxApplicants] = useState("50");
  const [creating, setCreating] = useState(false);
  const [showForm, setShowForm] = useState(false);

  const load = async (access: string) => {
    const page = await api<PageResponse<Job>>("/api/v1/jobs/organization/me?page=0&size=50", {
      token: access,
    });
    setJobs(page.content);
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
        setError(err instanceof ApiError ? err.message : "Could not load jobs");
      } finally {
        setLoading(false);
      }
    };
    void run();
  }, [ready, token, user, router]);

  const onCreate = async (e: FormEvent) => {
    e.preventDefault();
    if (!token) return;
    setCreating(true);
    setError(null);
    try {
      const skills = requiredSkills
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean)
        .map((skillName) => ({ skillName, skillType: "REQUIRED", weight: 1 }));

      const job = await api<Job>("/api/v1/jobs", {
        method: "POST",
        token,
        body: {
          title,
          description,
          location,
          employmentType: "FULL_TIME",
          experienceMin: 0,
          experienceMax: 5,
          currency: "INR",
          openings: openings ? Number(openings) : null,
          maxApplicants: maxApplicants ? Number(maxApplicants) : null,
          skills,
        },
      });
      setTitle("");
      setDescription("");
      setLocation("");
      setOpenings("1");
      setMaxApplicants("50");
      await load(token);
      router.push(`/recruiter/jobs/${job.id}`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create job");
    } finally {
      setCreating(false);
    }
  };

  const publish = async (jobId: string) => {
    if (!token) return;
    try {
      await api(`/api/v1/jobs/${jobId}/publish`, { method: "POST", token });
      await load(token);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Publish failed");
    }
  };

  return (
    <AppShell>
      <div className="tp-fade-up">
        <p className="tp-eyebrow">Recruiter</p>
        <h1 className="tp-display mt-2 text-4xl sm:text-5xl">Pipeline</h1>
        <p className="tp-muted mt-3 max-w-xl text-sm leading-relaxed">
          Create roles, publish when ready, then review applicants with calm focus.
        </p>

        <button
          type="button"
          className="tp-btn tp-btn-soft tp-composer-toggle mt-6 w-full"
          aria-expanded={showForm}
          onClick={() => setShowForm((v) => !v)}
        >
          {showForm ? "Close new role" : "+ New role"}
        </button>

        <form
          onSubmit={onCreate}
          className={`tp-panel tp-composer mt-4 grid gap-4 p-5 sm:mt-8 sm:p-8${
            showForm ? " is-open" : ""
          }`}
        >
          <h2 className="tp-display text-2xl">New role</h2>
          <label className="tp-field">
            <span className="tp-label">Title</span>
            <input className="tp-input" required value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          <label className="tp-field">
            <span className="tp-label">Location</span>
            <input className="tp-input" required value={location} onChange={(e) => setLocation(e.target.value)} />
          </label>
          <label className="tp-field">
            <span className="tp-label">Description</span>
            <textarea
              className="tp-textarea"
              required
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </label>
          <label className="tp-field">
            <span className="tp-label">Required skills (comma-separated)</span>
            <input
              className="tp-input"
              required
              value={requiredSkills}
              onChange={(e) => setRequiredSkills(e.target.value)}
            />
          </label>
          <div className="grid gap-4 sm:grid-cols-2">
            <label className="tp-field">
              <span className="tp-label">Openings (hire count)</span>
              <input
                className="tp-input"
                type="number"
                min={1}
                value={openings}
                onChange={(e) => setOpenings(e.target.value)}
              />
            </label>
            <label className="tp-field">
              <span className="tp-label">Max applicants</span>
              <input
                className="tp-input"
                type="number"
                min={1}
                value={maxApplicants}
                onChange={(e) => setMaxApplicants(e.target.value)}
              />
            </label>
          </div>
          {error ? <p className="tp-error">{error}</p> : null}
          <button className="tp-btn tp-btn-primary w-fit" disabled={creating}>
            {creating ? "Creating…" : "Create draft"}
          </button>
        </form>

        <div className="mt-10">
          <h2 className="tp-display text-2xl">Your jobs</h2>
          {loading ? (
            <p className="tp-muted mt-4 text-sm">Loading…</p>
          ) : jobs.length === 0 ? (
            <div className="mt-4">
              <EmptyState title="No jobs yet" body="Create your first role above." />
            </div>
          ) : (
            <ul className="tp-card-grid mt-4">
              {jobs.map((job) => (
                <li key={job.id} className="min-w-0">
                  <div className="tp-tile">
                    <div className="min-w-0">
                      <Link
                        href={`/recruiter/jobs/${job.id}`}
                        className="tp-tile-title hover:text-[var(--accent)]"
                      >
                        {job.title}
                      </Link>
                      <p className="tp-tile-meta">
                        {job.location}
                        {job.openings ? ` · ${job.openings} opening(s)` : ""}
                        {job.maxApplicants ? ` · max ${job.maxApplicants}` : ""}
                      </p>
                    </div>
                    <div className="tp-tile-foot flex flex-wrap items-center gap-2">
                      <StatusBadge status={job.status} />
                      {job.status === "DRAFT" ? (
                        <button
                          type="button"
                          className="tp-btn tp-btn-soft !min-h-8 !px-2.5 !text-xs"
                          onClick={() => void publish(job.id)}
                        >
                          Publish
                        </button>
                      ) : null}
                      <Link
                        href={`/recruiter/jobs/${job.id}`}
                        className="tp-btn tp-btn-ghost !min-h-8 !px-2.5 !text-xs"
                      >
                        Applicants
                      </Link>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </AppShell>
  );
}
