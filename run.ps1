Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Launching Smart Campus Scheduler CLI Application" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

java -cp "bin;lib/*" com.campus.scheduler.Main
