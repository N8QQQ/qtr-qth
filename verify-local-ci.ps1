# qtr-qth: Local CI Verification Script
# This script runs the Heritage Grade Quality Gate inside a Docker container.

$ImageName = "qtr-qth-ci"

Write-Host "--- 🏗️ Building Heritage CI Environment ---" -ForegroundColor Cyan
docker build -f Dockerfile.ci -t $ImageName .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed." -ForegroundColor Red
    exit 1
}

Write-Host "`n--- 🛡️ Running Quality Gate (No-Daemon Mode) ---" -ForegroundColor Cyan
docker run --rm $ImageName

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Quality Gate failed. Review the logs above for test or linting failures." -ForegroundColor Red
    exit 1
}

Write-Host "`n✅ Heritage Grade Certification: PASSED" -ForegroundColor Green
