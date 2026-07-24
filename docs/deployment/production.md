# Production: VPS Docker (backend) + Vercel (frontend)

| Piece | Where | Public URL |
|---|---|---|
| UI | Vercel | `https://….vercel.app` |
| API | Your VPS (Docker + Caddy) | `https://api.yourdomain.com` |
| Repo | GitHub | https://github.com/Rahull-06/TalentPulse-AI |

**Why HTTPS on the VPS?** Vercel is HTTPS. Browsers block `https` pages calling `http` APIs (mixed content). Caddy gets a free Let's Encrypt cert for your API domain.

---

## What you need

1. A **VPS** (Ubuntu 22.04+, 2 GB RAM minimum; 4 GB better for 6 JVMs) — e.g. Hetzner, DigitalOcean, Linode, AWS Lightsail  
2. A **domain** (or subdomain) with an **A record** → VPS public IP (example: `api.yourdomain.com`)  
3. Accounts: GitHub (done), [Vercel](https://vercel.com)

---

## Step 1 — VPS backend

SSH into the server, then:

```bash
# Docker Engine + Compose plugin (Ubuntu)
sudo apt update && sudo apt install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
# log out/in once so docker works without sudo

git clone https://github.com/Rahull-06/TalentPulse-AI.git
cd TalentPulse-AI

cp .env.prod.example .env
nano .env   # set API_DOMAIN, JWT_SECRET, passwords
```

`.env` must include:

```env
API_DOMAIN=api.yourdomain.com
JWT_SECRET=...long-random...
POSTGRES_PASSWORD=...
RABBITMQ_PASSWORD=...
TALENTPULSE_FRONTEND_URL=https://placeholder.vercel.app
```

Open firewall (if `ufw`):

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable
```

DNS: create **A** record `api.yourdomain.com` → VPS IP. Wait until it resolves.

Start stack (first build takes several minutes):

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
docker compose -f docker-compose.prod.yml ps
curl -sS https://api.yourdomain.com/actuator/health
```

Expect `{"status":"UP"}` (or similar).

---

## Step 2 — Vercel frontend

1. [vercel.com/new](https://vercel.com/new) → Import **Rahull-06/TalentPulse-AI**
2. **Root Directory** = `frontend`
3. Environment variable:

| Name | Value |
|---|---|
| `NEXT_PUBLIC_API_URL` | `https://api.yourdomain.com` (no trailing slash) |

4. Deploy  
5. Copy the deployment URL (`https://something.vercel.app`)

---

## Step 3 — Wire auth ↔ frontend

On the VPS, edit `.env`:

```env
TALENTPULSE_FRONTEND_URL=https://something.vercel.app
```

Restart auth (picks up reset-password links):

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d auth
```

Open the Vercel URL → Jobs should load from your API.

---

## Useful commands (VPS)

```bash
# Logs
docker compose -f docker-compose.prod.yml logs -f gateway
docker compose -f docker-compose.prod.yml logs -f auth

# Rebuild after git pull
git pull
docker compose -f docker-compose.prod.yml --env-file .env up -d --build

# Stop
docker compose -f docker-compose.prod.yml down
```

---

## Checklist

- [ ] DNS A record for `API_DOMAIN` points at VPS  
- [ ] `https://API_DOMAIN/actuator/health` is UP  
- [ ] Vercel `NEXT_PUBLIC_API_URL` = that HTTPS API  
- [ ] `TALENTPULSE_FRONTEND_URL` = Vercel URL  
- [ ] Strong secrets in `.env` (not the example defaults)

---

## Render (optional alternative)

See older notes in git history / `render.yaml` if you prefer Render instead of a VPS. For this monorepo, **VPS Docker + Vercel** is the smoother path.

Local dev remains: [local-run.md](./local-run.md)
