# TalentPulse AI — Project Phases (Roadmap)

Use this as your map. We build **step by step**, not everything at once.

---

## Phase 0 — Planning (done)

| Step | Status |
|---|---|
| Understand product & business problem | Done |
| Architecture design (microservices) | Done |
| Database design | Done |
| API design | Done |
| Entry-level folder structure | Done |

---

## Phase 1 — Tooling & Auth bootstrap (done)

| Step | Status |
|---|---|
| JDK check | Done |
| Auth Service `pom.xml` + main class + `application.yml` | Done |
| Install Maven | Done |
| Download Auth dependencies (`mvn dependency:resolve`) | Done |
| Postgres config (`docker-compose.yml` + init SQL) | Done (needs Docker or local Postgres install) |
| Local Postgres running | Done (Docker healthy) |
| Auth Service first run | Done (port 8081) |

---

## Phase 2 — Backend: Auth Service (in progress)

Order inside Auth (same pattern every service):

1. Explain business need  
2. Enums + Entities ← done  
3. Flyway DB migration ← done  
4. Repositories ← done  
5. DTOs ← done  
6. Security (password hash + JWT) ← done  
7. Service ← done  
8. Controller ← done  
9. Exception handling ← done  
10. Tests ← done  
11. Request flow review ← done  

Features: register (candidate/recruiter), login, refresh, logout, forgot/reset password, `/me`

**Phase 2 Auth Service: complete.**

---

## Phase 3 — Backend: Job Service (current)

1. Bootstrap (`pom`, main, yml) ← done  
2. Enums ← done  
3. Entities ← done  
4. Flyway ← done  
5. Repositories ← done  
6. DTOs ← done  
7. Security (validate Auth JWT) ← done  
8. Service ← done  
9. Controller ← done  
10. Exceptions ← done  
11. Tests ← done  

Features: create/edit job, publish, close, search published jobs, list org jobs, job skills

**Phase 3 Job Service: complete.**

---

## Phase 4 — Backend: Candidate Service (current)

1. Bootstrap (`pom`, main, yml) ← done  
2. Enums ← done  
3. Entities ← done  
4. Flyway ← done  
5. Repositories ← done  
6. DTOs ← done  
7. Security (validate Auth JWT) ← done  
8. Service ← done  
9. Controller ← done  
10. Exceptions ← done  
11. Tests ← done  

Features: profile, resume upload, apply, application pipeline, recruiter status actions

**Phase 4 Candidate Service: complete.**

---

## Phase 5 — Backend: Scoring Service (current)

1. Bootstrap (`pom`, main, yml) ← done  
2. Enums ← done  
3. Entities ← done  
4. Flyway ← done  
5. Repositories ← done  
6. DTOs ← done  
7. Security (validate Auth JWT) ← done  
8. Service (rule-based + optional Gemini) ← done  
9. Controller ← done  
10. Exceptions ← done  
11. Tests ← done  

Features: fit score, skill gaps, ranking, interview questions, AI fallback to rules

**Phase 5 Scoring Service: complete.**

---

## Phase 6 — Backend: Notification Service

1. Bootstrap (`pom`, main, yml) ← done  
2. Enums ← done  
3. Entities ← done  
4. Flyway ← done  
5. Repositories ← done  
6. DTOs ← done  
7. Security (validate Auth JWT) ← done  
8. Service ← done  
9. Controller ← done  
10. Exceptions ← done  
11. Tests ← done  

Features: in-app notifications, mark read, email logs (log-only v1)

**Phase 6 Notification Service: complete.** Next: API Gateway (Phase 7).

---

## Phase 5 — Backend: Scoring Service

Fit score, skill gaps, ranking, interview questions, AI + rule-based fallback.

---

## Phase 6 — Backend: Notification Service (summary)

Email + in-app notifications; RabbitMQ consumers later (Phase 9).

---

## Phase 7 — API Gateway

1. Bootstrap (`pom`, main, yml) ← done  
2. Static routes (Auth / Job / Candidate / Scoring / Notification) ← done  
3. CORS for local frontend ← done  
4. Actuator health + route smoke test ← done  

Single entry on port **8080**; JWT pass-through; no Eureka in v1.

**Phase 7 API Gateway: complete.** Next: Frontend (Phase 8).

---

## Phase 8 — Frontend (Next.js)

1. Next.js 15 scaffold + premium minimal design system ← done  
2. Auth (login / candidate / recruiter register) ← done  
3. Jobs browse + apply ← done  
4. Candidate profile + applications ← done  
5. Recruiter pipeline + notifications ← done  
6. Admin placeholder ← done  

Responsive UI with Syne + Manrope, evergreen accent, calm motion.

**Phase 8 Frontend: complete.** Next: Integration (Phase 9).

---

## Phase 9 — Integration

1. RabbitMQ in `docker-compose` ← done  
2. Event contracts (`docs/architecture/events.md`) ← done  
3. Auth / Job publishers ← done  
4. Candidate publish apply/status + score consumer ← done  
5. Scoring consume apply + publish score ← done  
6. Notification consumers ← done  

End-to-end: **apply → score → notify** (plus register/publish notifications).

**Phase 9 Integration: complete.** Next: Testing polish (Phase 10).

---

## Phase 10 — Testing

1. Critical-path unit + API tests (all services) ← done  
2. Fix apply status expectation (`AI_SCORING`) ← done  
3. Extra tests: score-from-event, mark recruiter review, auth refresh ← done  
4. `scripts/run-all-tests.ps1` + Swagger checklist docs ← done  

See [docs/testing/phase-10-testing.md](../testing/phase-10-testing.md).

**Phase 10 Testing: complete.** Next: Docker & Deployment (Phase 11).

---

## Phase 11 — Docker & Deployment

`docker-compose` (Postgres, Redis, RabbitMQ, services), run whole stack locally; deployment notes.

---

## How to read progress in one line

```text
Planning ✓ → Tooling ✓ → Auth ✓ → Job ✓ → Candidate ✓ → Scoring ✓ → Notification ✓
→ Gateway ✓ → Frontend ✓ → Integration ✓ → Tests ✓ → Docker
```

**You are here:** Phase 10 done → Phase 11 Docker & Deployment.
