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
rem Cho OS giai phong handle file sau khi tat daemon / game
timeout /t 1 /nobreak >nul
call :remove_build_output_dirs

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

:remove_build_output_dirs
rem Xoa core/build va lwjgl3/build neu con (tranh loi Gradle khong xoa duoc stale outputs tren Windows)
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$root = (Get-Location).Path;" ^
  "foreach ($p in @('core\\build','lwjgl3\\build')) {" ^
  "  $full = Join-Path $root $p;" ^
  "  if (Test-Path $full) { try { Remove-Item -LiteralPath $full -Recurse -Force -ErrorAction Stop; Write-Host ('[run-game.bat] Da xoa ' + $p) } catch { Write-Host ('[run-game.bat] Khong xoa duoc ' + $p + ' — hay tat game/IDE dang giu file.') } }" ^
  "}"
exit /b 0
