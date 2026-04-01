@echo off
setlocal enabledelayedexpansion
title XpenseTrack Backend Setup
color 0A

echo ============================================
echo    XpenseTrack Backend Setup Script
echo    Windows - No Docker Required
echo ============================================
echo.

set "INSTALL_DIR=%USERPROFILE%\xpensetrack-tools"
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

:: -----------------------------------------------
:: STEP 1: Check and Install Java 17
:: -----------------------------------------------
echo [1/4] Checking Java 17...
java -version 2>nul | findstr /i "17\." >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('java -version 2^>^&1') do (
        echo    Found: %%i
        goto java_ok
    )
)

:: Check if we already downloaded it locally
if exist "%INSTALL_DIR%\jdk-17\bin\java.exe" (
    set "JAVA_HOME=%INSTALL_DIR%\jdk-17"
    set "PATH=%INSTALL_DIR%\jdk-17\bin;%PATH%"
    echo    Using local JDK 17 from %INSTALL_DIR%\jdk-17
    goto java_ok
)

echo    Java 17 not found. Installing...
echo.

:: Try winget first
winget --version >nul 2>&1
if %errorlevel% equ 0 (
    echo    Trying winget install...
    winget install EclipseAdoptium.Temurin.17.JDK --accept-source-agreements --accept-package-agreements >nul 2>&1
    if %errorlevel% equ 0 (
        echo    Java 17 installed via winget.
        echo    Please CLOSE this window and run the script again in a NEW terminal.
        pause
        exit /b 0
    )
    echo    winget install failed, falling back to manual download...
)

echo    Downloading Eclipse Temurin JDK 17 (this may take a minute)...
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile '%INSTALL_DIR%\jdk17.zip' }"

if not exist "%INSTALL_DIR%\jdk17.zip" (
    echo    ERROR: Download failed.
    echo    Please install Java 17 manually from https://adoptium.net/
    pause
    exit /b 1
)

echo    Extracting JDK 17...
powershell -Command "Expand-Archive -Path '%INSTALL_DIR%\jdk17.zip' -DestinationPath '%INSTALL_DIR%\jdk17-temp' -Force"

:: Move the inner folder to jdk-17
for /d %%d in ("%INSTALL_DIR%\jdk17-temp\*") do (
    if exist "%INSTALL_DIR%\jdk-17" rmdir /s /q "%INSTALL_DIR%\jdk-17"
    move "%%d" "%INSTALL_DIR%\jdk-17" >nul
)
rmdir /s /q "%INSTALL_DIR%\jdk17-temp" 2>nul
del "%INSTALL_DIR%\jdk17.zip" 2>nul

set "JAVA_HOME=%INSTALL_DIR%\jdk-17"
set "PATH=%INSTALL_DIR%\jdk-17\bin;%PATH%"
echo    JDK 17 installed to %INSTALL_DIR%\jdk-17

:java_ok
echo.

:: -----------------------------------------------
:: STEP 2: Check and Install Maven
:: -----------------------------------------------
echo [2/4] Checking Maven...
mvn -version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=1,2,3" %%a in ('mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do echo    Found: %%a %%b %%c
    goto maven_ok
)

:: Check local install
if exist "%INSTALL_DIR%\maven\bin\mvn.cmd" (
    set "PATH=%INSTALL_DIR%\maven\bin;%PATH%"
    echo    Using local Maven from %INSTALL_DIR%\maven
    goto maven_ok
)

echo    Maven not found. Installing...

:: Try winget
winget --version >nul 2>&1
if %errorlevel% equ 0 (
    echo    Trying winget install...
    winget install Apache.Maven --accept-source-agreements --accept-package-agreements >nul 2>&1
    if %errorlevel% equ 0 (
        echo    Maven installed via winget.
        echo    Please CLOSE this window and run the script again in a NEW terminal.
        pause
        exit /b 0
    )
)

echo    Downloading Maven 3.9.6...
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%INSTALL_DIR%\maven.zip' }"

if not exist "%INSTALL_DIR%\maven.zip" (
    echo    ERROR: Download failed.
    echo    Please install Maven manually from https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo    Extracting Maven...
powershell -Command "Expand-Archive -Path '%INSTALL_DIR%\maven.zip' -DestinationPath '%INSTALL_DIR%' -Force"
if exist "%INSTALL_DIR%\maven" rmdir /s /q "%INSTALL_DIR%\maven"
rename "%INSTALL_DIR%\apache-maven-3.9.6" "maven"
del "%INSTALL_DIR%\maven.zip" 2>nul

set "PATH=%INSTALL_DIR%\maven\bin;%PATH%"
echo    Maven installed to %INSTALL_DIR%\maven

:maven_ok
echo.

:: -----------------------------------------------
:: STEP 3: Check and Install MongoDB
:: -----------------------------------------------
echo [3/4] Checking MongoDB...

:: Check if mongod is running on port 27017
netstat -an | findstr "27017" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo    MongoDB already running on port 27017.
    goto mongo_ok
)

:: Check if mongod exists
where mongod >nul 2>&1
if %errorlevel% equ 0 (
    echo    MongoDB found. Starting...
    goto start_mongo
)

:: Check local install
if exist "%INSTALL_DIR%\mongodb\bin\mongod.exe" (
    echo    Using local MongoDB from %INSTALL_DIR%\mongodb
    goto start_mongo
)

echo    MongoDB not found. Installing...

:: Try winget
winget --version >nul 2>&1
if %errorlevel% equ 0 (
    echo    Trying winget install...
    winget install MongoDB.Server --accept-source-agreements --accept-package-agreements >nul 2>&1
    if %errorlevel% equ 0 (
        echo    MongoDB installed via winget.
        echo    Please CLOSE this window and run the script again in a NEW terminal.
        pause
        exit /b 0
    )
)

echo    Downloading MongoDB 7.0 Community Server...
echo    (This is ~300MB, may take a few minutes)
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-7.0.20-signed.msi' -OutFile '%INSTALL_DIR%\mongodb.msi' }"

if not exist "%INSTALL_DIR%\mongodb.msi" (
    echo    MSI download failed. Trying zip version...
    powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-7.0.20.zip' -OutFile '%INSTALL_DIR%\mongodb.zip' }"

    if not exist "%INSTALL_DIR%\mongodb.zip" (
        echo    ERROR: Download failed.
        echo    Please install MongoDB manually from https://www.mongodb.com/try/download/community
        pause
        exit /b 1
    )

    echo    Extracting MongoDB...
    powershell -Command "Expand-Archive -Path '%INSTALL_DIR%\mongodb.zip' -DestinationPath '%INSTALL_DIR%\mongodb-temp' -Force"
    for /d %%d in ("%INSTALL_DIR%\mongodb-temp\*") do (
        if exist "%INSTALL_DIR%\mongodb" rmdir /s /q "%INSTALL_DIR%\mongodb"
        move "%%d" "%INSTALL_DIR%\mongodb" >nul
    )
    rmdir /s /q "%INSTALL_DIR%\mongodb-temp" 2>nul
    del "%INSTALL_DIR%\mongodb.zip" 2>nul
    echo    MongoDB extracted to %INSTALL_DIR%\mongodb
    goto start_mongo
)

echo    Installing MongoDB via MSI (silent)...
msiexec /i "%INSTALL_DIR%\mongodb.msi" /quiet /qn INSTALLLOCATION="%INSTALL_DIR%\mongodb" ADDLOCAL="ServerService,Server"
del "%INSTALL_DIR%\mongodb.msi" 2>nul
echo    MongoDB installed.

:: Check if the service was installed and started
sc query MongoDB >nul 2>&1
if %errorlevel% equ 0 (
    echo    MongoDB Windows service detected.
    sc start MongoDB >nul 2>&1
    timeout /t 3 /nobreak >nul
    netstat -an | findstr "27017" | findstr "LISTENING" >nul 2>&1
    if %errorlevel% equ 0 (
        echo    MongoDB service running on port 27017.
        goto mongo_ok
    )
)

:start_mongo
:: Find mongod executable
set "MONGOD_EXE="
where mongod >nul 2>&1
if %errorlevel% equ 0 (
    set "MONGOD_EXE=mongod"
) else if exist "%INSTALL_DIR%\mongodb\bin\mongod.exe" (
    set "MONGOD_EXE=%INSTALL_DIR%\mongodb\bin\mongod.exe"
) else if exist "C:\Program Files\MongoDB\Server\7.0\bin\mongod.exe" (
    set "MONGOD_EXE=C:\Program Files\MongoDB\Server\7.0\bin\mongod.exe"
)

if "!MONGOD_EXE!"=="" (
    echo    ERROR: Cannot find mongod.exe
    echo    Please install MongoDB from https://www.mongodb.com/try/download/community
    pause
    exit /b 1
)

:: Create data directory
set "MONGO_DATA=%INSTALL_DIR%\mongodb-data"
if not exist "%MONGO_DATA%" mkdir "%MONGO_DATA%"

echo    Starting MongoDB...
start "MongoDB" /min "!MONGOD_EXE!" --dbpath "%MONGO_DATA%" --port 27017

:: Wait for MongoDB to start
echo    Waiting for MongoDB to be ready...
set /a retries=0
:wait_mongo
timeout /t 2 /nobreak >nul
netstat -an | findstr "27017" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo    MongoDB is ready on port 27017.
    goto mongo_ok
)
set /a retries+=1
if %retries% lss 15 (
    echo    Still waiting... (attempt %retries%/15)
    goto wait_mongo
)
echo    WARNING: MongoDB may not have started. Continuing anyway...

:mongo_ok
echo.

:: -----------------------------------------------
:: STEP 4: Build and Run Spring Boot
:: -----------------------------------------------
echo [4/4] Building and starting XpenseTrack backend...
echo.

cd /d "%~dp0"

echo    Compiling project...
call mvn clean compile -q 2>nul
if %errorlevel% neq 0 (
    echo    First compile attempt failed, trying with full output...
    call mvn clean compile
    if %errorlevel% neq 0 (
        echo.
        echo    ERROR: Build failed. Check errors above.
        pause
        exit /b 1
    )
)
echo    Build successful.
echo.
echo ============================================
echo    XpenseTrack Backend Starting
echo    Server:  http://localhost:8080
echo    MongoDB: localhost:27017
echo ============================================
echo.
echo    Test signup:
echo    curl -X POST http://localhost:8080/api/auth/signup ^
echo      -H "Content-Type: application/json" ^
echo      -d "{\"fullName\":\"Test\",\"phoneNumber\":\"1234567890\",\"email\":\"test@test.com\",\"password\":\"pass123\",\"confirmPassword\":\"pass123\",\"termsAccepted\":true}"
echo.
echo    Press Ctrl+C to stop the server.
echo ============================================
echo.

call mvn spring-boot:run

pause
