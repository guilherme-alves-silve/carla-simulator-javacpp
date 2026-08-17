#!/usr/bin/env bash
# build-libcarla.sh
#
# Builds LibCarla (CARLA 0.9.16 C++ client SDK) from source on Ubuntu 22.04.
# Covers: UE4.26 fork clone/build, CARLA repo clone, dependency setup,
# LibCarla client build, and a smoke test linking against the result.
#
# Tested on: Ubuntu 22.04 (WSL2). NOT tested/recommended on newer Ubuntu
# releases — the bundled UE4 toolchain's linker (v17_clang-10.0.1-centos7,
# ~2019/2020 vintage) is incompatible with the RELR relocation format
# (.relr.dyn) used by glibc on Ubuntu 24.04+/26.04+.
#
# Prerequisites:
#   - GitHub account linked to the Epic Games organization
#     (https://www.epicgames.com/account/connections), required to clone
#     the private CarlaUnreal/UnrealEngine fork.
#   - System packages: build-essential g++ clang cmake ninja-build
#     libvulkan1 python3 python3-dev python3-pip python3-venv autoconf
#     wget curl rsync unzip git git-lfs libpng-dev libtiff5-dev
#     libjpeg-dev dos2unix
#
# Usage:
#   ./build-libcarla.sh [UE4_DIR] [CARLA_DIR]
# Defaults:
#   UE4_DIR   = ~/UnrealEngine_4.26
#   CARLA_DIR = ~/carla-simulator/carla-source-full-0.9.16

set -euo pipefail

UE4_DIR="${1:-$HOME/UnrealEngine_4.26}"
CARLA_DIR="${2:-$HOME/carla-simulator/carla-source-full-0.9.16}"

log() { echo -e "\n\033[1;32m==> $*\033[0m"; }

# ---------------------------------------------------------------------------
# 1. Unreal Engine 4.26 fork (provides the bundled clang toolchain CARLA
#    requires for ABI-compatible builds)
# ---------------------------------------------------------------------------
if [[ ! -d "$UE4_DIR" ]]; then
  log "Cloning UnrealEngine_4.26 fork into $UE4_DIR"
  git clone --depth 1 -b carla https://github.com/CarlaUnreal/UnrealEngine.git "$UE4_DIR"
else
  log "UE4_DIR already exists, skipping clone: $UE4_DIR"
fi

cd "$UE4_DIR"

# Locate the bundled toolchain's bin/ directory (not the clang++ binary
# itself). PATH needs the whole bin/ so cmake/make can also resolve clang,
# ld, ar etc. by name, not just via CC/CXX.
find_ue4_clang_bin() {
  find "$UE4_DIR/Engine/Extras/ThirdPartyNotUE/SDKs/HostLinux/Linux_x64" \
    -maxdepth 2 -type d -name "bin" 2>/dev/null | head -n1
}

UE4_CLANG_BIN="$(find_ue4_clang_bin)"

if [[ -z "$UE4_CLANG_BIN" ]]; then
  log "Building UnrealEngine_4.26 (Setup.sh, GenerateProjectFiles.sh, make) — this takes 1-2h"
  # No -j here on purpose: parallel make is known to break this build.
  ./Setup.sh && ./GenerateProjectFiles.sh && make
  # IMPORTANT: the toolchain directory name (e.g. v17_clang-10.0.1-centos7)
  # can change/appear only after Setup.sh runs — re-detect, don't assume
  # the pre-build path is still valid.
  UE4_CLANG_BIN="$(find_ue4_clang_bin)"
  if [[ -z "$UE4_CLANG_BIN" ]]; then
    echo "[error] Could not locate the UE4 clang toolchain bin/ dir after build." >&2
    exit 1
  fi
else
  log "UE4 toolchain already present, skipping engine build"
fi

export UE4_ROOT="$UE4_DIR"
export UE4_CLANG="$UE4_CLANG_BIN/clang++"
export PATH="$UE4_CLANG_BIN:$PATH"

if ! grep -q "export UE4_ROOT=" "$HOME/.bashrc" 2>/dev/null; then
  echo "export UE4_ROOT=$UE4_DIR" >> "$HOME/.bashrc"
fi

# ---------------------------------------------------------------------------
# 2. CARLA repository
# ---------------------------------------------------------------------------
CARLA_PARENT="$(dirname "$CARLA_DIR")"
mkdir -p "$CARLA_PARENT"

if [[ ! -d "$CARLA_DIR" ]]; then
  log "Cloning CARLA (ue4-dev) into $CARLA_DIR"
  git clone -b ue4-dev https://github.com/carla-simulator/carla "$CARLA_DIR"
else
  log "CARLA_DIR already exists, skipping clone: $CARLA_DIR"
fi

cd "$CARLA_DIR"

log "Normalizing line endings (guards against CRLF from Windows-side transfers)"
find . -type f \( -name "*.sh" -o -name "*.txt" -o -name "*.cfg" \) -exec dos2unix {} \; 2>/dev/null || true

log "Restoring executable bit on shell scripts (guards against tar transfers losing it)"
find . -name "*.sh" -exec chmod +x {} \;

export CARLA_UE4_ROOT="$CARLA_DIR"

log "Pulling CARLA content/assets (Update.sh)"
./Update.sh

log "Downloading and building dependencies (make setup) — boost, rpclib, recast, etc."
make setup

# The toolchain bin/ path can change again after `make setup` (it may pull
# a different/updated SDK version). Re-detect and refresh PATH before
# building LibCarla itself.
UE4_CLANG_BIN="$(find_ue4_clang_bin)"
if [[ -n "$UE4_CLANG_BIN" ]]; then
  export UE4_CLANG="$UE4_CLANG_BIN/clang++"
  export PATH="$UE4_CLANG_BIN:$PATH"
fi

log "Building LibCarla client"
make LibCarla

# ---------------------------------------------------------------------------
# 3. Smoke test — confirms the client SDK actually links
# ---------------------------------------------------------------------------
log "Running smoke test build"

BOOST_INSTALL_DIR="$(find "$CARLA_DIR/Build" -maxdepth 1 -iname 'boost-*-install' | head -n1)"
LIBCARLA_CLIENT_DIR="$(find "$CARLA_DIR/Build" -iname 'libcarla_client.a' -exec dirname {} \; | head -n1)"

if [[ -z "$BOOST_INSTALL_DIR" || -z "$LIBCARLA_CLIENT_DIR" ]]; then
  echo "[warn] Could not auto-locate boost-install or libcarla_client.a under $CARLA_DIR/Build."
  echo "       Skipping smoke test — locate them manually and compile with:"
  echo "       $UE4_CLANG smoke_test.cpp -std=c++14 -I\"\$HOME/carla/LibCarla/source\" \\"
  echo "         -I<boost-install>/include -L<libcarla_client dir> -l:libcarla_client.a -pthread -o smoke_test"
  exit 0
fi

if [[ ! -f smoke_test.cpp ]]; then
  cat > smoke_test.cpp <<'EOF'
// Minimal smoke test: confirms LibCarla client headers/lib link correctly.
#include <carla/client/Client.h>
#include <iostream>

int main() {
    carla::client::Client client("localhost", 2000);
    std::cout << "LibCarla client version: " << client.GetClientVersion() << std::endl;
    return 0;
}
EOF
fi

"$UE4_CLANG" smoke_test.cpp \
    -std=c++14 \
    -I"$CARLA_DIR/LibCarla/source" \
    -I"$BOOST_INSTALL_DIR/include" \
    -L"$LIBCARLA_CLIENT_DIR" \
    -l:libcarla_client.a \
    -pthread \
    -o smoke_test

log "Build finished. Binary: $CARLA_DIR/smoke_test"
echo "LibCarla headers: $CARLA_DIR/LibCarla/source"
echo "libcarla_client.a: $LIBCARLA_CLIENT_DIR"
