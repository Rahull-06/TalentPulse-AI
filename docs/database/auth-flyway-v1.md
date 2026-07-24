# Auth Database — Flyway

## What is Flyway?

Flyway applies **versioned SQL files** to your database.

- File name: `V1__create_auth_tables.sql`
- `V1` = version 1 (runs once)
- Next change = `V2__something.sql` (never edit V1 after it ran in shared envs)

Why not let Hibernate create tables (`ddl-auto: update`)?  
In industry we prefer **migrations** so schema changes are controlled and reviewable.

## Tables in V1

| Table | Purpose |
|---|---|
| `organizations` | Companies (tenants) |
| `users` | Login accounts |
| `refresh_tokens` | Refresh JWT storage (hashed) |
| `password_reset_tokens` | Forgot-password tokens (hashed) |

## When it runs

On app startup, if Postgres is available and Flyway is enabled in `application.yml`.

We still need a real Postgres database named `talentpulse_auth` before the first run.
