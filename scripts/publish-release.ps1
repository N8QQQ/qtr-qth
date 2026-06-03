param (
    [Parameter(Mandatory=$true)]
    [string]$Version,
    [Parameter(Mandatory=$true)]
    [string]$NotesPath
)

Write-Host "--- 🚀 qtr-qth: Manual Release Protocol ---" -ForegroundColor Cyan
Write-Host "Target Version: $Version"
Write-Host "Notes Source:   $NotesPath"

# 1. Verification
Write-Host "`n[1/3] Executing Final Quality Gate..." -ForegroundColor Yellow
./gradlew clean check
if ($LASTEXITCODE -ne 0) { 
    Write-Host "❌ Quality Gate Failed. Release Aborted." -ForegroundColor Red
    exit 1 
}

# 2. Asset Generation
Write-Host "`n[2/3] Generating Distribution Archive..." -ForegroundColor Yellow
./gradlew distZip

# Normalize version for filename (remove leading 'v' if present)
$CleanVersion = $Version.Replace("v", "")
$ZipPath = "build/distributions/qtr-qth-$CleanVersion.zip"

if (-not (Test-Path $ZipPath)) { 
    Write-Host "❌ Zip asset not found at $ZipPath. Ensure version matches build.gradle.kts." -ForegroundColor Red
    exit 1 
}

# 3. GitHub Publication
Write-Host "`n[3/3] Publishing to GitHub..." -ForegroundColor Yellow
# Note: This assumes the tag already exists or will be created on the current commit.
gh release create $Version --title "$Version" --notes-file $NotesPath
gh release upload $Version $ZipPath

Write-Host "`n✅ Release $Version Published Successfully with Assets." -ForegroundColor Green
