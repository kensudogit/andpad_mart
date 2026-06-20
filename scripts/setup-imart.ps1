# ANDPAD intra-mart 開発環境セットアップ
param(
    [switch]$SkipDocker,
    [switch]$SkipNpm
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent

Write-Host "=== ANDPAD intra-mart setup ===" -ForegroundColor Cyan
Write-Host "Root: $root"

Set-Location $root

if (-not $SkipDocker) {
    Write-Host "`n[1/3] Starting PostgreSQL (port 5435)..." -ForegroundColor Yellow
    docker compose up -d db
    Start-Sleep -Seconds 3
}

Write-Host "`n[2/3] Gradle build (backend + imart)..." -ForegroundColor Yellow
& .\gradlew.bat :backend:compileJava :imart:compileJava
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if (-not $SkipNpm) {
    Write-Host "`n[3/3] npm install (frontend)..." -ForegroundColor Yellow
    Push-Location frontend
    npm install
    Pop-Location
}

Write-Host "`n=== Setup complete ===" -ForegroundColor Green
Write-Host "API:  .\gradlew.bat :backend:bootRun"
Write-Host "Web:  cd frontend && npm run dev"
Write-Host "Test: .\gradlew.bat test"
Write-Host "WAR:  .\gradlew.bat :backend:bootWar  -> backend/build/libs/andpad-imart.war"
