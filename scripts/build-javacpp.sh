#!/usr/bin/env bash
set -euo pipefail

CARLA="$HOME/carla-source-full-0.9.16"
DEST="$HOME/carla-sdk"

echo "Creating folders..."

mkdir -p "$DEST/include"
mkdir -p "$DEST/lib"
mkdir -p "$DEST/bin"

echo "Copying headers..."

cp -r "$CARLA/LibCarla/source/." "$DEST/include/"

cp -r "$CARLA/Unreal/CarlaUE4/Plugins/Carla/CarlaDependencies/include/." "$DEST/include/"

echo "Copying libraries..."

# On Linux the equivalent static/shared libs typically live under a
# similarly-structured "lib" dir, but check your build tree — CARLA's
# Linux build outputs are often under Build/ or PythonAPI/carla/dependencies/lib
find "$CARLA/PythonAPI/carla/dependencies/lib" -maxdepth 1 \( -name "*.a" -o -name "*.so*" \) -exec cp -f {} "$DEST/lib/" \;

find "$CARLA/Unreal/CarlaUE4/Plugins/Carla/CarlaDependencies/lib" -maxdepth 1 \( -name "*.a" -o -name "*.so*" \) -exec cp -f {} "$DEST/lib/" \;

echo "Searching shared objects (.so)..."

find "$CARLA" -type f -name "*.so*" -exec cp -f {} "$DEST/bin/" \;

echo ""
echo "Finished."
echo ""
echo "Include: $DEST/include"
echo "Lib:     $DEST/lib"
echo "Bin:     $DEST/bin"

cd $HOME/carla-simulator-javacpp/ 

bash -c 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ && export PATH="$JAVA_HOME/bin:$PATH" && export CARLA_INCLUDE_DIR=$HOME/carla-sdk/include && export CARLA_LIB_DIR=$HOME/carla-sdk/lib && mvn -Pnative clean package -DskipTests'
