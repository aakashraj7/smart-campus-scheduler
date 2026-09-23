Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Compiling Smart Campus Scheduler (Spring Boot)" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

.\mvnw.cmd clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Compilation successful! Spring Boot Web Application ready to run." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Compilation failed! Please check error messages above." -ForegroundColor Red
}
