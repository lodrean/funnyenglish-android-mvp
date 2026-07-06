@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
cd backend
call gradlew.bat build 2>&1
if %ERRORLEVEL% == 0 (
  echo BUILD_SUCCESS
) else (
  echo BUILD_FAILED
)
cd ..
