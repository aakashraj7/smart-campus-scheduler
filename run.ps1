Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Launching Smart Campus Scheduler Web Application" -ForegroundColor Cyan
Write-Host "  URL: http://localhost:8080" -ForegroundColor Yellow
Write-Host "===================================================" -ForegroundColor Cyan

.\mvnw.cmd spring-boot:run
