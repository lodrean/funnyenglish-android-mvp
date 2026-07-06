@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\etaba\funnyenglish\backend
gradlew.bat bootJar
java -jar build\libs\funnyenglish-backend-1.0.0.jar --server.port=8082
