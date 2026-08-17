# Building LibCarla 0.9.16 on Linux

C++ client SDK build for CARLA 0.9.16, targeting the `carla-simulator-javacpp`
bridge (`CarlaBridge.h`/`.cpp`). This does **not** build PythonAPI or launch
the simulator server — only the client library and headers needed for the
JavaCPP-based Java bindings.

## OS requirement

**Ubuntu 22.04.** Do not use a newer release (24.04/26.04 confirmed broken).

The bundled UE4 toolchain (`v17_clang-10.0.1-centos7`, ~2019/2020) ships its
own linker, which does not understand the RELR compressed relocation format
(`.relr.dyn`) used by glibc on newer Ubuntu releases. This surfaces as:

```
ld: /usr/lib/x86_64-linux-gnu/libc.so.6: unknown type [0x13] section `.relr.dyn'
ld: skipping incompatible /usr/lib/x86_64-linux-gnu/libc.so.6
```

There is no in-place fix — the toolchain is frozen for UE4.26 ABI
compatibility and can't be updated. Use a dedicated Ubuntu 22.04 WSL
instance:

```powershell
wsl --install -d Ubuntu-22.04
```

## Prerequisites

1. **GitHub account linked to the Epic Games organization** — the
   `CarlaUnreal/UnrealEngine` fork is private. Link at
   [epicgames.com/account/connections](https://www.epicgames.com/account/connections),
   accept the org invite emailed to your GitHub account's address, then
   confirm access by opening https://github.com/EpicGames/UnrealEngine in a
   browser (should not 404).

2. **GitHub Personal Access Token** — HTTPS password auth is deprecated;
   the UE4 clone needs a PAT with `repo` scope
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

## Steps

The full sequence is automated in [`build-libcarla.sh`](./build-libcarla.sh).
Manual equivalent, for reference:

```bash
# 1. Unreal Engine 4.26 fork — provides the ABI-compatible clang toolchain
git clone --depth 1 -b carla https://github.com/CarlaUnreal/UnrealEngine.git ~/UnrealEngine_4.26
cd ~/UnrealEngine_4.26
./Setup.sh && ./GenerateProjectFiles.sh && make   # no -j: known to break the build
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

## Common failure points (already handled by the script)

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

## Output locations

- Headers: `$CARLA_UE4_ROOT/LibCarla/source/`
- Static lib: `libcarla_client.a` under `$CARLA_UE4_ROOT/Build/libcarla-client-build.*/LibCarla/cmake/client/` (exact path varies by build config — the script locates it via `find`)
- Boost headers used by LibCarla's public API: `$CARLA_UE4_ROOT/Build/boost-*-install/include/`

These three paths are the inputs for assembling `carla-sdk/` for the
`carla-simulator-javacpp` project (see `assemble-carla-sdk-linux.sh` and
`docs/WORKFLOW.md` in that repo).
