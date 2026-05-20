#!/usr/bin/env bash
# Lance AuthService SOAP en local (sans Docker, sans Ant).
# Pré-requis : Java 17+, Maven (pour résoudre les dépendances la première fois).

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
find src -name '*.java' > /tmp/auth-soap-sources.txt
javac --release 17 -cp "$LIB_DIR/*" -d "$BUILD_DIR" @/tmp/auth-soap-sources.txt
rm /tmp/auth-soap-sources.txt

echo "[run] Lancement AuthService SOAP sur :${SERVICE_PORT:-9001}"
exec java -cp "$BUILD_DIR:$LIB_DIR/*" esp.dgi.ws.soap.auth.server.ServerApp
