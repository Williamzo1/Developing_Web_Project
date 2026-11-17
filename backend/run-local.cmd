@echo off
setlocal
cd /d "%~dp0"
set "SPRING_PROFILES_ACTIVE=local"
set "PATH=%SystemRoot%\System32\WindowsPowerShell\v1.0;%PATH%"
call mvnw.cmd spring-boot:run
