param(
    [string]$CarlaRoot = (Join-Path (Resolve-Path ".").Path "CARLA_0.9.16"),
    [int]$RpcPort = 2000,
    [ValidateSet("Low", "Epic")]
    [string]$QualityLevel = "Low",
    [int]$ResX = 1280,
    [int]$ResY = 720
)

$ErrorActionPreference = "Stop"

$exe = Join-Path $CarlaRoot "CarlaUE4.exe"
if (-not (Test-Path $exe)) {
    Write-Host "[error] CarlaUE4.exe not found at $exe" -ForegroundColor Red
    exit 1
}

Write-Host "Starting CARLA:"
Write-Host "  $exe"
Write-Host "  RPC port: $RpcPort"
Write-Host "  Quality: $QualityLevel"

& $exe "-carla-rpc-port=$RpcPort" "-quality-level=$QualityLevel" "-resx=$ResX" "-resy=$ResY"
