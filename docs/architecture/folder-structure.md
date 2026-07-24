# Folder Structure (Interview-Friendly)

Simple **monorepo**: one Git repo, multiple small services.

---

## Root folders (what to say in interviews)

| Folder | One-line explanation |
|---|---|
| `docs/` | Project documentation |
| `infra/docker/` | Local Docker setup files |
| `libs/common-lib/` | Shared helpers (optional, later) |
| `services/` | Backend microservices |
| `frontend/` | Next.js UI |
| `scripts/` | Utility scripts |

We keep `k8s` / Prometheus out for now so the project stays explainable.

---

## Docs

| Folder | Use |
|---|---|
| `requirements/` | What the product must do |
| `architecture/` | How services connect |
| `database/` | Tables and relationships |
| `api/` | Endpoint contracts |
| `diagrams/` | Pictures for interviews/README |
| `deployment/` | How to run/deploy (later) |

---

## Services

| Service | Responsibility |
|---|---|
| `api-gateway` | Single entry point for frontend |
| `auth-service` | Login, JWT, users, organizations |
| `job-service` | Job create / publish / close |
| `candidate-service` | Profile, resume, apply |
| `scoring-service` | Fit score, ranking, questions |
| `notification-service` | Email + in-app notifications |

---

## Inside one service (simple layers)

Example: `auth-service`

```text
auth-service/
└── src/main/java/com/talentpulse/auth/
    ├── config/        # Spring settings
    ├── controller/    # REST APIs
    ├── service/       # Business logic
    ├── repository/    # Database access
    ├── entity/        # DB tables
    ├── dto/           # Request/response objects
    ├── security/      # JWT / password (Auth only)
    └── exception/     # Error handling
```

**Interview line:**
“I follow layered architecture — Controller → Service → Repository — and never expose entities directly; I use DTOs.”

Gateway only needs `config/` for now (routing, not a full CRUD app).

---

## What we removed on purpose

Too many nested packages (`mapper/`, `event/publisher/`, `client/`, `dto/request/`, `service/impl/`, etc.).

We can add a folder **when a feature needs it**. That keeps the project clean for freshers and still looks industry-standard.
