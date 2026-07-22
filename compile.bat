@echo off
echo ===================================================
echo   Compiling Smart Campus Scheduler (Java + MongoDB)
echo ===================================================

powershell -Command "Get-ChildItem -Recurse -Filter *.java -Path src | Resolve-Path -Relative | Set-Content -Encoding ascii sources.txt"

if not exist bin mkdir bin

powershell -Command "javac -d bin -cp 'lib/*' --%% @sources.txt"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation successful! Class files generated in bin/
) else (
    echo.
    echo ❌ Compilation failed! Please check error messages above.
)
