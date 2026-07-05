param(
    [string]$Version = "0.1.0-SNAPSHOT",
    [string]$Platform = "windows-x86_64"
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$jar = Join-Path $root "target\carla-javacpp-integration-$Version.jar"
$nativeJar = Join-Path $root "target\carla-javacpp-integration-$Version-$Platform.jar"

if (-not (Test-Path $jar)) {
    throw "Main JAR not found: $jar. Run the native package build first."
}

if (-not (Test-Path $nativeJar)) {
    throw "Native JAR not found: $nativeJar. Run the native package build first."
}

mvn install:install-file `
    "-Dfile=$jar" `
    "-DgroupId=org.carla" `
    "-DartifactId=carla-javacpp-integration" `
    "-Dversion=$Version" `
    "-Dpackaging=jar"

mvn install:install-file `
    "-Dfile=$nativeJar" `
    "-DgroupId=org.carla" `
    "-DartifactId=carla-javacpp-integration" `
    "-Dversion=$Version" `
    "-Dclassifier=$Platform" `
    "-Dpackaging=jar"
