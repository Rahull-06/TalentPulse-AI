# Production deploy

TalentPulse is a **monorepo**: Next.js UI + six Spring Boot services + Postgres + RabbitMQ.

| Component | Platform | Link |
|---|---|---|
| Frontend | **Vercel** | Deploy `frontend/` |
| Backend | **Docker Compose (VPS)** *or* **Render Blueprint** | See below |
| Repo | GitHub | https://github.com/Rahull-06/TalentPulse-AI |

**Recommendation for this architecture:** run the backend with **`docker-compose.prod.yml`** on a small VPS (or your PC for a demo). Use **Vercel** for the UI. Render works too, but needs paid private services + external RabbitMQ (CloudAMQP).

---

## A) Frontend → Vercel (do this after API URL exists)

1. Open [vercel.com/new](https://vercel.com/new) → import **Rahull-06/TalentPulse-AI**
2. **Root Directory** → `frontend` (Configure → edit)
3. Framework: Next.js (auto)
4. Environment variable:

| Name | Value |
|---|---|
| `NEXT_PUBLIC_API_URL` | `https://YOUR_GATEWAY_HOST` (no trailing slash) |

5. Deploy → copy the `*.vercel.app` URL  
6. Set that URL as `TALENTPULSE_FRONTEND_URL` on **auth-service** (password-reset links)

Gateway CORS already allows `https://*.vercel.app`.

---

## B) Backend → Docker Compose (recommended)

On a VPS with Docker (or locally to smoke-test production images):

```bash
git clone https://github.com/Rahull-06/TalentPulse-AI.git
cd TalentPulse-AI
# optional: export JWT_SECRET=... POSTGRES_PASSWORD=... RABBITMQ_PASSWORD=... TALENTPULSE_FRONTEND_URL=https://your-app.vercel.app
docker compose -f docker-compose.prod.yml up -d --build
```

- API: `http://YOUR_SERVER:8080/actuator/health`
- Put a reverse proxy (Caddy/Nginx) with HTTPS in front of `:8080` for production
- Point Vercel `NEXT_PUBLIC_API_URL` at that HTTPS gateway URL

This file builds all six services + Postgres + RabbitMQ on one network.

---

## C) Backend → Render Blueprint (alternative)

1. Create a free [CloudAMQP](https://www.cloudamqp.com/) instance (Little Lemur) — copy host, user, password, port `5672`
2. Open [Render Dashboard](https://dashboard.render.com) → **New** → **Blueprint**
3. Connect `Rahull-06/TalentPulse-AI` (uses root `render.yaml`)
4. When prompted (`sync: false` vars), set for **each** Java private service:

| Variable | Example |
|---|---|
| `TALENTPULSE_FRONTEND_URL` | `https://your-app.vercel.app` (auth only) |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST:5432/talentpulse_auth` (change DB name per service) |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | From Render Postgres |
| `RABBITMQ_HOST` / `USER` / `PASSWORD` | From CloudAMQP |

5. Create the five logical databases on the Render Postgres once:

```sql
CREATE DATABASE talentpulse_auth;
CREATE DATABASE talentpulse_job;
CREATE DATABASE talentpulse_candidate;
CREATE DATABASE talentpulse_scoring;
CREATE DATABASE talentpulse_notification;
```

6. After deploy, copy the **gateway** public URL → Vercel `NEXT_PUBLIC_API_URL`

> Private services (`pserv`) on Render use the **starter** plan (not free). Free alternative: use Docker Compose on a VPS (section B).

---

## Checklist

- [ ] Strong `JWT_SECRET` (same on every service that validates JWT)
- [ ] Postgres passwords not left as `talentpulse` in public deploys
- [ ] `NEXT_PUBLIC_API_URL` = HTTPS gateway
- [ ] Auth `TALENTPULSE_FRONTEND_URL` = Vercel URL
- [ ] Gateway CORS allows your frontend origin
- [ ] `/actuator/health` on gateway returns UP
- [ ] Jobs page loads from the Vercel URL

---

## Local vs production

| | Local | Production |
|---|---|---|
| UI | `npm run dev` | Vercel |
| API | `scripts/start-core-backend.ps1` | `docker-compose.prod.yml` or Render |
| Infra | `docker compose up -d` | Included in prod compose / Render+CloudAMQP |

See also: [local-run.md](./local-run.md) · [services/README.md](../../services/README.md)
