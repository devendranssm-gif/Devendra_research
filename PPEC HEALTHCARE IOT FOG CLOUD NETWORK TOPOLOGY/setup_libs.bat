@echo off
REM setup_libs.bat — Windows version of library setup
REM Usage: setup_libs.bat C:\path\to\iFogSim2

SET IFOGSIM2_DIR=%1
IF "%IFOGSIM2_DIR%"=="" SET IFOGSIM2_DIR=..\iFogSim2

echo === PPEC iFogSim2 Library Setup (Windows) ===

IF NOT EXIST "%IFOGSIM2_DIR%" (
    echo ERROR: iFogSim2 directory not found: %IFOGSIM2_DIR%
    echo Clone iFogSim2 first:
    echo   git clone https://github.com/Cloudslab/iFogSim2.git
    exit /b 1
)

echo [1] Building iFogSim2...
cd /d "%IFOGSIM2_DIR%"
call mvn clean package -DskipTests -q

echo [2] Installing iFogSim2 to Maven repo...
FOR /R target %%f IN (*.jar) DO (
    echo %%f | findstr /v "sources javadoc" >nul && SET IFOGSIM2_JAR=%%f && GOTO :found
)
:found

call mvn install:install-file ^
    -Dfile="%IFOGSIM2_JAR%" ^
    -DgroupId=org.fog-computing ^
    -DartifactId=ifogsim2 ^
    -Dversion=2.0.0 ^
    -Dpackaging=jar

echo [3] Creating libs directory...
IF NOT EXIST "%~dp0libs" mkdir "%~dp0libs"

echo [4] Copying JAR to libs/...
COPY "%IFOGSIM2_JAR%" "%~dp0libs\ifogsim2.jar"

echo.
echo === Setup complete ===
echo Now run:
echo   cd %~dp0
echo   mvn clean package -DskipTests
echo   java -jar output\PPEC-iFogSim2-full-jar-with-dependencies.jar
