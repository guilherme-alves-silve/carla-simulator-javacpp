param(
    [string]$Version = "0.1.0-SNAPSHOT",
    # Native platform classifier. Auto-detected from the host OS by
    # default; override with -Platform when installing an artifact
    # built on a different host.
    [string]$Platform = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrEmpty($Platform)) {
    $isWindows = ($null -ne $IsWindows -and $IsWindows) `
        -or [System.Environment]::OSVersion.Platform -eq "Win32NT"
    $isLinux = ($null -ne $IsLinux -and $IsLinux) `
        -or [System.Environment]::OSVersion.Platform -eq "Unix"
    $arch = [System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture.ToString().ToLower()
    if ($arch -ne "x64") {
        throw "Unsupported architecture '$arch'. Only x86_64 is supported."
    }
    if ($isWindows) {
        $Platform = "windows-x86_64"
    } elseif ($isLinux) {
        $Platform = "linux-x86_64"
    } else {
        throw "Unsupported host OS. Pass -Platform explicitly (e.g. windows-x86_64 or linux-x86_64)."
    }
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$jar = Join-Path $root "target\carla-simulator-javacpp-$Version.jar"
$nativeJar = Join-Path $root "target\carla-simulator-javacpp-$Version-$Platform.jar"

if (-not (Test-Path $jar)) {
    throw "Main JAR not found: $jar. Run the native package build first."
}

if (-not (Test-Path $nativeJar)) {
    throw "Native JAR not found: $nativeJar. Run the native package build first."
}

mvn install:install-file `
    "-Dfile=$jar" `
    "-DgroupId=org.carla" `
    "-DartifactId=carla-simulator-javacpp" `
    "-Dversion=$Version" `
    "-Dpackaging=jar"

mvn install:install-file `
    "-Dfile=$nativeJar" `
    "-DgroupId=org.carla" `
    "-DartifactId=carla-simulator-javacpp" `
    "-Dversion=$Version" `
    "-Dclassifier=$Platform" `
    "-Dpackaging=jar"
