@echo off
title Bank Management - Build & Run Backend
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-22"
set "CATALINA_HOME=C:\Users\DELL\Downloads\apache-tomcat-11.0.15"
set "M2_HOME=C:\Users\DELL\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16"

echo ========================================================
echo  Step 1: Building Backend (Maven)
echo ========================================================
call "%M2_HOME%\bin\mvn.cmd" package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================================
echo  Step 2: Syncing Files to Tomcat Webapps
echo ========================================================
xcopy /E /I /Y "target\bank-management\*" "%CATALINA_HOME%\webapps\bank-management\" >nul

echo.
echo ========================================================
echo  Step 3: Launching Tomcat 11 on http://localhost:8080
echo ========================================================
call "%CATALINA_HOME%\bin\catalina.bat" run
pause
