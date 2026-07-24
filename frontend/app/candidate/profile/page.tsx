"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/AppShell";
import { useAuth } from "@/components/AuthProvider";
import { api, ApiError } from "@/lib/api";
import type { CandidateProfile } from "@/lib/types";

export default function CandidateProfilePage() {
  const { user, token, ready } = useAuth();
  const router = useRouter();
  const [profile, setProfile] = useState<CandidateProfile | null>(null);
  const [headline, setHeadline] = useState("");
  const [summary, setSummary] = useState("");
  const [experienceYears, setExperienceYears] = useState("");
  const [location, setLocation] = useState("");
  const [phone, setPhone] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    if (!ready) return;
    if (!token || user?.role !== "CANDIDATE") {
      router.replace("/login");
      return;
    }
    const load = async () => {
      try {
        const data = await api<CandidateProfile>("/api/v1/candidates/me", { token });
        setProfile(data);
        setHeadline(data.headline ?? "");
        setSummary(data.summary ?? "");
        setExperienceYears(data.experienceYears?.toString() ?? "");
        setLocation(data.location ?? "");
        setPhone(data.phone ?? "");
      } catch (err) {
        const msg = err instanceof ApiError ? err.message : "Could not load profile";
        setError(msg);
        if (err instanceof ApiError && err.status === 401) {
          router.replace("/login");
        }
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [ready, token, user, router]);

  const onSave = async (e: FormEvent) => {
    e.preventDefault();
    if (!token) return;
    setSaved(false);
    setError(null);
    try {
      const data = await api<CandidateProfile>("/api/v1/candidates/me", {
        method: "PUT",
        token,
        body: {
          headline: headline || null,
          summary: summary || null,
          experienceYears: experienceYears ? Number(experienceYears) : null,
          location: location || null,
          phone: phone || null,
        },
      });
      setProfile(data);
      setSaved(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Save failed");
    }
  };

  const onUpload = async (file: File | null) => {
    if (!file || !token) return;
    setUploading(true);
    setError(null);
    try {
      const form = new FormData();
      form.append("file", file);
      await api("/api/v1/candidates/me/resumes", { method: "POST", token, formData: form });
      setSaved(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Upload failed");
    } finally {
      setUploading(false);
    }
  };

  return (
    <AppShell>
      <div className="tp-fade-up mx-auto max-w-2xl">
        <p className="tp-eyebrow">Candidate</p>
        <h1 className="tp-display mt-2 text-4xl">Your profile</h1>
        <p className="tp-muted mt-3 text-sm">Keep this lean — recruiters scan for clarity.</p>

        {loading ? (
          <p className="tp-muted mt-8 text-sm">Loading…</p>
        ) : (
          <form onSubmit={onSave} className="tp-panel mt-8 grid gap-4 p-6 sm:p-8">
            <label className="tp-field">
              <span className="tp-label">Headline</span>
              <input className="tp-input" value={headline} onChange={(e) => setHeadline(e.target.value)} />
            </label>
            <label className="tp-field">
              <span className="tp-label">Summary</span>
              <textarea className="tp-textarea" value={summary} onChange={(e) => setSummary(e.target.value)} />
            </label>
            <div className="grid gap-4 sm:grid-cols-2">
              <label className="tp-field">
                <span className="tp-label">Years of experience</span>
                <input
                  className="tp-input"
                  type="number"
                  min={0}
                  value={experienceYears}
                  onChange={(e) => setExperienceYears(e.target.value)}
                />
              </label>
              <label className="tp-field">
                <span className="tp-label">Location</span>
                <input className="tp-input" value={location} onChange={(e) => setLocation(e.target.value)} />
              </label>
            </div>
            <label className="tp-field">
              <span className="tp-label">Phone</span>
              <input className="tp-input" value={phone} onChange={(e) => setPhone(e.target.value)} />
            </label>

            <label className="tp-field">
              <span className="tp-label">Resume (PDF / DOC / DOCX)</span>
              <input
                className="tp-input"
                type="file"
                accept=".pdf,.doc,.docx"
                onChange={(e) => void onUpload(e.target.files?.[0] ?? null)}
              />
              {uploading ? <span className="tp-muted text-xs">Uploading…</span> : null}
            </label>

            {error ? <p className="tp-error">{error}</p> : null}
            {saved ? <p className="text-sm font-semibold text-[var(--success)]">Saved.</p> : null}
            {profile ? (
              <p className="tp-muted text-xs">Profile id: {profile.id}</p>
            ) : null}
            <button className="tp-btn tp-btn-primary mt-2 w-fit" type="submit">
              Save profile
            </button>
          </form>
        )}
      </div>
    </AppShell>
  );
}
