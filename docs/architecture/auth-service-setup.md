# Auth Service — Dependencies & Setup

## Machine check (done)

| Tool | Status |
|---|---|
| JDK | OK — Java 22 at `C:\Program Files\Java\jdk-22` |
| Maven | Not installed globally → we use a local Maven + Maven Wrapper |
| Docker | Not installed yet (needed later for Postgres) |

We compile as **Java 21** (project standard). JDK 22 can do that.

## What is Maven?

Maven downloads libraries (dependencies) and builds the project.

- `pom.xml` = shopping list of libraries
- `mvnw` / `mvnw.cmd` = Maven Wrapper (same Maven version for everyone)

## Auth Service dependencies (why each one)

| Dependency | Why |
|---|---|
| `spring-boot-starter-web` | REST APIs |
| `spring-boot-starter-security` | Login, roles, password hashing |
| `spring-boot-starter-validation` | Validate request body |
| `spring-boot-starter-data-jpa` | Database access |
| `postgresql` | PostgreSQL driver |
| `flyway-*` | Versioned DB migrations |
| `jjwt-*` | Create/read JWT tokens |
| `springdoc-openapi` | Swagger UI to test APIs |
| `lombok` | Less boilerplate code |
| `spring-boot-starter-test` + `h2` | Tests |

Not added yet (on purpose): Redis, RabbitMQ — when that feature arrives.

## Files created in this step

- `services/auth-service/pom.xml`
- `.../AuthServiceApplication.java`
- `.../application.yml`

## Run later (after Postgres is up)

```bash
cd services/auth-service
.\mvnw.cmd spring-boot:run
```

Port: **8081**
