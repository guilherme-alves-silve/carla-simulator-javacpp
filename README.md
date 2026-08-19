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

## Build the C++ Client SDK on Linux

C++ client SDK build for CARLA 0.9.16, targeting this bridge
(`CarlaBridge.h`/`.cpp`). This section only applies on Linux;
on Windows the SDK is pre-assembled (see the tutorial linked in
the previous section). The build below produces only the client
library and headers needed by the JavaCPP-based Java bindings —
not the PythonAPI and not the simulator server.

### OS requirement

**Ubuntu 22.04.** Do not use a newer release (24.04/26.04
confirmed broken).

The bundled UE4 toolchain
(`v17_clang-10.0.1-centos7`, ~2019/2020) ships its own linker,
which does not understand the RELR compressed relocation format
(`.relr.dyn`) used by glibc on newer Ubuntu releases. This
surfaces as:

```
ld: /usr/lib/x86_64-linux-gnu/libc.so.6: unknown type [0x13] section `.relr.dyn'
ld: skipping incompatible /usr/lib/x86_64-linux-gnu/libc.so.6
```

There is no in-place fix — the toolchain is frozen for UE4.26
ABI compatibility and can't be updated. Use a dedicated Ubuntu
22.04 WSL instance:

```powershell
wsl --install -d Ubuntu-22.04
```

### Prerequisites

1. **GitHub account linked to the Epic Games organization** —
   the `CarlaUnreal/UnrealEngine` fork is private. Link at
   [epicgames.com/account/connections](https://epicgames.com/account/connections),
   accept the org invite emailed to your GitHub account's
   address, then confirm access by opening
   <https://github.com/EpicGames/UnrealEngine> in a browser
   (should not 404).

2. **GitHub Personal Access Token** — HTTPS password auth is
   deprecated; the UE4 clone needs a PAT with `repo` scope
   ([github.com/settings/tokens](https://github.com/settings/tokens)).

3. **System packages:**
   ```bash
   sudo apt-get update
   sudo apt-get install build-essential g++ clang cmake ninja-build \
     libvulkan1 python3 python3-dev python3-pip python3-venv autoconf \
     wget curl rsync unzip git git-lfs libpng-dev libtiff5-dev \
     libjpeg-dev dos2unix
   ```

4. **~130 GB free disk** (91 GB UE4 + ~31 GB CARLA content).

### Steps

The full sequence is automated in
[`scripts/build-libcarla.sh`](scripts/build-libcarla.sh). Manual
equivalent, for reference:

```bash
# 1. Unreal Engine 4.26 fork — provides the ABI-compatible clang toolchain
git clone --depth 1 -b carla https://github.com/CarlaUnreal/UnrealEngine.git ~/UnrealEngine_4.26
cd ~/UnrealEngine_4.26
./Setup.sh && ./GenerateProjectFiles.sh   # no -j: known to break the build
export UE4_ROOT=~/UnrealEngine_4.26

# 2. CARLA repository
git clone -b ue4-dev https://github.com/carla-simulator/carla
export CARLA_UE4_ROOT=/path/to/carla
cd $CARLA_UE4_ROOT
./Update.sh                                        # pulls content/assets

# 3. Dependencies + LibCarla client
UE4_CLANG_BIN=$UE4_ROOT/Engine/Extras/ThirdPartyNotUE/SDKs/HostLinux/Linux_x64/v17_clang-10.0.1-centos7/x86_64-unknown-linux-gnu/bin
export UE4_CLANG=$UE4_CLANG_BIN/clang++
export PATH="$UE4_CLANG_BIN:$PATH"
make setup

# IMPORTANT: re-check the toolchain path after `make setup` — the SDK
# version directory can change/only appear at this point. Re-export
# UE4_CLANG and PATH with the actual path before continuing if it did.
make LibCarla

# 4. Smoke test
clang++ smoke_test.cpp \
    -std=c++14 \
    -I"$HOME/carla/LibCarla/source" \
    -I"$HOME/carla/Build/boost-1.90.0-c10-install/include" \
    -L"$HOME/carla/Build/libcarla-client-build.release/LibCarla/cmake/client" \
    -l:libcarla_client.a \
    -pthread \
    -o smoke_test
```

### Common failure points (already handled by the script)

| Symptom | Cause | Fix |
|---|---|---|
| `bad interpreter: No such file or directory` / stray `$'\r'` in scripts | CRLF line endings from a Windows-side transfer | `find . -name "*.sh" -exec dos2unix {} \;` |
| `Permission denied` running a `.sh` | Executable bit lost during `tar`/transfer | `find . -name "*.sh" -exec chmod +x {} \;` |
| `clang++: not found` | clang not installed | `sudo apt-get install clang` |
| `toolset clang-linux initialization: version '10.0' requested but 'clang++-10.0' not found` | `Util/BuildTools/Setup.sh` hardcodes `BOOST_TOOLSET="clang-10.0"` | Only relevant if bypassing `UE4_CLANG`; the script above avoids this by always pointing at the bundled toolchain. |
| `pyconfig.h file not found` | Boost's python component needs Python dev headers | `sudo apt-get install python3-dev` |
| `CMAKE_MAKE_PROGRAM is not set` (Ninja) | `ninja-build` not installed | `sudo apt-get install ninja-build` |
| `CMAKE_C_COMPILER not set` / path starts with `/Engine/...` | `$UE4_ROOT` unset or wrong in current shell | `export UE4_ROOT=~/UnrealEngine_4.26`; persist in `~/.bashrc` with the **absolute** path, not `~` |
| Toolchain not found even with `UE4_ROOT` correct | `PATH` only has `CC`/`CXX` pointed at the toolchain, but `cmake`/other tools resolve `clang`/`ld`/`ar` via `PATH` directly | Add the toolchain's `bin/` dir to `PATH`, not just `CC`/`CXX`: `export PATH="$UE4_ROOT/Engine/Extras/.../bin:$PATH"`. Re-check this **again after `make setup`** — the SDK version directory can change or only get created at that point. |

### Output locations

- Headers: `$CARLA_UE4_ROOT/LibCarla/source/`
- Static lib: `libcarla_client.a` under
  `$CARLA_UE4_ROOT/Build/libcarla-client-build.*/LibCarla/cmake/client/`
  (exact path varies by build config — the script locates it via
  `find`)
- Boost headers used by LibCarla's public API:
  `$CARLA_UE4_ROOT/Build/boost-*-install/include/`

These three paths are the inputs for assembling `carla-sdk/` for
this project (see `assemble-carla-sdk-linux.sh` and
[docs/WORKFLOW.md](docs/WORKFLOW.md) in this repo).

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

## Manual Build (must run in the root of the project)

Windows:
```powershell
cmd.exe /s /c "set `"JAVA_HOME=J:\tools\jdk-17`" && set `"PATH=J:\tools\jdk-17\bin;%PATH%`" && set `"CARLA_INCLUDE_DIR=J:\carla-sdk\include`" && set `"CARLA_LIB_DIR=J:\carla-sdk\lib`" && `"C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat`" -arch=x64 -host_arch=x64 >nul && mvn -Pnative clean package -DskipTests && copy target\carla-simulator-javacpp-*.jar jars\"
```

Linux:
```bash
bash -c 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ && export PATH="$JAVA_HOME/bin:$PATH" && export CARLA_INCLUDE_DIR=$HOME/carla-sdk/include && export CARLA_LIB_DIR=$HOME/carla-sdk/lib && mvn -Pnative clean package -DskipTests && cp target/carla-simulator-javacpp-*.jar jars/'
```

## Compilation process and building

Linux (Ubuntu 22.04, inside WSL or a native install — see
[Build the C++ Client SDK on Linux](#build-the-c-client-sdk-on-linux)
above for the LibCarla build that produces `carla-sdk/`):
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
target/carla-simulator-javacpp-VERSION.jar
target/carla-simulator-javacpp-VERSION-{windows,linux}-x86_64.jar
target/carla-simulator-javacpp-VERSION-sources.jar
target/carla-simulator-javacpp-VERSION-javadoc.jar
```

The last two jars are produced automatically by the build
(`maven-source-plugin` and `maven-javadoc-plugin`, both bound to
the `package` phase). The sources jar contains only the
`org.carla.javacpp.api` sources (the JavaCPP-generated binding
and the bundled examples are excluded), and the javadoc jar
contains the browsable HTML documentation for the same surface.

## Install In Local Maven

```powershell
.\scripts\install-local-artifacts.ps1
```

Run this after every API or native bridge change before testing from another Maven project.

## Run Examples

```powershell
.\scripts\start-carla.ps1 -QualityLevel Low
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

The test suite is split in two layers along the same line as the
build itself: pure Java unit tests run by default, and anything
that touches the native bridge or a running CARLA simulator
requires explicit profiles.

| Command | What runs | Prerequisites |
|---|---|---|
| `mvn test` | Pure Java unit tests (12 tests) — value object field access, builder semantics, validation, exception types, and the parts of the API that do not call into `CarlaNative` | None |
| `mvn -Pnative -Pintegration-tests verify` | All unit tests + native-bridge round-trip tests (`ValueTypesIT`, `WorldSettingsIT`, `WeatherParametersIT`) + CARLA integration tests (`CarlaConnectionIT`, `CarlaSpawnSensorIT`) | `carla-sdk/` on `CARLA_INCLUDE_DIR` / `CARLA_LIB_DIR`; CARLA simulator running on `localhost:2000` |
| `mvn -Pnative test` | All unit tests + native-bridge round-trip tests (no CARLA tests) | `carla-sdk/` on `CARLA_INCLUDE_DIR` / `CARLA_LIB_DIR` |
| `mvn -Pintegration-tests verify` | All unit tests + CARLA integration tests (no native marshalling tests, so the `.dll` is never built) | CARLA simulator running on `localhost:2000` |

The native-bridge round-trip tests live under
`src/integration-test/java/` because they pull in the
`jniCarlaNative` shared library through `Loader.load()` at class
init time. If the library is missing, the `CarlaNative` static
initializer fails with `UnsatisfiedLinkError` and the whole test
class breaks; there is no graceful per-test skip path. Keeping
those tests behind `-Pnative` is what makes the default
`mvn test` green on machines that do not have the native build
ready.

The `WorldSettingsTest` and `WeatherParametersTest` files in
`src/test/java/` are now empty placeholder classes pointing at
their `*IT` counterparts in `src/integration-test/java/`. They
are kept so package-private references from other test files
keep compiling without churn; new native-touching tests should
be added as `*IT` classes.

## Project Files

- `src/main/cpp/CarlaBridge.h`
- `src/main/cpp/CarlaBridge.cpp`
- `src/main/java/org/carla/javacpp/binding/CarlaNative.java`
- `src/main/java/org/carla/javacpp/api`
- `src/examples/java`
- `src/examples/python/original`

More detail is in [docs/WORKFLOW.md](docs/WORKFLOW.md).

## License and Third-Party Components

The Java source, the C++ bridge, and the Maven build configuration
in this repository are released under the **Apache License 2.0**
— see the [`LICENSE`](LICENSE) file at the project root and the
`<license>` block in [`pom.xml`](pom.xml).

The compiled native bridge (`libjniCarlaNative.so` /
`jniCarlaNative.dll`) and the Maven artifact itself depend on
several third-party components, each with its own license. The
high-level attributions are summarised in [`NOTICE`](NOTICE), and
the full license texts are reproduced in
[`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md). At a glance:

- **CARLA Simulator** (C++ client SDK + Python examples) — MIT
- **JavaCPP** (Maven plugin and runtime) — Apache 2.0
- **Boost C++ Libraries** (Linux: `boost_filesystem`) — BSL 1.0
- **Recast & Detour** (navigation mesh + pathfinding) — Zlib
- **Windows Shell Lightweight Utility API** (Windows: `Shlwapi`)
  — Microsoft proprietary; no redistribution required when
  dynamically linking
- **GNU C Library** (Linux: `pthread`, `dl`) — LGPL 2.1+;
  dynamically linked from the system library, no source bundled

The `LICENSE`, `NOTICE`, and `THIRD_PARTY_NOTICES.md` files are
also packaged inside the published JAR at `META-INF/` (see
the `<resources>` block in `pom.xml`), so consumers always have
the attribution clauses available next to the compiled bytecode.

If you find a missing or inaccurate attribution, please open a
pull request before the next release.

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
target/carla-simulator-javacpp-VERSION-javadoc.jar
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
