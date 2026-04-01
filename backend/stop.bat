@echo off
echo Stopping XpenseTrack...
echo.

:: Stop Spring Boot (kill Java on port 8080)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Stopping backend (PID: %%a)...
    taskkill /PID %%a /F >nul 2>&1
)

:: Stop MongoDB (kill mongod on port 27017)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :27017 ^| findstr LISTENING') do (
    echo Stopping MongoDB (PID: %%a)...
    taskkill /PID %%a /F >nul 2>&1
)

echo.
echo XpenseTrack stopped.
pause
