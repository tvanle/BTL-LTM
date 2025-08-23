@echo off
echo ===================================
echo     NGROK SETUP FOR WORDBRAIN2
echo ===================================
echo.

REM Check if ngrok.exe exists
if not exist ngrok.exe (
    echo [ERROR] ngrok.exe not found! Downloading...
    powershell -Command "Invoke-WebRequest -Uri 'https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-windows-amd64.zip' -OutFile 'ngrok.zip'"
    powershell -Command "Expand-Archive -Path 'ngrok.zip' -DestinationPath '.' -Force"
    del ngrok.zip
)

echo [1] Starting Spring Boot application...
start cmd /k "mvn spring-boot:run"

echo [2] Waiting for application to start (10 seconds)...
timeout /t 10 /nobreak > nul

echo [3] Starting Ngrok tunnel on port 8080...
echo.
echo ===================================
echo     NGROK TUNNEL INFORMATION
echo ===================================
echo.
echo After Ngrok starts, look for the line:
echo "Forwarding  https://xxxxx.ngrok.io -> http://localhost:8080"
echo.
echo Share this URL with other players!
echo ===================================
echo.

ngrok http 8080

pause