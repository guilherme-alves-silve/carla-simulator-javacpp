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
- `Actor.destroy()`
- `Vehicle.applyControl(VehicleControl)`
- `Camera.listen(...)` / `Camera.pollImage(...)`
- `CollisionSensor.listen(...)` / `CollisionSensor.pollEvent(...)`
- `LidarSensor.listen(...)` / `LidarSensor.pollMeasurement(...)`

## Example

```java
try (Client client = new Client("localhost", 2000)) {
    client.setTimeout(Duration.ofSeconds(10));

    try (World world = client.getWorld();
         BlueprintLibrary blueprints = world.getBlueprintLibrary()) {
        Blueprint blueprint = blueprints.filter("vehicle.*")
            .get(0)
            .setAttribute("role_name", "hero");

        Transform spawnPoint = world.getSpawnPoints().get(0);
        try (Vehicle vehicle = world.spawnVehicle(blueprint, spawnPoint)) {
            vehicle.applyControl(new VehicleControl().throttle(0.5f));
        }
    }
}
```

## Prerequisites

- JDK 17+; this workspace uses `tools\jdk-25\jdk-25.0.3+9`
- Maven 3.9+
- Visual Studio Build Tools x64 on Windows
- local `carla-sdk` with `include/` and `lib/`
- CARLA simulator running separately

JavaCPP is pinned to `1.5.10` in `pom.xml`.

Do not commit `carla-sdk/`, `CARLA_*/`, `target/`, `tools/`, JARs, or DLLs.

The local CARLA C++ SDK used here was made possible by following this Windows build tutorial: [Building CARLA from Source on Windows 10/11 with Visual Studio 2022](https://wambitz.github.io/tech-blog/carla/python/c%2B%2B/simulation/autonomous-vehicles/2024/09/29/carla-win11.html).

The `carla-sdk/` folder was assembled from the compiled CARLA source tree by copying `LibCarla\source`, CARLA dependency includes, `.lib` files, and DLLs into `include/`, `lib/`, and `bin/`. A small `test_carla.cpp` program was compiled with `cl` first to verify that `carla::client::Client` could connect to the simulator before wiring JavaCPP. The exact script and command are recorded in [docs/WORKFLOW.md](docs/WORKFLOW.md).

## Build

```powershell
cmd.exe /s /c "set `"JAVA_HOME=J:\carla_javacpp_integration\tools\jdk-25\jdk-25.0.3+9`" && set `"PATH=J:\carla_javacpp_integration\tools\jdk-25\jdk-25.0.3+9\bin;%PATH%`" && set `"CARLA_INCLUDE_DIR=J:\carla_javacpp_integration\carla-sdk\include`" && set `"CARLA_LIB_DIR=J:\carla_javacpp_integration\carla-sdk\lib`" && `"C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat`" -arch=x64 -host_arch=x64 >nul && mvn -Pnative clean package -DskipTests"
```

Output:

```text
target\carla-javacpp-integration-0.1.0-SNAPSHOT.jar
target\carla-javacpp-integration-0.1.0-SNAPSHOT-windows-x86_64.jar
```

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
examples/java/CarlaJavaSmokeTest.java
examples/java/CarlaCameraViewer.java
examples/java/CarlaTutorialExample.java
examples/java/CarlaSensorSynchronizationExample.java
examples/java/CarlaLidarViewer.java
examples/java/CarlaMultiSensorViewer.java
examples/java/CarlaWeatherExample.java
examples/java/CarlaTrafficExample.java
```

`CarlaCameraViewer` is the manual driving example with an RGB camera attached behind the vehicle.

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
- `examples/java`
- `scripts/Install-LocalArtifacts.ps1`

More detail is in [docs/WORKFLOW.md](docs/WORKFLOW.md).
