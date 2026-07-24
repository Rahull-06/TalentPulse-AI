# Phase 6 — Notification Service

## Business need

Users get notified when important events happen:
applied, shortlisted, rejected, job published, etc.

## This service owns

- In-app `notifications` (inbox)
- `email_logs` (delivery audit)

v1: create notifications via REST (and log “emails”).
Later: RabbitMQ consumers for `APPLICATION_CREATED`, etc.

## Build order

1. Bootstrap ← done
2. Enums ← done
3. Entities ← done
4. Flyway ← done
5. Repositories ← done
6. DTOs ← done
7. Security (JWT) ← done
8. Service ← done
9. Controller ← done
10. Exceptions ← done
11. Tests ← done

**Phase 6 complete.**

## Ports / DB

| | |
|---|---|
| Port | 8085 |
| Database | `talentpulse_notification` |
