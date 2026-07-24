# Production deploy

TalentPulse is a **monorepo**: Next.js UI + six Spring Boot services + Postgres + RabbitMQ.

| Component | Recommended host | Status |
|---|---|---|
| Frontend (`frontend/`) | [Vercel](https://vercel.com) | Planned |
| Backend services | [Render](https://render.com) **or** Docker on a VPS | Planned |
| Postgres / RabbitMQ | Render managed + Render Redis/Rabbit **or** Compose volumes | Planned |

**Live links** (fill in after first deploy):

- App: _TBD_
- API health: _TBD_ (`https://<gateway>/actuator/health`)
- Repo: https://github.com/Rahull-06/TalentPulse-AI

---

## Why Vercel + Render (first deploy)

| Choice | Best for |
|---|---|
| **Vercel** | Next.js frontend: previews, HTTPS, env vars, zero Node ops |
| **Render** | Spring Boot jars or Docker images; managed Postgres; less server babysitting |

**Docker Compose on a VPS** is better when you want one bill, one network, and parity with `docker compose` locally — but you own OS updates, TLS, and restarts.

**Recommendation:** ship **Vercel + Render** first; revisit full Docker if you outgrow Render’s multi-service layout.

---

## 1) Frontend → Vercel

1. Import `Rahull-06/TalentPulse-AI` in Vercel.
2. **Root Directory:** `frontend`
3. Framework: Next.js (auto)
4. Environment variable:

| Name | Value |
|---|---|
| `NEXT_PUBLIC_API_URL` | `https://<your-gateway-host>` (no trailing slash) |

5. Deploy. Open the Vercel URL and confirm Jobs loads against the gateway.

CORS on the gateway must allow the Vercel origin (e.g. `https://*.vercel.app` and your custom domain).

---

## 2) Backend → Render (suggested layout)

Create (same private network / region):

| Render service | Source | Port |
|---|---|---:|
| `talentpulse-gateway` | `services/api-gateway` | 8080 |
| `talentpulse-auth` | `services/auth-service` | 8081 |
| `talentpulse-job` | `services/job-service` | 8082 |
| `talentpulse-candidate` | `services/candidate-service` | 8083 |
| `talentpulse-scoring` | `services/scoring-service` | 8084 |
| `talentpulse-notification` | `services/notification-service` | 8085 |
| PostgreSQL | Managed | 5432 |
| RabbitMQ | External addon / container | 5672 |

Point gateway env vars at internal service URLs (Render private DNS), for example:

- `TALENTPULSE_AUTH_URL=http://talentpulse-auth:8081`
- `TALENTPULSE_JOB_URL=http://talentpulse-job:8082`
- …same pattern for candidate / scoring / notification

Only **gateway** needs a public URL. Put the same JWT secret on every service that validates tokens.

> Dockerfiles per service can be added next; until then, use a Render **Docker** or **native** Java build that runs `mvn -f services/<name>/pom.xml spring-boot:run` / `java -jar`.

---

## 3) Backend → Docker (alternative)

Add one Dockerfile per service (or a multi-stage root build), then on a VPS:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Local `docker-compose.yml` today only runs **Postgres + RabbitMQ**. Production Compose would also run the six JVMs (or images) behind a reverse proxy (Caddy / Traefik / Nginx) terminating TLS on `:443` → gateway `:8080`.

---

## Checklist before go-live

- [ ] Strong unique `JWT` secret (not the local default)
- [ ] Postgres passwords rotated from `talentpulse` / `talentpulse`
- [ ] RabbitMQ credentials rotated
- [ ] `NEXT_PUBLIC_API_URL` points at HTTPS gateway
- [ ] Gateway CORS allows the Vercel domain
- [ ] Auth `frontend-url` (password reset links) points at the Vercel URL
- [ ] Flyway migrations succeed on empty DBs
- [ ] Health checks: gateway + each service `/actuator/health`

---

## Local vs production

| | Local | Production |
|---|---|---|
| UI | `npm run dev` → `:3000` | Vercel |
| API | `scripts/start-core-backend.ps1` | Render / Docker |
| Infra | `docker compose up -d` | Managed or Compose |

See also: [local-run.md](./local-run.md) · [services/README.md](../../services/README.md)
