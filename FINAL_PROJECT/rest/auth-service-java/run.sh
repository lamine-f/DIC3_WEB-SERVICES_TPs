#!/usr/bin/env bash
# Lance AuthService REST (Jersey + Grizzly) en local.
set -e
cd "$(dirname "$0")"

LIB_DIR="lib"
BUILD_DIR="build/classes"

if [ ! -d "$LIB_DIR" ] || [ -z "$(ls -A "$LIB_DIR" 2>/dev/null)" ]; then
    echo "[setup] Téléchargement des dépendances avec Maven..."
    mvn -B -q -f pom-deps.xml dependency:copy-dependencies -DoutputDirectory="$LIB_DIR" -DincludeScope=runtime
fi

echo "[build] Compilation..."
mkdir -p "$BUILD_DIR"
SRCS=$(find src -name '*.java')
javac --release 17 -cp "$LIB_DIR/*" -d "$BUILD_DIR" $SRCS

echo "[run] Lancement AuthService REST sur :${SERVICE_PORT:-8081}"
exec java -cp "$BUILD_DIR:$LIB_DIR/*" esp.dgi.ws.rest.auth.server.Main
