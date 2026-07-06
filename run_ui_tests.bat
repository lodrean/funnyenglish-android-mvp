@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\etaba\funnyenglish\android
gradlew.bat connectedAndroidTest --info
