@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

rem Use existing PATH / JAVA_HOME if already valid
if defined JAVA_HOME (
  if exist "!JAVA_HOME!\bin\java.exe" goto :run
)
java -version >nul 2>&1
if !errorlevel! equ 0 goto :run

rem Auto-detect JDK (newest folder name first: /o-n)
call :try_dir "%ProgramFiles%\Eclipse Adoptium" jdk-*
if defined JAVA_HOME goto :run
call :try_dir "%ProgramFiles%\Java" jdk-*
if defined JAVA_HOME goto :run
call :try_dir "%ProgramFiles%\Microsoft" jdk-*
if defined JAVA_HOME goto :run
call :try_dir "%LocalAppData%\Programs\Eclipse Adoptium" jdk-*
if defined JAVA_HOME goto :run

echo.
echo [run-game.bat] Khong tim thay JDK. Hay cai Temurin JDK va ghi nho tuy chon PATH + JAVA_HOME,
echo hoac dat bien JAVA_HOME tro toi thu muc JDK (co bin\java.exe).
echo.
exit /b 1

:try_dir
set "_base=%~1"
set "_pat=%~2"
if not exist "!_base!" exit /b 1
for /f "delims=" %%I in ('dir /b /ad /o-n "!_base!\!_pat!" 2^>nul') do (
  if exist "!_base!\%%I\bin\java.exe" (
    set "JAVA_HOME=!_base!\%%I"
    exit /b 0
  )
)
exit /b 1

:run
if defined JAVA_HOME set "JAVA_HOME=!JAVA_HOME:"=!"

call :kill_running_game
call gradlew.bat --stop >nul 2>&1

call gradlew.bat lwjgl3:run
exit /b %errorlevel%

:kill_running_game
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$patterns = @('com.ChronosDetective.game.lwjgl3.Lwjgl3Launcher');" ^
  "$killed = 0;" ^
  "Get-CimInstance Win32_Process | Where-Object { $_.Name -match '^(java|javaw)\.exe$' -and $_.CommandLine } | ForEach-Object {" ^
  "  $cmd = $_.CommandLine;" ^
  "  if ($patterns | Where-Object { $cmd -like ('*' + $_ + '*') }) {" ^
  "    try { Stop-Process -Id $_.ProcessId -Force -ErrorAction Stop; $killed++; Write-Host ('[run-game.bat] Killed PID ' + $_.ProcessId) } catch {}" ^
  "  }" ^
  "};" ^
  "if ($killed -eq 0) { Write-Host '[run-game.bat] Khong tim thay game dang chay.' }"
exit /b 0
