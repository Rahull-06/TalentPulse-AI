# Production: Render (API) + Vercel (UI)

| Piece | Platform | URL |
|---|---|---|
| Backend | **Render** Blueprint (`render.yaml`) | `https://talentpulse-gateway.onrender.com` |
| Frontend | **Vercel** (`frontend/`) | `https://….vercel.app` |
| Repo | GitHub | https://github.com/Rahull-06/TalentPulse-AI |

Do these in order.

---

## 0) One free RabbitMQ (CloudAMQP)

Render has no free RabbitMQ. Create one:

1. [cloudamqp.com](https://www.cloudamqp.com/) → **Little Lemur** (free)
2. Open the instance → copy **Host**, **User**, **Password** (AMQP port `5672`)

Keep this tab open.

---

## 1) Backend → Render

1. [dashboard.render.com](https://dashboard.render.com) → **New** → **Blueprint**
2. Connect **Rahull-06/TalentPulse-AI** (branch `main`)
3. Render reads `render.yaml` → apply
4. When prompted, set the same values on **every** service that asks:

| Variable | Value |
|---|---|
| `RABBITMQ_HOST` | CloudAMQP host |
| `RABBITMQ_USER` | CloudAMQP user |
| `RABBITMQ_PASSWORD` | CloudAMQP password |
| `TALENTPULSE_FRONTEND_URL` | `https://placeholder.vercel.app` (auth only — update after Vercel) |

5. Wait until **talentpulse-db** (Postgres) is available.
6. Open Postgres → **Shell** (or Connect) and run:

```sql
CREATE DATABASE talentpulse_auth;
CREATE DATABASE talentpulse_job;
CREATE DATABASE talentpulse_candidate;
CREATE DATABASE talentpulse_scoring;
CREATE DATABASE talentpulse_notification;
```

7. **Manual Deploy** each Java service once (so they pick up the new DBs), or Blueprint sync redeploy.
8. Open **talentpulse-gateway** → copy its URL, e.g.  
   `https://talentpulse-gateway-xxxx.onrender.com`  
9. Check: `https://YOUR-GATEWAY/actuator/health` → should be UP

> Free web services **sleep** after idle time; first request can take 30–60s while JVMs wake up.

---

## 2) Frontend → Vercel

1. [vercel.com/new](https://vercel.com/new) → Import **Rahull-06/TalentPulse-AI**
2. **Root Directory** = `frontend`
3. Environment variable:

| Name | Value |
|---|---|
| `NEXT_PUBLIC_API_URL` | `https://YOUR-GATEWAY.onrender.com` (no trailing slash) |

4. Deploy → copy `https://….vercel.app`

---

## 3) Wire auth → Vercel URL

On Render → **talentpulse-auth** → Environment:

| Name | Value |
|---|---|
| `TALENTPULSE_FRONTEND_URL` | your `https://….vercel.app` |

Redeploy **talentpulse-auth**.

Open the Vercel site → Jobs should load (allow cold start).

---

## Checklist

- [ ] CloudAMQP created; host/user/pass set on all services  
- [ ] Five `CREATE DATABASE` statements run on Render Postgres  
- [ ] Gateway `/actuator/health` is UP  
- [ ] Vercel `NEXT_PUBLIC_API_URL` = gateway HTTPS URL  
- [ ] Auth `TALENTPULSE_FRONTEND_URL` = Vercel URL  

Gateway CORS already allows `https://*.vercel.app`.

---

## Optional: VPS Docker instead

If you later prefer one server: [previous VPS notes](./production.md) / `docker-compose.prod.yml` (still in the repo).

Local dev: [local-run.md](./local-run.md)
