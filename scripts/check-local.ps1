$ErrorActionPreference = "Continue"
$ports = 8080,8081,8082,8083,8084,8085,8086
Write-Host "=== Docker Compose status ==="
docker compose ps
Write-Host "`n=== Actuator health ==="
foreach ($port in $ports) {
  try {
    $response = Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -TimeoutSec 5
    Write-Host ("{0}: {1}" -f $port, $response.status)
  } catch { Write-Host ("{0}: FAILED - {1}" -f $port, $_.Exception.Message) }
}
Write-Host "`n=== Gateway route smoke tests (401 is expected without a token on protected routes) ==="
foreach ($path in @('/api/v1/assets','/api/v1/alerts','/api/v1/work-orders','/api/v1/notifications')) {
  try { $r = Invoke-WebRequest -Uri ("http://localhost:8080" + $path) -Method GET -SkipHttpErrorCheck -TimeoutSec 5; Write-Host ("{0}: HTTP {1}" -f $path, [int]$r.StatusCode) }
  catch { Write-Host ("{0}: FAILED - {1}" -f $path, $_.Exception.Message) }
}
