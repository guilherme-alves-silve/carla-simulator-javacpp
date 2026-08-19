#!/bin/bash
set -euo pipefail

# Configurações
VERSION="0.2.0"
PLATFORM=""
GPG_KEY="guilherme_alves_silve@hotmail.com"

if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "win32" ]]; then
    # Windows
    PLATFORM="windows-x86_64"
elif [[ "$OSTYPE" == "linux-gnu" ]]; then
    # Linux
    PLATFORM="linux-x86_64"
else
    echo "Unsupported host OS."
    exit 1
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="$ROOT/target"
STAGING_ROOT="$TARGET/central-staging"
GROUP_PATH="io/github/guilherme-alves-silve/carla-simulator-javacpp/$VERSION"
STAGING_DIR="$STAGING_ROOT/$GROUP_PATH"

ARTIFACTS=(
    "carla-simulator-javacpp-$VERSION.jar"
    "carla-simulator-javacpp-$VERSION-linux-x86_64.jar"
    "carla-simulator-javacpp-$VERSION-windows-x86_64.jar"
    "carla-simulator-javacpp-$VERSION-sources.jar"
    "carla-simulator-javacpp-$VERSION-javadoc.jar"
)

# ---------------------------------------------------------------------------
#   1) gpg actual path
#   2) Gpg4win 64-bit  -> /c/Program Files/GnuPG/bin/gpg.exe
#   3) Gpg4win 32-bit  -> /c/Program Files (x86)/GnuPG/bin/gpg.exe
# ---------------------------------------------------------------------------
resolve_gpg() {
    local candidates=()

    if command -v gpg &> /dev/null; then
        candidates+=("gpg")
    fi

    if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "win32" ]]; then
        candidates+=("/c/Program Files/GnuPG/bin/gpg.exe")
        candidates+=("/c/Program Files (x86)/GnuPG/bin/gpg.exe")
    fi

    for candidate in "${candidates[@]}"; do
        if [[ "$candidate" != "gpg" ]] && [[ ! -x "$candidate" ]]; then
            continue
        fi

        echo "Testing gpg candidate: $candidate" >&2

        if "$candidate" --list-secret-keys "$GPG_KEY" &> /dev/null; then
            echo "  -> OK, secret key found using: $candidate" >&2
            GPG_BIN="$candidate"
            return 0
        else
            echo "  -> did not find the secret key (or errored) with this binary" >&2
        fi
    done

    return 1
}

GPG_BIN=""
if ! resolve_gpg; then
    echo "" >&2
    echo "Could not find a working gpg that sees the secret key for: $GPG_KEY" >&2
    echo "Tried the system gpg and the Gpg4win 64-bit/32-bit paths." >&2
    echo "Check 'gpg --list-secret-keys' manually, or update the paths in resolve_gpg()." >&2
    exit 1
fi

echo "Using gpg: $GPG_BIN"
echo ""

for artifact in "${ARTIFACTS[@]}"; do
    FILE="jars/$artifact"
    if [[ ! -f "$FILE" ]]; then
        echo "Missing artifact: $FILE"
        exit 1
    fi
done

if [[ -d "$STAGING_ROOT" ]]; then
    rm -rf "$STAGING_ROOT"
fi

mkdir -p "$STAGING_DIR"

for artifact in "${ARTIFACTS[@]}"; do
    cp "jars/$artifact" "$STAGING_DIR/"
done

cp "$ROOT/pom.xml" "$STAGING_DIR/carla-simulator-javacpp-$VERSION.pom"

NATIVE_JAR="$TARGET/carla-simulator-javacpp-$VERSION-$PLATFORM.jar"
if [[ -f "$NATIVE_JAR" ]]; then
    cp "$NATIVE_JAR" "$STAGING_DIR/"
fi

FILES_TO_PROCESS=("${ARTIFACTS[@]}")
if [[ -f "$NATIVE_JAR" ]]; then
    FILES_TO_PROCESS+=("carla-simulator-javacpp-$VERSION-$PLATFORM.jar")
fi
FILES_TO_PROCESS+=("carla-simulator-javacpp-$VERSION.pom")

for f in "${FILES_TO_PROCESS[@]}"; do
    FILE="$STAGING_DIR/$f"
    echo "Signing $f ..."

    "$GPG_BIN" --batch --yes --armor --detach-sign --default-key "$GPG_KEY" --output "$FILE.asc" "$FILE"

    if [[ ! -f "$FILE.asc" ]]; then
        echo "Signature file not created!" >&2
        exit 1
    fi

    if ! "$GPG_BIN" --verify "$FILE.asc" "$FILE" &> /dev/null; then
        echo "Signature verification failed for $f!" >&2
        exit 1
    fi

    echo "  Signature created and verified: $FILE.asc"

    # Gera os checksums (MD5/SHA1 exigidos; SHA256/SHA512 recomendados)
    MD5=$(md5sum "$FILE" | awk '{print $1}')
    SHA1=$(sha1sum "$FILE" | awk '{print $1}')
    SHA256=$(sha256sum "$FILE" | awk '{print $1}')
    SHA512=$(sha512sum "$FILE" | awk '{print $1}')

    echo "$MD5" > "$FILE.md5"
    echo "$SHA1" > "$FILE.sha1"
    echo "$SHA256" > "$FILE.sha256"
    echo "$SHA512" > "$FILE.sha512"

    echo "  Checksums generated"
done

# ---------------------------------------------------------------------------
#   1) zip          (actual PATH)
#   2) 7-Zip        (7z.exe, 64-bit e 32-bit)
#   3) WinRAR       (Rar.exe)
# ---------------------------------------------------------------------------
resolve_zip_tool() {
    if command -v zip &> /dev/null; then
        echo "Using zip: zip (found in PATH)" >&2
        ZIP_BIN="zip"
        ZIP_KIND="zip"
        return 0
    fi
    echo "zip not found in PATH, looking for alternatives..." >&2

    local sevenzip_candidates=(
        "/c/Program Files/7-Zip/7z.exe"
        "/c/Program Files (x86)/7-Zip/7z.exe"
    )
    for candidate in "${sevenzip_candidates[@]}"; do
        if [[ -x "$candidate" ]]; then
            echo "Using zip: $candidate (7-Zip)" >&2
            ZIP_BIN="$candidate"
            ZIP_KIND="7z"
            return 0
        fi
    done

    local winrar_candidates=(
        "/c/Program Files/WinRAR/Rar.exe"
        "/c/Program Files (x86)/WinRAR/Rar.exe"
    )
    for candidate in "${winrar_candidates[@]}"; do
        if [[ -x "$candidate" ]]; then
            echo "Using zip: $candidate (WinRAR, via -afzip)" >&2
            ZIP_BIN="$candidate"
            ZIP_KIND="winrar"
            return 0
        fi
    done

    return 1
}

ZIP_BIN=""
ZIP_KIND=""
if ! resolve_zip_tool; then
    echo "" >&2
    echo "Could not find zip, 7-Zip or WinRAR to create the bundle." >&2
    echo "Install one of them (e.g. 'pacman -S zip' or 7-Zip/WinRAR for Windows)." >&2
    exit 1
fi

ZIP_PATH="$TARGET/central-bundle-$VERSION.zip"
if [[ -f "$ZIP_PATH" ]]; then
    rm "$ZIP_PATH"
fi

# IMPORTANT: Central (https://central.sonatype.com/) requires the ZIP to contain the relative path
# starting at "io/github/...", not the absolute staging path.
# That's why we cd into STAGING_ROOT before zipping, in all cases.
(
    cd "$STAGING_ROOT"
    case "$ZIP_KIND" in
        zip)
            "$ZIP_BIN" -r "$ZIP_PATH" "io"
            ;;
        7z)
            "$ZIP_BIN" a -tzip "$ZIP_PATH" "io"
            ;;
        winrar)
            "$ZIP_BIN" a -afzip "$ZIP_PATH" "io"
            ;;
    esac
)

echo ""
echo "Bundle ready: $ZIP_PATH"
echo ""
echo "Bundle contents:"
if command -v unzip &> /dev/null; then
    unzip -l "$ZIP_PATH"
elif [[ "$ZIP_KIND" == "7z" ]]; then
    "$ZIP_BIN" l "$ZIP_PATH"
elif [[ "$ZIP_KIND" == "winrar" ]]; then
    "$ZIP_BIN" l "$ZIP_PATH"
else
    echo "(unzip not available to list contents; bundle was still created)"
fi
