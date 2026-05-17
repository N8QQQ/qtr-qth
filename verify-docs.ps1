# qtr-qth: Local Documentation Staging Script
# This script launches a high-fidelity Jekyll environment for documentation preview.

$ImageName = "qtr-qth-docs"

Write-Host "--- 🏗️ Building Documentation Staging Environment ---" -ForegroundColor Cyan
docker build -f Dockerfile.docs -t $ImageName .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed." -ForegroundColor Red
    exit 1
}

Write-Host "`n--- 🚀 Launching Documentation Hub (Local) ---" -ForegroundColor Cyan
Write-Host "View your changes at: http://localhost:4000/qtr-qth/" -ForegroundColor Green
Write-Host "Press Ctrl+C in this terminal to stop the server.`n" -ForegroundColor Yellow

# Run the container with a volume mount to the 'content' sub-directory
docker run --rm -it -p 4000:4000 -v "${PWD}/docs:/srv/jekyll/content" $ImageName
