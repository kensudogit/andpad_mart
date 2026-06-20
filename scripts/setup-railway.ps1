# ANDPAD intra-mart — Railway 初回セットアップ
# 使い方: cd C:\devlop\andpad_mart && .\scripts\setup-railway.ps1

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "=== Railway link (discerning-transformation / production / andpad_mart) ===" -ForegroundColor Cyan
railway link -p discerning-transformation -e production -s andpad_mart

Write-Host "`n=== Copy variables from andpad_j (same Postgres) ===" -ForegroundColor Cyan
Push-Location (Join-Path (Split-Path $PSScriptRoot -Parent) "..\andpad_j")
railway link -p discerning-transformation -e production -s andpad_j | Out-Null
$vars = railway variable list --json | ConvertFrom-Json
Pop-Location

railway link -p discerning-transformation -e production -s andpad_mart | Out-Null
railway variable set "DATABASE_URL=$($vars.DATABASE_URL)" -s andpad_mart
railway variable set "JWT_SECRET=$($vars.JWT_SECRET)" -s andpad_mart
railway variable set "OPENAI_API_KEY=$($vars.OPENAI_API_KEY)" -s andpad_mart

Write-Host "`n=== Deploy ===" -ForegroundColor Cyan
railway up --detach

Write-Host @"

=== Verify ===
  https://andpadmart-production-a5f0.up.railway.app/health
  https://andpadmart-production-a5f0.up.railway.app/api/status

Do NOT set API_URL on unified deploy.

"@ -ForegroundColor Yellow
