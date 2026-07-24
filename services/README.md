# TalentPulse services

Six independent **Spring Boot** apps. The **frontend talks only to the API Gateway** (`8080`). Other services are internal (gateway routes + RabbitMQ events).

```text
Browser ──► api-gateway :8080
               │
               ├── auth-service         :8081
               ├── job-service          :8082
               ├── candidate-service    :8083
               ├── scoring-service      :8084
               └── notification-service :8085

Postgres (per-service DBs) · RabbitMQ (async events)
```

---

## Service map

| Service | Port | Responsibility | Public API prefix |
|---|---:|---|---|
| **api-gateway** | 8080 | Routing, CORS, single UI entry | `/api/v1/**` |
| **auth-service** | 8081 | Register, login, refresh, password reset | `/api/v1/auth/**` |
| **job-service** | 8082 | Job CRUD, publish, openings / capacity | `/api/v1/jobs/**` |
| **candidate-service** | 8083 | Candidate profile, resume, applications | `/api/v1/candidates/**`, `/api/v1/applications/**`, `/api/v1/jobs/*/applications` |
| **scoring-service** | 8084 | Fit score, matched / missing skills, interview hints | `/api/v1/scoring/**` |
| **notification-service** | 8085 | In-app inbox | `/api/v1/notifications/**` |

Shared JWT secret is configured under `talentpulse.security.jwt` in each service that validates tokens.

---

## What each service owns

### api-gateway
- Spring Cloud Gateway (WebFlux)
- Forwards `Authorization` to downstream services
- CORS for `localhost` and private LAN origins (local/dev)

### auth-service
- Users, roles (`CANDIDATE`, `RECRUITER`, `ADMIN`)
- Access + refresh tokens
- Forgot / reset password (reset **link**, not OTP)

### job-service
- Job posts (draft / published / closed)
- Openings and max applicants
- Recruiter pipeline data source for listings

### candidate-service
- Profile + resume upload
- Apply to jobs (enforces capacity)
- Application status history
- Publishes `application-created` events

### scoring-service
- Consumes application events
- Computes fit score + skill gaps
- Publishes score-completed events

### notification-service
- Consumes user / application / status / score events
- Inbox APIs (list, unread count, mark read)

---

## Events (RabbitMQ)

High-level flow after a candidate applies:

```text
candidate-service ──application-created──► scoring-service
                                       └─► notification-service

scoring-service ──score-completed──► candidate-service
                                  └─► notification-service

job/candidate status changes ──► notification-service
```

Queue names and contracts: [docs/architecture/events.md](../docs/architecture/events.md)

---

## Local run

**Infra** (from repo root):

```powershell
docker compose up -d
```

**All six services** (Windows helper):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-core-backend.ps1
```

Or one terminal per service (use **JDK 22**):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-22'
mvn -f services\auth-service\pom.xml spring-boot:run
mvn -f services\job-service\pom.xml spring-boot:run
mvn -f services\candidate-service\pom.xml spring-boot:run
mvn -f services\scoring-service\pom.xml spring-boot:run
mvn -f services\notification-service\pom.xml spring-boot:run
mvn -f services\api-gateway\pom.xml spring-boot:run
```

After Rabbit listener / DTO fixes, purge poison messages and restart event services:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\restart-event-services.ps1
```

---

## Build

Each service is a normal Maven module:

```powershell
mvn -f services\auth-service\pom.xml clean package -DskipTests
```

Repeat for `job-service`, `candidate-service`, `scoring-service`, `notification-service`, `api-gateway`.

---

## Config tips

| Topic | Note |
|---|---|
| Databases | Created by `infra/docker/init-databases.sql` via Compose |
| JWT | Same signing secret across services that verify tokens |
| Java version | Project targets **21**; prefer **JDK 22** locally (Java 25 can break Lombok) |
| Frontend URL | Auth password-reset links use configured frontend base URL |

Per-service design notes live under [`docs/architecture/`](../docs/architecture/).

---

## Production

Gateway is the only service that should be public. Auth / Job / Candidate / Scoring / Notification stay private (Render private network, Docker network, or VPC).

See [docs/deployment/production.md](../docs/deployment/production.md).
