# Phase 7 — API Gateway

## Business need

Frontend talks to **one** URL (port 8080).  
Gateway routes each path to the right microservice and forwards the JWT header.

## What this service does

| Concern | v1 approach |
|---|---|
| Routing | Spring Cloud Gateway static routes |
| JWT | Pass-through (`Authorization` forwarded; each service validates) |
| CORS | Allowed for local Next.js (`localhost` / `127.0.0.1`) |
| Discovery | None yet (no Eureka) — URIs via env vars |

## Routes

| Path | Target (default) |
|---|---|
| `/api/v1/auth/**` | `http://localhost:8081` |
| `/api/v1/jobs/*/applications` | `http://localhost:8083` (candidate) |
| `/api/v1/jobs/**` | `http://localhost:8082` |
| `/api/v1/candidates/**` | `http://localhost:8083` |
| `/api/v1/applications/**` | `http://localhost:8083` |
| `/api/v1/scoring/**` | `http://localhost:8084` |
| `/api/v1/notifications/**` | `http://localhost:8085` |

Override targets with env vars: `TALENTPULSE_AUTH_URL`, `TALENTPULSE_JOB_URL`, etc.

## Ports

| | |
|---|---|
| Gateway | **8080** |
| Health | `GET /actuator/health` |

## Run locally

```text
1. Start Postgres + domain services (8081–8085)
2. mvn spring-boot:run -f services/api-gateway/pom.xml
3. Call http://localhost:8080/api/v1/...
```

## Interview line

“The API Gateway is the single entry point. It routes by path and does not own business logic — Auth still issues JWTs; each service validates them.”
