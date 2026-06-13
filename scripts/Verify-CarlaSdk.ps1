param(
    [string]$CarlaIncludeDir = $env:CARLA_INCLUDE_DIR,
    [string]$CarlaLibDir = $env:CARLA_LIB_DIR,
    [switch]$RequireCompiler
)

$ErrorActionPreference = "Stop"

function Write-Check {
    param([string]$Message)
    Write-Host "[check] $Message"
}

function Fail {
    param([string]$Message)
    Write-Host "[error] $Message" -ForegroundColor Red
    exit 1
}

Write-Check "Validating Java"
if (-not $env:JAVA_HOME) {
    Fail "JAVA_HOME is not set. Example: `$env:JAVA_HOME = (Resolve-Path 'tools\jdk-25\jdk-25.0.3+9').Path"
}

$javaExe = Join-Path $env:JAVA_HOME "bin\java.exe"
if (-not (Test-Path $javaExe)) {
    Fail "java.exe was not found at $javaExe"
}
& $javaExe -version

Write-Check "Validating Maven"
$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvn) {
    Fail "mvn was not found in PATH. Install Maven 3.9+ or add it to PATH."
}
Write-Host "Maven: $($mvn.Source)"

Write-Check "Validating CARLA_INCLUDE_DIR"
if (-not $CarlaIncludeDir) {
    Fail "CARLA_INCLUDE_DIR is not set."
}
if (-not (Test-Path $CarlaIncludeDir)) {
    Fail "CARLA_INCLUDE_DIR does not exist: $CarlaIncludeDir"
}

$requiredHeaders = @(
    "carla\Time.h",
    "carla\client\Client.h",
    "carla\client\World.h",
    "carla\client\Actor.h",
    "carla\client\BlueprintLibrary.h",
    "carla\geom\Transform.h"
)

foreach ($header in $requiredHeaders) {
    $path = Join-Path $CarlaIncludeDir $header
    if (-not (Test-Path $path)) {
        Fail "Missing CARLA header: $path"
    }
}

Write-Check "Validating CARLA_LIB_DIR"
if (-not $CarlaLibDir) {
    Fail "CARLA_LIB_DIR is not set."
}
if (-not (Test-Path $CarlaLibDir)) {
    Fail "CARLA_LIB_DIR does not exist: $CarlaLibDir"
}

$libraryPatterns = @(
    "carla_client.lib",
    "libcarla_client.lib",
    "libcarla_client.a",
    "libcarla_client.so",
    "libcarla_client.dylib"
)

$foundLibs = @()
foreach ($pattern in $libraryPatterns) {
    $foundLibs += Get-ChildItem -Path $CarlaLibDir -Filter $pattern -ErrorAction SilentlyContinue
}

if ($foundLibs.Count -eq 0) {
    Write-Host ""
    Write-Host "Tip: the CARLA WindowsNoEditor package may contain headers under Plugins\carlaviz\backend\third_party\LibCarla\source, but not the linkable C++ client library." -ForegroundColor Yellow
    Write-Host "Build LibCarla from source or provide a CARLA SDK/lib directory that contains the client library." -ForegroundColor Yellow
    Fail "No CARLA client library found in $CarlaLibDir. Expected one of: $($libraryPatterns -join ', ')"
}

Write-Host "Found CARLA client library:"
$foundLibs | ForEach-Object { Write-Host "  $($_.FullName)" }

if ($RequireCompiler) {
    Write-Check "Validating native C++ compiler"
    $cl = Get-Command cl.exe -ErrorAction SilentlyContinue
    $gpp = Get-Command g++ -ErrorAction SilentlyContinue
    $clangpp = Get-Command clang++ -ErrorAction SilentlyContinue

    if (-not $cl -and -not $gpp -and -not $clangpp) {
        Fail "No native C++ compiler found. On Windows, run from a Visual Studio x64 Native Tools prompt or install Build Tools."
    }

    if ($cl) { Write-Host "MSVC: $($cl.Source)" }
    if ($gpp) { Write-Host "g++: $($gpp.Source)" }
    if ($clangpp) { Write-Host "clang++: $($clangpp.Source)" }
}

Write-Host ""
Write-Host "Environment looks ready. Run:"
Write-Host "  mvn -Pnative test"
