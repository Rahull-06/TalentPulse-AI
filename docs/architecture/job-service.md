# Phase 3 — Job Service

## Business need

Recruiters create and manage job openings for their company.
Candidates browse **published** jobs.

## This service owns

- Jobs (title, JD, location, salary, status…)
- Job skills (required / preferred)

It does **not** own users/login — Auth Service does.
Job Service only **validates** JWT from Auth (same secret).

## Multi-tenant rule

Every job has `organizationId`.
A recruiter can only manage jobs for **their** organization.

## Build order (same as Auth)

1. Bootstrap (`pom`, main, yml) ← done
2. Enums ← done
3. Entities
4. Flyway
5. Repositories
6. DTOs
7. Security (JWT validate only)
8. Service
9. Controller
10. Exceptions
11. Tests

## Ports / DB

| | |
|---|---|
| Port | 8082 |
| Database | `talentpulse_job` |
