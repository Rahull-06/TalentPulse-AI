# Installs Apache Maven into TalentPulse/tools (no admin required).
# Run:
#   powershell -ExecutionPolicy Bypass -File scripts/install-maven.ps1

$ErrorActionPreference = "Stop"
$mavenVersion = "3.9.9"
$root = Split-Path -Parent $PSScriptRoot
$tools = Join-Path $root "tools"
$zip = Join-Path $tools "apache-maven-$mavenVersion-bin.zip"
$mavenHome = Join-Path $tools "apache-maven-$mavenVersion"
$url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"

New-Item -ItemType Directory -Force -Path $tools | Out-Null

if (-not (Test-Path (Join-Path $mavenHome "bin\mvn.cmd"))) {
    Write-Host "Downloading Maven $mavenVersion ..."
    Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
    Write-Host "Extracting to $tools ..."
    Expand-Archive -Path $zip -DestinationPath $tools -Force
    Remove-Item $zip -Force
} else {
    Write-Host "Maven already present at $mavenHome"
}

$env:JAVA_HOME = "C:\Program Files\Java\jdk-22"
if (-not (Test-Path $env:JAVA_HOME)) {
    Write-Host "WARNING: JAVA_HOME not found at $env:JAVA_HOME - set it to your JDK path."
}

$mvnCmd = Join-Path $mavenHome "bin\mvn.cmd"
Write-Host ""
Write-Host "Maven ready."
Write-Host "JAVA_HOME = $env:JAVA_HOME"
Write-Host "MVN       = $mvnCmd"
Write-Host ""
Write-Host "Next (download Auth Service dependencies):"
Write-Host "  cd services\auth-service"
Write-Host "  set JAVA_HOME and run:"
Write-Host ("  " + $mvnCmd + " -DskipTests dependency:resolve")
