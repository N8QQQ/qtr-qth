#!/usr/bin/env pwsh
param (
    [Parameter(Mandatory=$true)]
    [int]$PrId
)

Write-Host "--- ⚖️ qtr-qth: Manual Merge Protocol ---" -ForegroundColor Cyan
Write-Host "Reviewing PR #$PrId..."
gh pr view $PrId

Write-Host "`nInitiating Squash Merge..." -ForegroundColor Yellow
gh pr merge $PrId --squash --delete-branch

Write-Host "`n✅ Merge Complete. Synchronizing local main..." -ForegroundColor Green
git checkout main
git pull origin main
