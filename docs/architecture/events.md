# Phase 9 — Event contracts (RabbitMQ)

## Topology

| Piece | Value |
|---|---|
| Exchange | `talentpulse.events` (topic) |
| Broker | `localhost:5672` user/pass `talentpulse` |
| UI | http://localhost:15672 |

## Routing keys

| Key | Publisher | Consumers |
|---|---|---|
| `user.registered` | Auth | Notification |
| `job.published` | Job | Notification |
| `application.created` | Candidate | Scoring, Notification |
| `application.status-changed` | Candidate | Notification |
| `score.completed` | Scoring | Candidate, Notification |

## End-to-end flow

```text
Apply → APPLICATION_CREATED
     → Scoring computes fit
     → SCORE_COMPLETED
     → Candidate status → RECRUITER_REVIEW
     → Notification inbox updated
```

## Toggle

`talentpulse.events.enabled=false` disables Rabbit (used in unit/API tests).
