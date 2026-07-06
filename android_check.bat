@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
echo JAVA_HOME=%JAVA_HOME%
android\gradlew.bat -p android tasks --dry-run
