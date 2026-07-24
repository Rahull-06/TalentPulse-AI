# Phase 5 — Scoring Service

## Business need

After a candidate applies, TalentPulse computes an **explainable fit score**:
matched skills, missing skills, short summary, ranking for recruiters,
and optional interview questions.

## Critical product rule

```text
AI suggests → Recruiter decides
```

No auto-hire / auto-reject endpoints.

If AI is unavailable → **rule-based scoring** still works and the API says so.

## This service owns

- `score_results` (fit score + explanation)
- `interview_questions`

It reads application/job/resume data via request payload or client calls (v1: request DTO with skills text).

## Build order

1. Bootstrap ← done
2. Enums ← done
3. Entities
4. Flyway
5. Repositories
6. DTOs
7. Security (JWT)
8. Service (rule-based + optional Gemini)
9. Controller
10. Exceptions
11. Tests

## Ports / DB

| | |
|---|---|
| Port | 8084 |
| Database | `talentpulse_scoring` |
