"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { EmptyState } from "@/components/EmptyState";
import { StatusBadge } from "@/components/StatusBadge";
import { api, formatApiError } from "@/lib/api";
import type { Job, PageResponse } from "@/lib/types";

export default function JobsPage() {
  const [q, setQ] = useState("");
  const [location, setLocation] = useState("");
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = async (query = q, loc = location) => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({ page: "0", size: "20" });
      if (query.trim()) params.set("q", query.trim());
      if (loc.trim()) params.set("location", loc.trim());
      const page = await api<PageResponse<Job>>(`/api/v1/jobs?${params}`);
      setJobs(page.content);
    } catch (err) {
      setError(formatApiError(err, "Could not load jobs"));
      setJobs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onSearch = (e: FormEvent) => {
    e.preventDefault();
    void load();
  };

  return (
    <AppShell>
      <div className="tp-fade-up tp-page-hero">
        <p className="tp-eyebrow">Open roles</p>
        <h1 className="tp-display mt-2 text-[clamp(2rem,6vw,3rem)]">Find your next signal</h1>
        <p className="tp-muted mt-3 max-w-xl text-sm leading-relaxed mx-auto sm:mx-0">
          Browse published roles. Apply when your profile and resume are ready.
        </p>

        <form onSubmit={onSearch} className="tp-search-bar mt-8 grid gap-3 sm:grid-cols-[1fr_180px_auto] text-left">
          <input
            className="tp-input"
            placeholder="Role, skill, keyword"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
          <input
            className="tp-input"
            placeholder="Location"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />
          <button className="tp-btn tp-btn-primary w-full sm:w-auto" type="submit">
            Search
          </button>
        </form>

        {error ? <p className="tp-alert">{error}</p> : null}

        <div className="mt-8">
          {loading ? (
            <p className="tp-muted text-sm">
              Loading roles… if this is the first visit after a while, servers may take up to a minute to wake.
            </p>
          ) : jobs.length === 0 && !error ? (
            <EmptyState
              title="No published roles yet"
              body="Check back soon, or refine your search."
            />
          ) : jobs.length === 0 ? null : (
            <ul className="tp-card-grid">
              {jobs.map((job) => (
                <li key={job.id} className="min-w-0">
                  <Link href={`/jobs/${job.id}`} className="tp-tile group">
                    <div className="min-w-0">
                      <h2 className="tp-tile-title transition-colors group-hover:text-[var(--accent)]">
                        {job.title}
                      </h2>
                      <p className="tp-tile-meta">
                        {[job.location, job.employmentType?.replaceAll("_", " ")]
                          .filter(Boolean)
                          .join(" · ")}
                      </p>
                    </div>
                    <div className="tp-tile-foot">
                      <StatusBadge status={job.status} />
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </AppShell>
  );
}
