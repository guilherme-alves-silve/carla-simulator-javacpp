# Build and Local Usage Workflow

This document records the workflow that is actually used by this project.

## What This Project Builds

The supported integration is:

```text
Java API -> JavaCPP JNI -> small C++ bridge -> CARLA C++ client SDK -> CARLA simulator
```

JavaCPP is used to generate and compile the JNI layer for `CarlaNative`.
This project does not currently generate Java classes from the full CARLA SDK headers with the JavaCPP Parser.

## External Files

Keep these files out of Git:

- `carla-sdk/`
- `CARLA_*/`
- `target/`
- `tools/`
- generated JARs and DLLs

The expected local CARLA SDK layout is:

```text
carla-sdk/
  include/
    carla/...
    boost/...
  lib/
    carla_client.lib
    rpc.lib
    Recast.lib
    Detour.lib
    DetourCrowd.lib
    DetourTileCache.lib
```

The local Windows CARLA build/SDK used by this project was produced with help from this tutorial:

- [Building CARLA from Source on Windows 10/11 with Visual Studio 2022](https://wambitz.github.io/tech-blog/carla/python/c%2B%2B/simulation/autonomous-vehicles/2024/09/29/carla-win11.html)

## How `carla-sdk` Was Assembled

After compiling CARLA from source, this PowerShell script was used to copy the required headers, libraries, and DLLs into a smaller local SDK folder:

```powershell
$CARLA="C:\Users\vboxuser\Documents\carla-source-full-0.9.16"
$DEST="C:\Users\vboxuser\Documents\carla-sdk"

Write-Host "Creating folders..."

New-Item -ItemType Directory -Force "$DEST\include" | Out-Null
New-Item -ItemType Directory -Force "$DEST\lib" | Out-Null
New-Item -ItemType Directory -Force "$DEST\bin" | Out-Null

Write-Host "Copying headers..."

Copy-Item `
    "$CARLA\LibCarla\source\*" `
    "$DEST\include\" `
    -Recurse -Force

Copy-Item `
    "$CARLA\Unreal\CarlaUE4\Plugins\Carla\CarlaDependencies\include\*" `
    "$DEST\include\" `
    -Recurse -Force

Write-Host "Copying libraries..."

Copy-Item `
    "$CARLA\PythonAPI\carla\dependencies\lib\*.lib" `
    "$DEST\lib\" `
    -Force

Copy-Item `
    "$CARLA\Unreal\CarlaUE4\Plugins\Carla\CarlaDependencies\lib\*.lib" `
    "$DEST\lib\" `
    -Force

Write-Host "Searching DLLs..."

Get-ChildItem `
    $CARLA `
    -Recurse `
    -Filter *.dll `
    | ForEach-Object {
        Copy-Item $_.FullName "$DEST\bin\" -Force
    }

Write-Host ""
Write-Host "Finished."
Write-Host ""
Write-Host "Include: $DEST\include"
Write-Host "Lib:     $DEST\lib"
Write-Host "Bin:     $DEST\bin"
```

In this repository, that SDK is expected at:

```text
J:\carla_javacpp_integration\carla-sdk
```

## C++ SDK Smoke Test

Before wiring JavaCPP, the copied SDK was validated with a small C++ program named `test_carla.cpp`:

```cpp
#include <iostream>
#include <carla/client/Client.h>

int main() {
    try {
        carla::client::Client client("localhost", 2000);
        client.SetTimeout(std::chrono::seconds(5));

        auto world = client.GetWorld();

        std::cout << "Connected!" << std::endl;
        std::cout << world.GetMap()->GetName() << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << std::endl;
    }

    return 0;
}
```

The manual compile command used for that validation was:

```bat
cl /EHsc /MD ^
 /I"C:\Users\vboxuser\Documents\carla-sdk\include" ^
 test_carla.cpp ^
 /link ^
 /LIBPATH:"C:\Users\vboxuser\Documents\carla-sdk\lib" ^
 carla_client.lib ^
 rpc.lib ^
 Recast.lib ^
 Detour.lib ^
 DetourCrowd.lib ^
 DetourTileCache.lib ^
 DebugUtils.lib ^
 libboost_filesystem-vc143-mt-x64-1_84.lib ^
 libboost_system-vc143-mt-x64-1_84.lib ^
 shlwapi.lib ^
 ws2_32.lib ^
 iphlpapi.lib
```

This proved that the CARLA C++ client could connect to a running simulator before adding the JavaCPP bridge.

## Build

Use JavaCPP `1.5.10`, pinned in `pom.xml`.

From a normal PowerShell in the repository root:

```powershell
cmd.exe /s /c "set `"JAVA_HOME=J:\carla_javacpp_integration\tools\jdk-25\jdk-25.0.3+9`" && set `"PATH=J:\carla_javacpp_integration\tools\jdk-25\jdk-25.0.3+9\bin;%PATH%`" && set `"CARLA_INCLUDE_DIR=J:\carla_javacpp_integration\carla-sdk\include`" && set `"CARLA_LIB_DIR=J:\carla_javacpp_integration\carla-sdk\lib`" && `"C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat`" -arch=x64 -host_arch=x64 >nul && mvn -Pnative clean package -DskipTests"
```

Successful output creates:

```text
target/carla-javacpp-integration-0.1.0-SNAPSHOT.jar
target/carla-javacpp-integration-0.1.0-SNAPSHOT-windows-x86_64.jar
```

The first JAR contains Java classes. The second JAR contains the Windows native `jniCarlaNative.dll`.

## Install Locally

After every API or native bridge change, install both JARs into the local Maven repository:

```powershell
.\scripts\Install-LocalArtifacts.ps1
```

This installs:

```text
org.carla:carla-javacpp-integration:0.1.0-SNAPSHOT
org.carla:carla-javacpp-integration:0.1.0-SNAPSHOT:windows-x86_64
```

## Main Project Dependency

In another Maven project, use:

```xml
<dependency>
    <groupId>org.carla</groupId>
    <artifactId>carla-javacpp-integration</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>org.carla</groupId>
    <artifactId>carla-javacpp-integration</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <classifier>windows-x86_64</classifier>
</dependency>
```

## Run Examples

Start CARLA first, then run one of:

```text
src/examples/java/CarlaJavaSmokeTest.java
src/examples/java/CarlaCameraViewer.java
src/examples/java/CarlaTutorialExample.java
src/examples/java/CarlaSensorSynchronizationExample.java
src/examples/java/CarlaLidarViewer.java
src/examples/java/CarlaMultiSensorViewer.java
src/examples/java/CarlaWeatherExample.java
src/examples/java/CarlaTrafficExample.java
src/examples/java/CarlaStartRecordingExample.java
src/examples/java/CarlaReplayRecordingExample.java
src/examples/java/CarlaRecorderFileInfoExample.java
src/examples/java/CarlaRecorderCollisionsExample.java
src/examples/java/CarlaRecorderActorsBlockedExample.java
```

`CarlaCameraViewer` uses map spawn points and `trySpawnVehicle`, then attaches an RGB camera behind the vehicle for manual driving.

Recorder examples mirror CARLA's Python recorder scripts: start recording, replay a file, inspect file info, list collisions, and list blocked actors.

These files are intentionally under `src/examples`, not `src/main`, so they can be opened or marked as a source root in the IDE without becoming part of the packaged library JAR.

The original Python examples copied from CARLA are stored in:

```text
src/examples/python/original
```

Only a small cross-reference docstring was added to each copied Python file:

```python
"""
Java alternative: ExampleName.java
"""
```

Each Java alternative starts with:

```java
/** Based on the Python example: file_name.py */
```

The copied Python examples come from CARLA 0.9.16:

```text
https://github.com/carla-simulator/carla/tree/0.9.16/PythonAPI/examples
```

They are covered by CARLA's MIT License. Keep the original copyright headers in those files and keep [../THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md) with the repository.

## Tests

Run unit tests:

```powershell
mvn test
```

Run integration tests only when the CARLA simulator is already running:

```powershell
mvn -Pintegration-tests verify
```

The integration tests live under:

```text
src/integration-test/java
```

## Directly Included CARLA Headers

The bridge directly includes only:

```text
carla/Memory.h
carla/client/Actor.h
carla/client/ActorList.h
carla/client/ActorBlueprint.h
carla/client/BlueprintLibrary.h
carla/client/Client.h
carla/client/Map.h
carla/client/Sensor.h
carla/client/Vehicle.h
carla/client/World.h
carla/geom/Location.h
carla/geom/Rotation.h
carla/geom/Transform.h
carla/rpc/ActorId.h
carla/rpc/VehicleControl.h
carla/sensor/SensorData.h
carla/sensor/data/CollisionEvent.h
carla/sensor/data/Image.h
carla/sensor/data/LidarMeasurement.h
boost/shared_ptr.hpp
```

The full `carla-sdk/include` directory is still required because those headers include many transitive dependencies.
