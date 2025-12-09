# ============================================
# Script de Ejecución - Sistema ECG (PowerShell)
# ============================================

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   SISTEMA ECG - Monitor de Electrocardiograma" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que existen los archivos necesarios
if (-not (Test-Path "sqlite-jdbc.jar")) {
    Write-Host "ERROR: No se encuentra sqlite-jdbc.jar" -ForegroundColor Red
    Write-Host "Por favor ejecuta primero el script de instalación." -ForegroundColor Yellow
    Read-Host "Presiona Enter para salir"
    exit 1
}

if (-not (Test-Path "slf4j-api.jar")) {
    Write-Host "ERROR: No se encuentran las dependencias SLF4J" -ForegroundColor Red
    Write-Host "Por favor ejecuta primero el script de instalación." -ForegroundColor Yellow
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host "[1/2] Compilando sistema..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp ".;sqlite-jdbc.jar" SistemaECG.java DatabaseManager.java

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: La compilación falló" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host "[2/2] Iniciando aplicación..." -ForegroundColor Green
Write-Host ""
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" SistemaECG

Write-Host ""
Write-Host "Sistema finalizado." -ForegroundColor Cyan
Read-Host "Presiona Enter para salir"
