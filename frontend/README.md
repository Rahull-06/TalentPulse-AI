# TalentPulse frontend

Next.js 15 app for candidates and recruiters.

## Local

```powershell
copy .env.local.example .env.local
npm install
npm run dev
```

Open http://localhost:3000 — API defaults to `http://localhost:8080` (gateway).

## Environment

| Variable | Description |
|---|---|
| `NEXT_PUBLIC_API_URL` | Gateway base URL (no trailing slash) |

## Deploy

Deploy the `frontend/` directory to **Vercel**. Set `NEXT_PUBLIC_API_URL` to your public gateway URL.

See [docs/deployment/production.md](../docs/deployment/production.md).
