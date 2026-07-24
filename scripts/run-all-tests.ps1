# Run all TalentPulse backend tests (Phase 10)
# Usage (from repo root):
#   .\scripts\run-all-tests.ps1

$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME) { 
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-22"
}
$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\Apache\maven\bin;" + $env:Path

$services = @(
    "auth-service",
    "job-service",
    "candidate-service",
    "scoring-service",
    "notification-service",
    "api-gateway"
)

$root = Split-Path -Parent $PSScriptRoot
$failed = @()

foreach ($name in $services) {
    $pom = Join-Path $root "services\$name\pom.xml"
    Write-Host "`n=== Testing $name ===" -ForegroundColor Cyan
    & mvn -f $pom test
    if ($LASTEXITCODE -ne 0) {
        $failed += $name
    }
}

if ($failed.Count -gt 0) {
    Write-Host "`nFAILED: $($failed -join ', ')" -ForegroundColor Red
    exit 1
}

Write-Host "`nAll service tests passed." -ForegroundColor Green
