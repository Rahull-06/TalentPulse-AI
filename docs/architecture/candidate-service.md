# Phase 4 — Candidate Service

## Business need

Candidates complete a profile, upload a resume, and apply to published jobs.
Recruiters view applications for their org and move status (shortlist/reject/…).

## This service owns

- Candidate profile
- Resumes (file + parse status)
- Candidate skills
- Applications + status history

It does **not** own jobs (Job Service) or fit scores (Scoring Service).
`jobId` / `organizationId` are reference UUIDs only.

## Pipeline

```text
APPLIED → SCREENING → AI_SCORING → RECRUITER_REVIEW
       → SHORTLISTED → INTERVIEW → SELECTED | REJECTED
```

## Build order

1. Bootstrap ← done
2. Enums ← done
3. Entities
4. Flyway
5. Repositories
6. DTOs
7. Security (JWT)
8. Service
9. Controller
10. Exceptions
11. Tests

## Ports / DB

| | |
|---|---|
| Port | 8083 |
| Database | `talentpulse_candidate` |
