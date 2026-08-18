# CARLA JavaCPP Integration

Small Java integration layer for CARLA using JavaCPP and the CARLA C++ client SDK.

The supported path is intentionally small:

```text
Java API -> JavaCPP JNI -> small C++ bridge -> CARLA C++ client SDK -> CARLA simulator
```

JavaCPP is used to generate and compile the JNI glue for `CarlaNative`. This project does not currently generate Java classes from the full CARLA SDK headers with the JavaCPP Parser.

## Current API

Implemented surface:

- `Client(host, port)`
- `Client.setTimeout(Duration)`
- `Client.getWorld()`
- `World.getMapName()`
- `World.getMap()`
- `World.getBlueprintLibrary()`
- `World.getActors()`
- `World.getSpawnPoints()`
- `World.spawnActor(...)`
- `World.trySpawnActor(...)`
- `World.spawnVehicle(...)`
- `World.trySpawnVehicle(...)`
- `World.spawnRgbCamera(...)`
- `World.spawnCollisionSensor(...)`
- `World.spawnLidar(...)`
- `BlueprintLibrary.filter(pattern)`
- `Blueprint.setAttribute(key, value)`
- `Actor.getId()`
- `Actor.getTypeId()`
- `Actor.getTransform()`
- `Actor.getLocation()`
- `Actor.destroy()`
- `Map.getName()`
- `Map.getWaypoint(Location)`
- `Map.generateWaypoints(distance)`
- `Waypoint.getId()`
- `Waypoint.getRoadId()`
- `Waypoint.getSectionId()`
- `Waypoint.getLaneId()`
- `Waypoint.getDistance()`
- `Waypoint.getTransform()`
- `Waypoint.isJunction()`
- `Waypoint.getLaneWidth()`
- `Waypoint.next(distance)`
- `Waypoint.previous(distance)`
- `Waypoint.getRight()`
- `Waypoint.getLeft()`
- `Vehicle.applyControl(VehicleControl)`
- `Camera.listen(...)` / `Camera.pollImage(...)`
- `CollisionSensor.listen(...)` / `CollisionSensor.pollEvent(...)`
- `LidarSensor.listen(...)` / `LidarSensor.pollMeasurement(...)`

## Example

```java
try (var client = new Client("localhost", 2000)) {
    client.setTimeout(Duration.ofSeconds(10));

    try (var world = client.getWorld();
         var blueprints = world.getBlueprintLibrary()) {
        List<Blueprint> vehicles = blueprints.filter("vehicle.*");
        var blueprint = vehicles.get(0)
            .setAttribute("role_name", "hero");

        List<Transform> spawnPoints = world.getSpawnPoints();
        var spawnPoint = spawnPoints.get(0);

        try (var vehicle = world.spawnVehicle(blueprint, spawnPoint)) {
            var control = new VehicleControl().throttle(0.5f);
            vehicle.applyControl(control);
        }
    }
}
```

## Prerequisites

- JDK 17+; this workspace uses `tools\jdk-17`
- Maven 3.9+
- Visual Studio Build Tools x64 on Windows
- local `carla-sdk` with `include/` and `lib/`
- CARLA simulator running separately

JavaCPP is pinned to `1.5.10` in `pom.xml`.

`Map` and `Waypoint` are wrappers over the original CARLA C++ client API (`carla::client::Map` and `carla::client::Waypoint`). They are exposed so Java route planning code can stay close to the Python API instead of reimplementing map access from scratch.

Do not commit `carla-sdk/`, `CARLA_*/`, `target/`, `tools/`, JARs, or DLLs.

The local CARLA C++ SDK used here was made possible with help from this Windows build tutorial: [Building CARLA from Source on Windows 10/11 with Visual Studio 2022](https://wambitz.github.io/tech-blog/carla/python/c%2B%2B/simulation/autonomous-vehicles/2024/09/29/carla-win11.html).

The workflow used for this project was not based on copying only a prebuilt SDK from the tutorial. A full CARLA source repository was cloned locally, and only the LibCarla/C++ client SDK pieces needed by this JavaCPP integration were compiled and extracted.

The `carla-sdk/` folder was assembled from the compiled CARLA source tree by copying `LibCarla\source`, CARLA dependency includes, `.lib` files, and DLLs into `include/`, `lib/`, and `bin/`. A small `test_carla.cpp` program was compiled with `cl` first to verify that `carla::client::Client` could connect to the simulator before wiring JavaCPP. The exact script and command are recorded in [docs/WORKFLOW.md](docs/WORKFLOW.md).

## Build

The native platform classifier (`carla.native.platform`) is
detected at build time by the
[`os-maven-plugin`](https://github.com/trustin/os-maven-plugin)
extension and exposed as `${os.detected.classifier}` (for example
`windows-x86_64` or `linux-x86_64`). It is consumed by the
`maven-jar-plugin` for the platform-specific classifier and by the
JavaCPP `maven-compiler-plugin` to select the matching
`@Platform` entry on `CarlaNative`. To force a specific
classifier (for instance, when cross-building), pass
`-Dcarla.native.platform=linux-x86_64` on the command line.

Windows:
```powershell
cmd.exe /s /c "set `"JAVA_HOME=J:\tools\jdk-17`" && set `"PATH=J:\tools\jdk-17\bin;%PATH%`" && set `"CARLA_INCLUDE_DIR=J:\carla-sdk\include`" && set `"CARLA_LIB_DIR=J:\carla-sdk\lib`" && `"C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat`" -arch=x64 -host_arch=x64 >nul && mvn -Pnative clean package -DskipTests"
```

Linux (Ubuntu 22.04, inside WSL or a native install — see
[BUILDING_LIBCARLA_LINUX.md](BUILDING_LIBCARLA_LINUX.md) for the
LibCarla build that feeds the SDK in):
```bash
export JAVA_HOME="$HOME/carla-simulator-javacpp/tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export CARLA_INCLUDE_DIR="$HOME/carla-simulator-javacpp/carla-sdk/include"
export CARLA_LIB_DIR="$HOME/carla-simulator-javacpp/carla-sdk/lib"
mvn -Pnative clean package -DskipTests
```

Output (the classifier suffix is the value of
`${os.detected.classifier}` for the host that ran the build):

```text
target/carla-simulator-javacpp-0.1.0-SNAPSHOT.jar
target/carla-simulator-javacpp-0.1.0-SNAPSHOT-{windows,linux}-x86_64.jar
target/carla-simulator-javacpp-0.1.0-SNAPSHOT-sources.jar
target/carla-simulator-javacpp-0.1.0-SNAPSHOT-javadoc.jar
```

The last two jars are produced automatically by the build
(`maven-source-plugin` and `maven-javadoc-plugin`, both bound to
the `package` phase). The sources jar contains only the
`org.carla.javacpp.api` sources (the JavaCPP-generated binding
and the bundled examples are excluded), and the javadoc jar
contains the browsable HTML documentation for the same surface.

## Install In Local Maven

```powershell
.\scripts\Install-LocalArtifacts.ps1
```

Run this after every API or native bridge change before testing from another Maven project.

## Run Examples

```powershell
.\scripts\Start-Carla.ps1 -QualityLevel Low
```

Then run from your IDE or compile/run:

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

`CarlaCameraViewer` is the manual driving example with an RGB camera attached behind the vehicle.

The corresponding original Python examples copied from CARLA are under:

```text
src/examples/python/original
```

Each Java example points to the Python file it was based on, and each copied Python file points back to the Java alternative.

The examples live under `src/examples`, outside `src/main`, so Maven does not include them in the final library JAR.

The copied CARLA examples are MIT-licensed. Keep their original copyright headers and see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the original source link and license text.

## Tests

Unit tests do not require a running simulator:

```powershell
mvn test
```

Integration tests require CARLA running on `localhost:2000` and are opt-in:

```powershell
mvn -Pintegration-tests verify
```

## Project Files

- `src/main/cpp/CarlaBridge.h`
- `src/main/cpp/CarlaBridge.cpp`
- `src/main/java/org/carla/javacpp/binding/CarlaNative.java`
- `src/main/java/org/carla/javacpp/api`
- `src/examples/java`
- `src/examples/python/original`
- `scripts/Install-LocalArtifacts.ps1`

More detail is in [docs/WORKFLOW.md](docs/WORKFLOW.md).

## Generate API Documentation

The public surface of this project is documented in two ways:

- **Javadoc** for the Java API (`org.carla.javacpp.api`).
- **Doxygen** for the C++ native bridge (`src/main/cpp/CarlaBridge.h`).

The Javadoc is configured in `pom.xml` through
`maven-javadoc-plugin`, and the Doxygen config lives at
`docs/Doxyfile`. The Maven configuration excludes the auto-generated
`org.carla.javacpp.binding` package and the
`org.carla.javacpp.examples` package (which is not part of the
public library), so only the user-facing API is documented.

### Generate Javadoc (Java API)

From the project root, with `JAVA_HOME` pointing at JDK 17+ and
`mvn` on the `PATH`:

```powershell
mvn javadoc:javadoc
```

The generated HTML is written to:

```text
target/site/apidocs/index.html
```

To package a browsable JAR alongside the library JAR (without
re-running the Javadoc toolchain by hand), use:

```powershell
mvn javadoc:jar
```

The JAR is written to:

```text
target/carla-simulator-javacpp-0.1.0-SNAPSHOT-javadoc.jar
```

On Windows, if you keep the JDK inside `tools\jdk-17` as the
build instructions describe, the full one-liner is:

```powershell
cmd.exe /s /c "set `"JAVA_HOME=%~dp0tools\jdk-17`" && set `"PATH=%~dp0tools\jdk-17\bin;%PATH%`" && mvn javadoc:javadoc"
```

### Generate Doxygen (C++ Native Bridge)

`docs/Doxyfile` is a Doxygen configuration that scans
`src/main/cpp/CarlaBridge.h` and `src/main/cpp/CarlaBridge.cpp` and
writes the HTML output to `docs/api/cpp/`.

Doxygen is the only external tool required; the JDK is not used.
Install it from <https://www.doxygen.nl/download.html> (or via
your package manager of choice), then run from the project root:

```powershell
doxygen docs/Doxyfile
```

The output is written to:

```text
docs/api/cpp/index.html
```

`docs/api/` is listed in `.gitignore`; the generated HTML is a
local build artifact and should not be committed.
