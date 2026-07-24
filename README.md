# TalentPulse AI

Enterprise recruitment platform: AI scores fit and surfaces skill gaps; **recruiters make the final decision**.

**Repository:** [github.com/Rahull-06/TalentPulse-AI](https://github.com/Rahull-06/TalentPulse-AI)

| | |
|---|---|
| **Live app (frontend)** | _Set after Vercel deploy_ |
| **API (backend)** | _Set after Docker Compose / Render deploy_ |

**Next deploy steps:** [docs/deployment/production.md](docs/deployment/production.md)  
**(Recommended)** Backend: `docker compose -f docker-compose.prod.yml up -d --build` · Frontend: Vercel (`frontend/`)

---

## Folder structure

```text
TalentPulse/
├── frontend/                 # Next.js 15 UI (port 3000)
├── services/                 # Spring Boot microservices (see services/README.md)
│   ├── api-gateway/          # 8080 — single entry for the UI
│   ├── auth-service/         # 8081 — register, login, JWT
│   ├── job-service/          # 8082 — job posts
│   ├── candidate-service/    # 8083 — profiles, apply, resumes
│   ├── scoring-service/      # 8084 — fit scores & skill gaps
│   └── notification-service/ # 8085 — inbox events
├── infra/docker/             # DB init scripts
├── scripts/                  # Local start helpers (Windows)
├── docs/                     # Architecture & runbooks
├── docker-compose.yml        # Postgres + RabbitMQ (local)
└── README.md
```

---

## Tech stack

| Layer | Tech |
|---|---|
| Frontend | Next.js 15, React 19, Tailwind CSS 4 |
| Backend | Java 21, Spring Boot 3.5, Spring Cloud Gateway |
| Data | PostgreSQL 16, Flyway |
| Events | RabbitMQ |
| Auth | JWT (access + refresh) |

---

## Quick start (local)

**1. Infra**

```powershell
docker compose up -d
```

**2. Backend** (JDK 22 recommended; opens 6 terminals)

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-core-backend.ps1
```

**3. Frontend**

```powershell
cd frontend
copy .env.local.example .env.local
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000)

Full guide: [docs/deployment/local-run.md](docs/deployment/local-run.md)

---

## Deploy plan

| Piece | Platform | Why |
|---|---|---|
| `frontend/` | **Vercel** | Best fit for Next.js |
| Java services + Postgres + RabbitMQ | **Render** *or* **Docker** on a VPS | See below |

**Render vs Docker (backend)**

- **Render** — easier first production deploy; managed Postgres; one web service per microservice (or a Docker image). Good when you want less ops.
- **Docker Compose on a VPS** — one compose stack for all services + Postgres + RabbitMQ; closer to local; more control, more setup.

Recommendation for this monorepo: **Vercel (UI) + Render (API)** for the first public deploy; move to a full Docker Compose host later if cost or ops needs change.

Details: [docs/deployment/production.md](docs/deployment/production.md)

---

## Documentation

| Doc | Purpose |
|---|---|
| [services/README.md](services/README.md) | All backend services, ports, routes |
| [docs/deployment/local-run.md](docs/deployment/local-run.md) | Run everything locally |
| [docs/deployment/production.md](docs/deployment/production.md) | Vercel + Render / Docker |
| [docs/architecture/](docs/architecture/) | Service design notes |

---

## License

Private / educational project unless otherwise stated.
