# Phase 9 — Integration

Wire domain services with **RabbitMQ events** + light REST (Candidate → Job for skill snapshot).

## Flow

```text
Register → user.registered → Notification inbox
Publish job → job.published → Notification
Apply → application.created → Scoring + Notification
       → score.completed → Candidate (RECRUITER_REVIEW) + Notification
Status change → application.status-changed → Notification
```

## Infra

```bash
docker compose up -d
```

RabbitMQ UI: http://localhost:15672 (`talentpulse` / `talentpulse`)

## Toggle

`talentpulse.events.enabled=false` in tests (Rabbit auto-config excluded).

See [events.md](events.md) for routing keys.
