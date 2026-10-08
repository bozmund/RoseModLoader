@echo off
rem Rose command line (Agent Bridge, MCP, launching). Builds itself on first use.
setlocal
set "ROSE_HOME=%~dp0"
if "%ROSE_HOME:~-1%"=="\" set "ROSE_HOME=%ROSE_HOME:~0,-1%"
set "ROSE_BIN=%ROSE_HOME%\bridge-cli\build\install\rose\bin\rose.bat"
if not exist "%ROSE_BIN%" (
  call "%ROSE_HOME%\gradlew.bat" -q -p "%ROSE_HOME%" :bridge-cli:installDist 1>&2 || exit /b 1
)
call "%ROSE_BIN%" %*
