# Restart event-related services after Rabbit listener fixes.
# Run from repo root in PowerShell (or: powershell -ExecutionPolicy Bypass -File .\scripts\restart-event-services.ps1)

$ErrorActionPreference = "Stop"
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "C:\Program Files\Java\jdk-22" }
$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\Apache\maven\bin;" + $env:Path
$root = Split-Path -Parent $PSScriptRoot

foreach ($port in 8082, 8083, 8084, 8085) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) {
        Write-Host "Stopping PID $($conn.OwningProcess) on $port"
        Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
    }
}

Start-Sleep -Seconds 2

docker exec talentpulse-rabbitmq rabbitmqctl purge_queue notification.status-changed | Out-Null
docker exec talentpulse-rabbitmq rabbitmqctl purge_queue scoring.application-created | Out-Null
docker exec talentpulse-rabbitmq rabbitmqctl purge_queue notification.application-created | Out-Null
docker exec talentpulse-rabbitmq rabbitmqctl purge_queue candidate.score-completed | Out-Null
docker exec talentpulse-rabbitmq rabbitmqctl purge_queue notification.score-completed | Out-Null
docker exec talentpulse-rabbitmq rabbitmqctl purge_queue notification.user-registered | Out-Null

function Start-Svc([string]$name, [string]$pom) {
    Write-Host "Starting $name..." -ForegroundColor Cyan
    $cmd = @"
cd '$root'
`$env:JAVA_HOME='$env:JAVA_HOME'
`$env:Path='$env:JAVA_HOME\bin;C:\Program Files\Apache\maven\bin;' + `$env:Path
Write-Host '=== $name ===' -ForegroundColor Green
mvn -f $pom spring-boot:run
"@
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
    Start-Sleep -Seconds 2
}

Start-Svc "Job (8082)" "services\job-service\pom.xml"
Start-Svc "Candidate (8083)" "services\candidate-service\pom.xml"
Start-Svc "Scoring (8084)" "services\scoring-service\pom.xml"
Start-Svc "Notification (8085)" "services\notification-service\pom.xml"

Write-Host ""
Write-Host "Restarted Job/Candidate/Scoring/Notification. Keep Auth + Gateway running." -ForegroundColor Green
Write-Host "Then: candidate re-applies -> score appears; shortlist/reject -> candidate inbox updates." -ForegroundColor DarkGray
