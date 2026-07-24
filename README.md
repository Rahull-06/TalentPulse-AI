# TalentPulse AI

Enterprise Talent Intelligence Platform — AI helps recruiters screen candidates; recruiters make the final hiring decision.

## Folder structure (entry-level)

```text
TalentPulse/
├── docs/
│   ├── api/              # API notes
│   ├── architecture/     # System design notes
│   ├── database/         # DB design notes
│   ├── diagrams/         # Simple architecture diagrams
│   ├── requirements/     # Feature / business requirements
│   └── deployment/       # Deploy notes (later)
├── infra/
│   └── docker/           # Docker-related files for local setup
├── libs/
│   └── common-lib/       # Small shared code (later)
├── services/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── job-service/
│   ├── candidate-service/
│   ├── scoring-service/
│   └── notification-service/
├── frontend/             # Next.js UI (later)
├── scripts/              # Small helper scripts (later)
├── .gitignore
└── README.md
```

`docker-compose.yml` will sit at the **root** when we add infrastructure.
`infra/k8s` and monitoring come **later** — not needed for fresher / entry-level MVP.

Details: [docs/architecture/folder-structure.md](docs/architecture/folder-structure.md)

## Build order

1. Folder structure ← **done**
2. Auth Service bootstrap (pom + config) ← **in progress**
3. Auth features (register / login / JWT)
4. Job → Candidate → Scoring → Notification
5. API Gateway
6. Frontend
7. Tests + Docker

## Prerequisites (your machine)

| Tool | Status |
|---|---|
| JDK 22 | Installed (`C:\Program Files\Java\jdk-22` — we compile as Java 21) |
| Maven | Install with `scripts/install-maven.ps1` |
| Docker | Later (Postgres / Redis / RabbitMQ) |

Auth setup notes: [docs/architecture/auth-service-setup.md](docs/architecture/auth-service-setup.md)

## Tech stack

Java 21, Spring Boot 3, PostgreSQL, Redis, RabbitMQ, Spring AI (Gemini), Next.js, Docker
