# Starts full local backend for UI:
# Auth, Job, Candidate, Scoring, Notification, Gateway
# Requires: Docker Desktop running + `docker compose up -d`
#
# Prefer JDK 22 (Lombok/annotation processing is unreliable on Java 25).

$ErrorActionPreference = "Stop"

$jdk22 = "C:\Program Files\Java\jdk-22"
if (Test-Path "$jdk22\bin\java.exe") {
    $env:JAVA_HOME = $jdk22
} elseif (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = $jdk22
}

$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\Apache\maven\bin;" + $env:Path
$root = Split-Path -Parent $PSScriptRoot
$javaHome = $env:JAVA_HOME
$pathPrefix = "$javaHome\bin;C:\Program Files\Apache\maven\bin;"

Write-Host "Using JAVA_HOME=$javaHome" -ForegroundColor Cyan
# java -version writes to stderr; do not let that abort the script
cmd /c "`"$javaHome\bin\java.exe`" -version 2>&1" | Select-Object -First 1 | ForEach-Object { Write-Host $_ }

function Start-ServiceWindow([string]$name, [string]$pom) {
    Write-Host "Starting $name..." -ForegroundColor Cyan
    $cmd = @"
cd '$root'
`$env:JAVA_HOME='$javaHome'
`$env:Path='$pathPrefix' + `$env:Path
Write-Host '=== $name ===' -ForegroundColor Green
Write-Host \"JAVA_HOME=`$env:JAVA_HOME\"
mvn -f $pom clean spring-boot:run
"@
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
    Start-Sleep -Seconds 2
}

Start-ServiceWindow "Auth (8081)" "services\auth-service\pom.xml"
Start-ServiceWindow "Job (8082)" "services\job-service\pom.xml"
Start-ServiceWindow "Candidate (8083)" "services\candidate-service\pom.xml"
Start-ServiceWindow "Scoring (8084)" "services\scoring-service\pom.xml"
Start-ServiceWindow "Notification (8085)" "services\notification-service\pom.xml"
Start-ServiceWindow "Gateway (8080)" "services\api-gateway\pom.xml"

Write-Host ""
Write-Host "Opened 6 backend windows. Wait ~90s, then use http://localhost:3000" -ForegroundColor Green
Write-Host "Ports: Auth 8081 | Job 8082 | Candidate 8083 | Scoring 8084 | Notification 8085 | Gateway 8080" -ForegroundColor DarkGray
Write-Host "Close any old failed windows first. If Port already in use, that service is already up." -ForegroundColor Yellow
