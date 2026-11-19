@echo off
chcp 65001 >nul
REM ============================================================
REM  Graduation System - One-click server + Cloudflare tunnel
REM  Double-click to start. Keep this window OPEN while testing.
REM  Press Ctrl+C (or close the window) to stop.
REM ============================================================

set "CF=C:\Program Files (x86)\cloudflared\cloudflared.exe"
set "APPDIR=C:\Users\KH\Desktop\graduation-system"
set "LOG=%TEMP%\gs_cloudflare.log"

cd /d "%APPDIR%"

echo [1/2] Starting Laravel server on http://localhost:8000 ...
start "GS-Laravel-Server" cmd /c "php artisan serve --host=127.0.0.1 --port=8000"

timeout /t 4 /nobreak >nul

if exist "%LOG%" del "%LOG%"

echo [2/2] Starting Cloudflare quick tunnel ...
start "GS-Cloudflare-Tunnel" cmd /k ""%CF%" tunnel --url http://localhost:8000 2> "%LOG%""

echo.
echo Waiting for the tunnel URL ...
timeout /t 10 /nobreak >nul

echo.
echo ================= YOUR API BASE URL =================
powershell -NoProfile -Command "(Select-String -Path '%LOG%' -Pattern 'https://[a-z0-9-]+\.trycloudflare\.com' | Select-Object -First 1).Matches.Value + '/api/'"
echo =====================================================
echo.
echo Paste the URL above into the app's "Server Settings" screen
echo (link at the bottom of the login screen), or it is already
echo baked into the latest APK build.
echo.
echo Keep BOTH new windows open while you use the app.
pause
