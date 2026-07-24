# PostgreSQL Setup (Auth Service)

## Why Postgres?

Auth Service stores users, orgs, and tokens in PostgreSQL.  
Config is already in `application.yml`:

- Host: `localhost:5432`
- DB: `talentpulse_auth`
- User / password: `talentpulse` / `talentpulse`

## Recommended path (industry-like): Docker Compose

1. Install **Docker Desktop** for Windows  
2. Start Docker Desktop  
3. From project root:

```bat
docker compose up -d
```

This starts Postgres and creates databases via `infra/docker/init-databases.sql`.

4. Check:

```bat
docker compose ps
```

5. Run Auth Service:

```bat
cd services\auth-service
mvn spring-boot:run
```

Flyway will create tables from `V1__create_auth_tables.sql`.

## Alternative: Install Postgres locally (no Docker)

1. Install PostgreSQL from [postgresql.org](https://www.postgresql.org/download/windows/) or winget  
2. During setup, remember the postgres superuser password  
3. Create role + database (in pgAdmin or `psql`):

```sql
CREATE USER talentpulse WITH PASSWORD 'talentpulse';
CREATE DATABASE talentpulse_auth OWNER talentpulse;
```

4. Then run Auth Service the same way.

## Current machine status

If Docker and `psql` are both missing, install one of the options above before the first run.
