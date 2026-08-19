#!/bin/bash

# Configurações
VERSION="0.2.0"
PLATFORM=""
GPG_KEY="guilherme_alves_silve@hotmail.com"

# Detecta o sistema operacional
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

# Define o caminho raiz e target
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="$ROOT/target"
STAGING_ROOT="$TARGET/central-staging"
GROUP_PATH="io/github/guilherme-alves-silve/carla-simulator-javacpp/$VERSION"
STAGING_DIR="$STAGING_ROOT/$GROUP_PATH"

# Lista de artefatos
ARTIFACTS=(
    "carla-simulator-javacpp-$VERSION.jar"
    "carla-simulator-javacpp-$VERSION-sources.jar"
    "carla-simulator-javacpp-$VERSION-javadoc.jar"
)

# Verifica se os artefatos existem
for artifact in "${ARTIFACTS[@]}"; do
    FILE="$TARGET/$artifact"
    if [[ ! -f "$FILE" ]]; then
        echo "Missing artifact: $FILE"
        exit 1
    fi
done

# Remove o diretório de staging se existir
if [[ -d "$STAGING_ROOT" ]]; then
    rm -rf "$STAGING_ROOT"
fi

# Cria o diretório de staging
mkdir -p "$STAGING_DIR"

# Copia os artefatos para o diretório de staging
for artifact in "${ARTIFACTS[@]}"; do
    cp "$TARGET/$artifact" "$STAGING_DIR/"
done

# Copia o pom.xml
cp "$ROOT/pom.xml" "$STAGING_DIR/carla-simulator-javacpp-$VERSION.pom"

# Verifica se há um arquivo nativo
NATIVE_JAR="$TARGET/carla-simulator-javacpp-$VERSION-$PLATFORM.jar"
if [[ -f "$NATIVE_JAR" ]]; then
    cp "$NATIVE_JAR" "$STAGING_DIR/"
fi

# Lista de arquivos a serem processados
FILES_TO_PROCESS=("${ARTIFACTS[@]}")
if [[ -f "$NATIVE_JAR" ]]; then
    FILES_TO_PROCESS+=("carla-simulator-javacpp-$VERSION-$PLATFORM.jar")
fi
FILES_TO_PROCESS+=("carla-simulator-javacpp-$VERSION.pom")

# Assina os arquivos com GPG
for f in "${FILES_TO_PROCESS[@]}"; do
    FILE="$STAGING_DIR/$f"
    echo "Signing $f ..."

    # Verifica se gpg está instalado
    if ! command -v gpg &> /dev/null; then
        echo "gpg not found!" >&2
        exit 1
    fi

    # Assina o arquivo
    gpg --armor --detach-sign --default-key "$GPG_KEY" --output "$FILE.asc" "$FILE" 2>&1

    # Verifica se a assinatura foi criada
    if [[ ! -f "$FILE.asc" ]]; then
        echo "Signature file not created!" >&2
        exit 1
    fi

    echo "  Signature created: $FILE.asc"

    # Gera os checksums
    MD5=$(md5sum "$FILE" | awk '{print $1}')
    SHA1=$(sha1sum "$FILE" | awk '{print $1}')

    # Cria os arquivos de checksum
    echo "$MD5" > "$FILE.md5"
    echo "$SHA1" > "$FILE.sha1"

    echo "  Checksums generated"
done

# Cria o ZIP bundle
ZIP_PATH="$TARGET/central-bundle-$VERSION.zip"
if [[ -f "$ZIP_PATH" ]]; then
    rm "$ZIP_PATH"
fi

# Cria o ZIP
zip -r "$ZIP_PATH" "$STAGING_DIR"

# Verifica o conteúdo do ZIP
echo ""
echo "Bundle ready: $ZIP_PATH"
echo ""
echo "Bundle contents:"
unzip -l "$ZIP_PATH"
