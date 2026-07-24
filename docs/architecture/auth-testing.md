# Auth Service — Testing

## Why two kinds of tests?

| Type | File | Speed | What it proves |
|---|---|---|---|
| Unit | `AuthServiceTest` | Fast | Business rules (mocked DB) |
| Integration | `AuthApiIntegrationTest` | Slower | Real HTTP + security + H2 DB |

## Run tests

```bat
cd services\auth-service
mvn test
```

Uses profile `test` → in-memory **H2** (no Docker Postgres needed for tests).

## Flow covered

`register` → `login` → `GET /me` with Bearer token
