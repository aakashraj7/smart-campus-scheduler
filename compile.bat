@echo off
echo ===================================================
echo   Compiling Smart Campus Scheduler (Spring Boot)
echo ===================================================

call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Compilation successful! Spring Boot Web Application ready to run.
) else (
    echo.
    echo [ERROR] Compilation failed! Please check error messages above.
)
