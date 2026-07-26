# TalentPulse — Backend Services

Java **21** / **Spring Boot 3.5** microservices for an AI-assisted hiring platform.  
The browser talks **only** to the API Gateway. Everything else is routed or event-driven.

---

## Architecture (short)

```text
                    ┌─────────────────────────────────────┐
   Next.js (Vercel) │  api-gateway  :8080                  │
                    │  CORS · routing · warmup             │
                    └──────────────┬──────────────────────┘
           ┌───────────┬───────────┼───────────┬───────────┐
           ▼           ▼           ▼           ▼           ▼
        auth:8081   job:8082  candidate:8083 scoring:8084  notification:8085
           │           │           │           │              │
           └───────────┴──── PostgreSQL (Flyway) ─────────────┘
                                 │
                            RabbitMQ
              (application.created → score → inbox)
```

| Principle | How we applied it |
|---|---|
| **Single entry** | UI → Gateway only (`NEXT_PUBLIC_API_URL`) |
| **Bounded contexts** | One deployable service per domain |
| **Async where it hurts sync** | Apply → score → notify via RabbitMQ |
| **Shared auth contract** | Same JWT secret; each service validates locally |
| **Schema ownership** | Flyway migrations per service DB / schema |

---

## Service map

| Service | Port | Owns | API |
|---|---:|---|---|
| **api-gateway** | 8080 | Routing, CORS, `/system/warmup` | `/api/v1/**` |
| **auth-service** | 8081 | Users, roles, JWT, password reset | `/api/v1/auth/**` |
| **job-service** | 8082 | Jobs, publish, openings | `/api/v1/jobs/**` |
| **candidate-service** | 8083 | Profile, PDF resume, applications | `/api/v1/candidates/**`, `/api/v1/applications/**` |
| **scoring-service** | 8084 | Fit score, skill match/gap, interview hints | `/api/v1/scoring/**` |
| **notification-service** | 8085 | In-app inbox | `/api/v1/notifications/**` |

---

## How a hire flow works

```text
1. Recruiter publishes a job          → job-service
2. Candidate uploads PDF resume       → candidate-service (PDFBox parse + skills)
3. Candidate applies                  → candidate-service
4. Event: application.created         → RabbitMQ
5. scoring-service scores             → rule-based (+ Gemini if GEMINI_API_KEY set)
6. Event: score.completed             → candidate + notification
7. Recruiter sees fit % & skill gaps  → decides select / reject
```

Recruiters stay in control — AI/rules **rank and explain**; they don’t auto-hire.

---

## What each service does (1 line)

- **api-gateway** — Spring Cloud Gateway (WebFlux); path-based routes to the five backends.
- **auth-service** — Register / login / refresh; roles `CANDIDATE`, `RECRUITER`, `ADMIN`.
- **job-service** — Draft → publish → close; capacity (openings / applicants).
- **candidate-service** — Profile, PDF-only resume store + parse, apply, rescore on re-upload.
- **scoring-service** — Matched / missing skills + fit score; optional Gemini AI.
- **notification-service** — Inbox from domain events (apply, status, score).

---

## Events (RabbitMQ)

| Routing key | Publisher | Consumers |
|---|---|---|
| `user.registered` | auth | notification |
| `application.created` | candidate | scoring, notification |
| `score.completed` | scoring | candidate, notification |
| `application.status-changed` | candidate | notification |

Details: [`docs/architecture/events.md`](../docs/architecture/events.md)

---

## Stack & standards

| Area | Choice |
|---|---|
| Runtime | Java 21, Spring Boot 3.5.x |
| Gateway | Spring Cloud Gateway (WebFlux) |
| Persistence | PostgreSQL + Flyway + JPA |
| Messaging | Spring AMQP / RabbitMQ |
| Security | JWT (access + refresh), shared secret |
| Docs API | springdoc OpenAPI per service |
| Packaging | Docker (multi-stage Maven → JRE) |

---

## Local run

**1. Infra** (Postgres + RabbitMQ):

```powershell
docker compose up -d
```

**2. All services** (Windows):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-core-backend.ps1
```

**3. Or one service:**

```powershell
mvn -f services\auth-service\pom.xml spring-boot:run
```

Health: `http://localhost:8080/actuator/health`  
Gateway must be up for the frontend (`http://localhost:3000`).

---

## Build

```powershell
mvn -f services\<name>\pom.xml clean package -DskipTests
```

Docker (from repo root):

```powershell
docker build -f services\api-gateway\Dockerfile .
```

---

## Production (Render)

| Piece | Where |
|---|---|
| Blueprint | [`render.yaml`](../render.yaml) |
| Gateway URL | `https://talentpulse-gateway.onrender.com` |
| Frontend | Vercel → `NEXT_PUBLIC_API_URL=<gateway>` |
| Broker | CloudAMQP (env: `RABBITMQ_*`) |
| DB | Render Postgres (shared instance; per-service `DB_NAME`) |

**Notes we learned shipping this:**

- Free web services **cannot receive private network traffic** → gateway uses public `*.onrender.com` URLs.
- Free services **sleep after ~15 min idle** → GitHub Action keep-alive + `/api/v1/system/warmup`.
- Resume files need a **writable dir** (`TALENTPULSE_RESUME_DIR`); parsed text lives in Postgres.

---

## Folder layout

```text
services/
├── api-gateway/
├── auth-service/
├── job-service/
├── candidate-service/
├── scoring-service/
├── notification-service/
└── README.md          ← you are here
```

Each service: `src/main/java` · `application.yml` · `db/migration` (if DB) · `Dockerfile`.

---

## Related docs

| Doc | Purpose |
|---|---|
| [Root README](../README.md) | Full monorepo overview |
| [Local run](../docs/deployment/local-run.md) | Step-by-step local |
| [Production](../docs/deployment/production.md) | Vercel + Render |
| [Architecture](../docs/architecture/) | Deeper design notes |
