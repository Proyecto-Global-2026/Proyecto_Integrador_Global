# Ejecuta la coleccion de Postman desde linea de comandos con Newman.
# Requisito: node instalado y  npx newman  disponible.
# Uso:  powershell -ExecutionPolicy Bypass -File postman\run-newman.ps1

param(
	[string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

$coleccion = Join-Path $PSScriptRoot "ProyectoGlobal.postman_collection.json"
$entorno = Join-Path $PSScriptRoot "Entorno-Local.postman_environment.json"

Write-Host "== Proyecto Global - pruebas funcionales ==" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl"

try {
	Invoke-WebRequest -Uri "$BaseUrl/v3/api-docs" -UseBasicParsing -TimeoutSec 5 | Out-Null
} catch {
	Write-Host "El backend no responde en $BaseUrl. Levantalo antes de correr las pruebas." -ForegroundColor Red
	exit 1
}

$args = @(
	"run", $coleccion,
	"--environment", $entorno,
	"--env-var", "baseUrl=$BaseUrl",
	"--reporters", "cli,newman-reporter-json",
	"--reporter-json-export", "postman/newman-report.json"
)

npx --yes newman @args
$codigo = $LASTEXITCODE

if ($codigo -ne 0) {
	Write-Host ""
	Write-Host "Algunas pruebas fallaron. Revisa el reporte postman/newman-report.json" -ForegroundColor Red
	exit $codigo
}

Write-Host ""
Write-Host "Todas las pruebas pasaron." -ForegroundColor Green