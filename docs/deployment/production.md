# Production: Render (API) + Vercel (UI) only

| Piece | Platform |
|---|---|
| Backend (6 services + Postgres) | **Render** |
| Frontend | **Vercel** |
| RabbitMQ | **CloudAMQP** (free addon; Render has no free Rabbit) |

No VPS. No local Docker for hosting. Repo: https://github.com/Rahull-06/TalentPulse-AI

All Java services share **one** Render Postgres database (Flyway history tables are separate per service).

---

## 1) CloudAMQP

1. https://www.cloudamqp.com → Little Lemur (free)  
2. Copy Host / User / Password  

---

## 2) Render Blueprint

1. https://dashboard.render.com → **New → Blueprint**  
2. Repo `Rahull-06/TalentPulse-AI`, branch `main`  
3. Apply `render.yaml`  
4. When prompted, set on every service that asks:

| Variable | Value |
|---|---|
| `RABBITMQ_HOST` | CloudAMQP host |
| `RABBITMQ_USER` | CloudAMQP user |
| `RABBITMQ_PASSWORD` | CloudAMQP password |
| `TALENTPULSE_FRONTEND_URL` | `https://placeholder.vercel.app` (auth; update after Vercel) |

5. Wait for services to finish building  
6. Copy **talentpulse-gateway** URL → open `/actuator/health`

> Free services sleep after idle; first hit can take ~1 minute.

---

## 3) Vercel

1. https://vercel.com/new → import same repo  
2. Root Directory = `frontend`  
3. Env: `NEXT_PUBLIC_API_URL` = `https://YOUR-GATEWAY.onrender.com`  
4. Deploy → copy `https://….vercel.app`

---

## 4) Wire auth

Render → **talentpulse-auth** → Environment:

`TALENTPULSE_FRONTEND_URL` = your Vercel URL → **Manual Deploy**

---

## If a service is red

Open **Logs** on that service, Manual Deploy → Deploy latest commit, paste the error here.
