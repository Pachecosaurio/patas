@echo off
REM ============================================
REM Script de Ejecución - Sistema ECG
REM ============================================

echo.
echo ================================================
echo   SISTEMA ECG - Monitor de Electrocardiograma
echo ================================================
echo.

REM Verificar que existen los archivos necesarios
if not exist "sqlite-jdbc.jar" (
    echo ERROR: No se encuentra sqlite-jdbc.jar
    echo Por favor ejecuta primero el script de instalacion.
    pause
    exit /b 1
)

if not exist "slf4j-api.jar" (
    echo ERROR: No se encuentran las dependencias SLF4J
    echo Por favor ejecuta primero el script de instalacion.
    pause
    exit /b 1
)

echo [1/2] Compilando sistema...
javac -encoding UTF-8 -cp ".;sqlite-jdbc.jar" SistemaECG.java DatabaseManager.java

if %errorlevel% neq 0 (
    echo.
    echo ERROR: La compilacion fallo
    pause
    exit /b 1
)

echo [2/2] Iniciando aplicacion...
echo.
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" SistemaECG

echo.
echo Sistema finalizado.
pause
