# Local stack before using the UI

Always start from repo root: `c:\Projects\TalentPulse`

## One-time infra

```powershell
docker compose up -d
```

## Backend (6 services)

**CMD:**

```bat
powershell -ExecutionPolicy Bypass -File .\scripts\start-core-backend.ps1
```

**PowerShell:**

```powershell
.\scripts\start-core-backend.ps1
```

| Service | Port | Needed for |
|---------|------|------------|
| Gateway | 8080 | All UI API calls |
| Auth | 8081 | Sign up / Sign in |
| Job | 8082 | Jobs list / recruiter jobs |
| Candidate | 8083 | Profile, resume, apply |
| Scoring | 8084 | Fit scores after apply |
| Notification | 8085 | Inbox |

If you see **Port already in use**, that service is already running — do not start it again.

Manual (one terminal each):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-22'
mvn -f services\auth-service\pom.xml spring-boot:run
mvn -f services\job-service\pom.xml spring-boot:run
mvn -f services\candidate-service\pom.xml spring-boot:run
mvn -f services\scoring-service\pom.xml spring-boot:run
mvn -f services\notification-service\pom.xml spring-boot:run
mvn -f services\api-gateway\pom.xml spring-boot:run
```

## Frontend

```powershell
cd frontend
npm run dev
```

Open http://localhost:3000

## Tips

- Access tokens expire in **15 minutes**; the UI auto-refreshes via refresh token. If you still see Unauthorized, Sign out → Sign in.
- Inbox needs **Notification (8085)**. Profile/apply need **Candidate (8083)**.
- Keep each service window open while developing.
