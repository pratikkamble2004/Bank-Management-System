@echo off
title Bank Management - Angular Frontend
cd /d "%~dp0frontend"

echo ========================================================
echo  Starting Angular Frontend Dev Server (Port 4200)
echo ========================================================
call npm start
pause
