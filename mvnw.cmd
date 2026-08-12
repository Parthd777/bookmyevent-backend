@echo off
setlocal

set MAVEN_VERSION=3.9.6
set MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MAVEN_CMD=%MAVEN_DIR%\bin\mvn.cmd
set DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip
set ZIP_FILE=%USERPROFILE%\.m2\wrapper\apache-maven-%MAVEN_VERSION%-bin.zip

if exist "%MAVEN_CMD%" goto runMaven

echo Downloading Maven %MAVEN_VERSION% ...
if not exist "%USERPROFILE%\.m2\wrapper\dists" mkdir "%USERPROFILE%\.m2\wrapper\dists"

powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%ZIP_FILE%' -UseBasicParsing"
if errorlevel 1 ( echo Download failed. & exit /b 1 )

powershell -NoProfile -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
if errorlevel 1 ( echo Extraction failed. & exit /b 1 )

del "%ZIP_FILE%"
echo Maven %MAVEN_VERSION% ready.

:runMaven
"%MAVEN_CMD%" %*
endlocal
