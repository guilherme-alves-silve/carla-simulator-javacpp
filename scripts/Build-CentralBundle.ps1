param(
    [string]$Version = "0.1.0",
    [string]$Platform = "",
    [string]$GpgKey = "guilherme_alves_silve@hotmail.com"
)

$ErrorActionPreference = "Continue"

# Locate gpg.exe
$gpgExe = [string]::Empty
if ($null -ne $env:GPG_EXECUTABLE -and $env:GPG_EXECUTABLE -ne "") {
    $gpgExe = $env:GPG_EXECUTABLE
}
if ($gpgExe -eq "" -or -not (Test-Path $gpgExe)) {
    $candidates = @(
        "C:\Program Files\GnuPG\bin\gpg.exe",
        "C:\Program Files (x86)\GnuPG\bin\gpg.exe",
        "C:\msys64\usr\bin\gpg.exe"
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { $gpgExe = $c; break }
    }
}
if ($gpgExe -eq "" -or -not (Test-Path $gpgExe)) {
    throw "gpg.exe not found."
}

Write-Host "Using GPG: $gpgExe"

# Test GPG key
Write-Host "Checking GPG key: $GpgKey"
$keyCheck = & $gpgExe --list-keys $GpgKey 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Key not found!" -ForegroundColor Red
    Write-Host $keyCheck
    exit 1
} else {
    Write-Host "Key found!" -ForegroundColor Green
}

# Auto-detect platform
if ([string]::IsNullOrEmpty($Platform)) {
    $isWindows = ($null -ne $IsWindows -and $IsWindows) -or [System.Environment]::OSVersion.Platform -eq "Win32NT"
    $isLinux = ($null -ne $IsLinux -and $IsLinux) -or [System.Environment]::OSVersion.Platform -eq "Unix"
    if ($isWindows) {
        $Platform = "windows-x86_64"
    } elseif ($isLinux) {
        $Platform = "linux-x86_64"
    } else {
        throw "Unsupported host OS."
    }
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$target = Join-Path $root "target"
$stagingRoot = Join-Path $target "central-staging"
$groupPath = "io/github/guilherme-alves-silve/carla-simulator-javacpp/$Version"
$stagingDir = Join-Path $stagingRoot $groupPath

$artifacts = @(
    "carla-simulator-javacpp-$Version.jar",
    "carla-simulator-javacpp-$Version-sources.jar",
    "carla-simulator-javacpp-$Version-javadoc.jar"
)

foreach ($a in $artifacts) {
    $p = Join-Path $target $a
    if (-not (Test-Path $p)) {
        throw "Missing artifact: $p"
    }
}

if (Test-Path $stagingRoot) {
    Remove-Item -Recurse -Force $stagingRoot
}
New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null

foreach ($a in $artifacts) {
    Copy-Item -Path (Join-Path $target $a) -Destination $stagingDir
}
Copy-Item -Path (Join-Path $root "pom.xml") -Destination (Join-Path $stagingDir "carla-simulator-javacpp-$Version.pom")

$nativeJar = Join-Path $target "carla-simulator-javacpp-$Version-$Platform.jar"
$includeNative = Test-Path $nativeJar
if ($includeNative) {
    Copy-Item $nativeJar $stagingDir
}

$filesToProcess = @($artifacts)
if ($includeNative) {
    $filesToProcess += "carla-simulator-javacpp-$Version-$Platform.jar"
}
$filesToProcess += "carla-simulator-javacpp-$Version.pom"

foreach ($f in $filesToProcess) {
    $path = Join-Path $stagingDir $f
    Write-Host "Signing $f ..."

    # Try to sign with GPG
    & $gpgExe --armor --detach-sign --default-key $GpgKey --output "$path.asc" $path 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "GPG failed with exit code: $LASTEXITCODE" -ForegroundColor Red
        throw "gpg failed for $f"
    }

    if (-not (Test-Path "$path.asc")) {
        Write-Host "Signature file not created!" -ForegroundColor Red
        throw "Failed to create signature for $f"
    }

    Write-Host "  Signature created: $path.asc" -ForegroundColor Green

    $md5 = (Get-FileHash -Path $path -Algorithm MD5).Hash.ToLower()
    $sha1 = (Get-FileHash -Path $path -Algorithm SHA1).Hash.ToLower()
    Set-Content -Path "$path.md5" -Value $md5 -NoNewline -Encoding ASCII
    Set-Content -Path "$path.sha1" -Value $sha1 -NoNewline -Encoding ASCII

    Write-Host "  Checksums generated" -ForegroundColor Green
}

# Build the ZIP
$zipPath = Join-Path $target "central-bundle-$Version.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath }

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$zip = [System.IO.Compression.ZipFile]::Open($zipPath, [System.IO.Compression.ZipArchiveMode]::Create)

$entryPrefix = "$groupPath/"
Get-ChildItem $stagingDir -File | ForEach-Object {
    $entryName = $entryPrefix + $_.Name
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $entryName, [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
}
$zip.Dispose()

Write-Host ""
Write-Host "Bundle ready: $zipPath" -ForegroundColor Green
Write-Host ""
Write-Host "Bundle contents:"

$verify = [System.IO.Compression.ZipFile]::OpenRead($zipPath)
$verify.Entries | Sort-Object FullName | ForEach-Object {
    $name = $_.FullName
    $size = $_.Length
    Write-Host "  $name ($size bytes)"
}
$verify.Dispose()
