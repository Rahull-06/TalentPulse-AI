# Phase 10 — Testing Guide

## What we test

| Layer | Example | Speed |
|---|---|---|
| Unit | `AuthServiceTest`, `RuleBasedScoringEngineTest` | Fast |
| API (MockMvc + H2) | `*ApiIntegrationTest` | Medium |
| Gateway routes | `ApiGatewayApplicationTest` | Fast |
| Manual | Swagger UI per service | Human |

Events are **disabled** in `application-test.yml` so tests do not need RabbitMQ.

---

## Run all backend tests

PowerShell (from repo root):

```powershell
.\scripts\run-all-tests.ps1
```

Or one service:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-22'
mvn -f services\auth-service\pom.xml test
```

Frontend build check:

```powershell
cd frontend
npm run build
```

---

## Critical paths covered

| Flow | Test |
|---|---|
| Register → login → `/me` → refresh | `AuthApiIntegrationTest` |
| Create job → publish → public search | `JobApiIntegrationTest` |
| Profile → resume → apply → recruiter list | `CandidateApiIntegrationTest` |
| Apply → AI_SCORING → mark recruiter review | `ApplicationServiceTest`, `MarkRecruiterReviewTest` |
| Compute score (rules) | `ScoringApiIntegrationTest`, `ScoringServiceComputeFromEventTest` |
| Inbox create / mark read | `NotificationApiIntegrationTest` |
| Gateway routes registered | `ApiGatewayApplicationTest` |

---

## Manual Swagger checklist

Start a service, then open its Swagger UI:

| Service | Port | Swagger |
|---|---|---|
| Auth | 8081 | http://localhost:8081/swagger-ui.html |
| Job | 8082 | http://localhost:8082/swagger-ui.html |
| Candidate | 8083 | http://localhost:8083/swagger-ui.html |
| Scoring | 8084 | http://localhost:8084/swagger-ui.html |
| Notification | 8085 | http://localhost:8085/swagger-ui.html |
| Gateway health | 8080 | http://localhost:8080/actuator/health |

### Smoke checklist (via Gateway `http://localhost:8080`)

1. `POST /api/v1/auth/register/candidate` → tokens  
2. `POST /api/v1/auth/login` → tokens  
3. `GET /api/v1/auth/me` with Bearer token  
4. Recruiter: register → create job → publish  
5. Candidate: profile + resume → apply  
6. Wait a few seconds (RabbitMQ) → `GET /api/v1/scoring/applications/{id}`  
7. `GET /api/v1/notifications/me` → see inbox items  
8. Recruiter: shortlist / reject application  

---

## Interview line

“I use unit tests for business rules, MockMvc + H2 for API contracts, and Swagger for manual smoke. Async events are toggled off in tests so CI stays reliable.”
