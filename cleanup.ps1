# Cleanup script for Docker build preparation

Write-Host "Cleaning up unnecessary files for Docker build..." -ForegroundColor Green

# Remove build artifacts
if (Test-Path "build") {
    Write-Host "Removing build directory..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force "build"
}

# Remove Gradle cache
if (Test-Path ".gradle") {
    Write-Host "Removing .gradle cache..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force ".gradle"
}

# Remove IDE files
if (Test-Path ".idea") {
    Write-Host "Removing .idea directory..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force ".idea"
}

# Remove test scripts (not needed in production)
if (Test-Path "test_api.ps1") {
    Write-Host "Removing test_api.ps1..." -ForegroundColor Yellow
    Remove-Item "test_api.ps1"
}

if (Test-Path "simple_test.ps1") {
    Write-Host "Removing simple_test.ps1..." -ForegroundColor Yellow
    Remove-Item "simple_test.ps1"
}

# Remove temporary files
if (Test-Path "tatus") {
    Write-Host "Removing tatus file..." -ForegroundColor Yellow
    Remove-Item "tatus"
}

Write-Host "Cleanup completed!" -ForegroundColor Green
Write-Host "Files removed:" -ForegroundColor Cyan
Write-Host "- build/ (build artifacts)" -ForegroundColor Cyan
Write-Host "- .gradle/ (Gradle cache)" -ForegroundColor Cyan
Write-Host "- .idea/ (IDE files)" -ForegroundColor Cyan
Write-Host "- test_api.ps1 (test script)" -ForegroundColor Cyan
Write-Host "- simple_test.ps1 (test script)" -ForegroundColor Cyan
Write-Host "- tatus (temporary file)" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ready for Docker build!" -ForegroundColor Green 