@echo off
setlocal

if defined JAVA_HOME (
  if not exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME="
)

call "%~dp0gradle-8.4-extract\gradle-8.4\bin\gradle.bat" %*
