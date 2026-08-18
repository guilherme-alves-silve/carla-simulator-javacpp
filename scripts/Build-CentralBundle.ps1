param(
    [string]$Version = "0.1.0",
    [string]$Platform = "",
    [string]$GpgKey = "guilherme_alves_silve@hotmail.com"
)

$ErrorActionPreference = "Stop"

# Locate gpg.exe. Honor $env:GPG_EXECUTABLE first, then fall back
# to the common install locations on Windows. We need a real
# GnuPG (or Gpg4win with command-line tools) for detached signing;
# the gpg shipped with Git for Windows is intentionally crippled
# and refuses to write to %APPDATA%\gnupg, so we explicitly avoid
# it.
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
    throw "gpg.exe not found. Install GnuPG (https://gnupg.org) or Gpg4win (https://gpg4win.org) with command-line tools, then re-run, or set `$env:GPG_EXECUTABLE."
}

# Auto-detect the native platform classifier when not provided.
if ([string]::IsNullOrEmpty($Platform)) {
    $isWindows = ($null -ne $IsWindows -and $IsWindows) `
        -or [System.Environment]::OSVersion.Platform -eq "Win32NT"
    $isLinux = ($null -ne $IsLinux -and $IsLinux) `
        -or [System.Environment]::OSVersion.Platform -eq "Unix"
    if ($isWindows) {
        $Platform = "windows-x86_64"
    } elseif ($isLinux) {
        $Platform = "linux-x86_64"
    } else {
        throw "Unsupported host OS. Pass -Platform explicitly (e.g. windows-x86_64 or linux-x86_64)."
    }
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$target = Join-Path $root "target"
$stagingRoot = Join-Path $target "central-staging"
$groupPath = "io/github/guilherme-alves-silve/carla-simulator-javacpp/$Version"
$stagingDir = Join-Path $stagingRoot $groupPath

# Artifacts required by the Central Portal. The native classifier
# jar is optional here: when present it is signed and bundled
# alongside; when absent, the upload only includes the main,
# sources and javadoc artifacts plus the pom.
$artifacts = @(
    "carla-simulator-javacpp-$Version.jar",
    "carla-simulator-javacpp-$Version-sources.jar",
    "carla-simulator-javacpp-$Version-javadoc.jar"
)

# Sanity check: every artifact must exist before we proceed.
foreach ($a in $artifacts) {
    $p = Join-Path $target $a
    if (-not (Test-Path $p)) {
        throw "Missing artifact: $p. Run 'mvn -Pnative clean package -DskipTests' first."
    }
}

# Wipe and recreate the staging directory. We keep the staging
# under <group-path> so the contents can be zipped with the right
# internal structure (no extra wrapper directory that would break
# the Central validator).
if (Test-Path $stagingRoot) {
    Remove-Item -Recurse -Force $stagingRoot
}
New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null

foreach ($a in $artifacts) {
    Copy-Item -Path (Join-Path $target $a) -Destination $stagingDir
}
Copy-Item -Path (Join-Path $root "pom.xml") `
    -Destination (Join-Path $stagingDir "carla-simulator-javacpp-$Version.pom")

$nativeJar = Join-Path $target "carla-simulator-javacpp-$Version-$Platform.jar"
$includeNative = Test-Path $nativeJar
if ($includeNative) {
    Copy-Item $nativeJar $stagingDir
}

# Generate detached GPG signatures (ASCII-armored) and MD5/SHA1
# checksums for every primary artifact. Checksum files are plain
# text with just the hash value, as required by the Central Portal.
$filesToProcess = @($artifacts)
if ($includeNative) {
    $filesToProcess += "carla-simulator-javacpp-$Version-$Platform.jar"
}
$filesToProcess += "carla-simulator-javacpp-$Version.pom"

foreach ($f in $filesToProcess) {
    $path = Join-Path $stagingDir $f

    Write-Host "Signing $f ..."
    # gpg writes its "using key XYZ" diagnostic to stderr, which
    # PowerShell mis-renders as a non-zero exit. Redirect stderr
    # away (but capture it for failure reporting) and trust the
    # actual exit code via $LASTEXITCODE.
    $stderr = ""
    & $gpgExe --armor --detach-sign --default-key $GpgKey --output "$path.asc" $path 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "gpg failed for $f (exit code $LASTEXITCODE)"
    }

    $md5 = (Get-FileHash -Path $path -Algorithm MD5).Hash.ToLower()
    $sha1 = (Get-FileHash -Path $path -Algorithm SHA1).Hash.ToLower()
    Set-Content -Path "$path.md5" -Value $md5 -NoNewline -Encoding ASCII
    Set-Content -Path "$path.sha1" -Value $sha1 -NoNewline -Encoding ASCII
}

# Build the final ZIP. We use .NET ZipFile directly so the
# internal layout is exactly <group-path>/<filename> with no
# extra wrapper directory. (The PowerShell Compress-Archive
# cmdlet always wraps a directory in a same-named folder, which
# the Central Portal rejects.)
$zipPath = Join-Path $target "central-bundle-$Version.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath }

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$zip = [System.IO.Compression.ZipFile]::Open(
    $zipPath,
    [System.IO.Compression.ZipArchiveMode]::Create)

$entryPrefix = "$groupPath/"
Get-ChildItem $stagingDir -File | ForEach-Object {
    $entryName = $entryPrefix + $_.Name
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
        $zip, $_.FullName, $entryName,
        [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
}
$zip.Dispose()

Write-Host ""
Write-Host "Bundle ready: $zipPath"
Write-Host ""
Write-Host "Internal layout:"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$verify = [System.IO.Compression.ZipFile]::OpenRead($zipPath)
$verify.Entries | Sort-Object FullName | ForEach-Object {
    Write-Host ("  {0,-90} {1,10:N0} bytes" -f $_.FullName, $_.Length)
}
$verify.Dispose()
