<#
  Seeds the REOAGMS databases with the demo portfolio WITHOUT starting live simulation.
  Data goes through the real REST APIs (gateway -> services -> PostgreSQL), so validation, RBAC,
  audit logging and RabbitMQ events behave exactly as in production. Safe to re-run: every stage is idempotent.

  Usage:   .\scripts\seed-database.ps1            # seed, then exit
           .\scripts\seed-database.ps1 -Reset     # DELETE all data first, then seed (destructive)
#>
param([switch]$Reset)
Set-Location (Split-Path $PSScriptRoot -Parent)
if ($Reset) {
  Write-Host "Resetting all REOAGMS volumes..." -ForegroundColor Yellow
  docker compose down -v
}
docker compose up -d --build identity-service asset-service monitoring-service maintenance-service analytics-service notification-service api-gateway
Write-Host "Waiting for the gateway..."
do { Start-Sleep 5; try { $ok = (Invoke-WebRequest http://localhost:8080/actuator/health -UseBasicParsing -TimeoutSec 5).StatusCode -eq 200 } catch { $ok = $false } } until ($ok)
docker compose run --rm --no-deps -e SIM_MODE=seed simulator
Write-Host "Seed complete. Start live telemetry with: docker compose up -d simulator" -ForegroundColor Green
