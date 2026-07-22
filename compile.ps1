Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Compiling Smart Campus Scheduler (Java + MongoDB)" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

# Professional Dependency Resolution: Auto-download dependencies if lib/ is missing
if (-not (Test-Path lib)) {
    New-Item -ItemType Directory -Force -Path lib | Out-Null
    Write-Host "Downloading required dependencies from Maven Central..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-sync/4.11.1/mongodb-driver-sync-4.11.1.jar' -OutFile 'lib/mongodb-driver-sync-4.11.1.jar'
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-core/4.11.1/mongodb-driver-core-4.11.1.jar' -OutFile 'lib/mongodb-driver-core-4.11.1.jar'
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/mongodb/bson/4.11.1/bson-4.11.1.jar' -OutFile 'lib/bson-4.11.1.jar'
    Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar' -OutFile 'lib/jbcrypt-0.4.jar'
}

Get-ChildItem -Recurse -Filter *.java -Path src | Resolve-Path -Relative | Set-Content -Encoding ascii sources.txt

if (-not (Test-Path bin)) {
    New-Item -ItemType Directory bin | Out-Null
}

javac -d bin -cp "lib/*" --% @sources.txt

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Compilation successful! Class files generated in bin/" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Compilation failed! Please check error messages above." -ForegroundColor Red
}
