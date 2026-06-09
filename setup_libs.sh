#!/usr/bin/env bash
# setup_libs.sh
# Helper to install iFogSim2 into local Maven repo and copy JARs to libs/
# Usage: ./setup_libs.sh /path/to/iFogSim2

set -e

IFOGSIM2_DIR="${1:-../iFogSim2}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIBS_DIR="$SCRIPT_DIR/libs"

echo "=== PPEC iFogSim2 Library Setup ==="

if [ ! -d "$IFOGSIM2_DIR" ]; then
    echo "ERROR: iFogSim2 directory not found: $IFOGSIM2_DIR"
    echo "Usage: ./setup_libs.sh /path/to/iFogSim2"
    echo ""
    echo "Clone iFogSim2 first:"
    echo "  git clone https://github.com/Cloudslab/iFogSim2.git"
    exit 1
fi

echo "[1] Building iFogSim2..."
cd "$IFOGSIM2_DIR"
mvn clean package -DskipTests -q

IFOGSIM2_JAR="$(find target -name '*.jar' ! -name '*sources*' ! -name '*javadoc*' | head -1)"
if [ -z "$IFOGSIM2_JAR" ]; then
    echo "ERROR: Could not find iFogSim2 JAR in $IFOGSIM2_DIR/target"
    exit 1
fi

echo "[2] Installing iFogSim2 to local Maven repository..."
mvn install:install-file \
    -Dfile="$IFOGSIM2_JAR" \
    -DgroupId=org.fog-computing \
    -DartifactId=ifogsim2 \
    -Dversion=2.0.0 \
    -Dpackaging=jar \
    -q

echo "[3] Creating libs/ directory..."
mkdir -p "$LIBS_DIR"

echo "[4] Copying ifogsim2.jar to libs/..."
cp "$IFOGSIM2_JAR" "$LIBS_DIR/ifogsim2.jar"

echo "[5] Locating CloudSim JAR..."
CLOUDSIM_JAR="$(find ~/.m2/repository/org/cloudbus -name 'cloudsim-*.jar' \
    ! -name '*sources*' ! -name '*javadoc*' 2>/dev/null | head -1)"

if [ -n "$CLOUDSIM_JAR" ]; then
    cp "$CLOUDSIM_JAR" "$LIBS_DIR/cloudsim-3.0.3.jar"
    echo "[5] Copied: $CLOUDSIM_JAR → libs/cloudsim-3.0.3.jar"
else
    echo "[5] WARNING: CloudSim JAR not found in ~/.m2 — may be bundled inside ifogsim2.jar"
    echo "    If build fails, manually copy cloudsim-3.0.3.jar to libs/"
fi

echo ""
echo "=== Setup complete ==="
echo "Now build and run PPEC:"
echo "  cd $SCRIPT_DIR"
echo "  mvn clean package -DskipTests"
echo "  java -jar output/PPEC-iFogSim2-full-jar-with-dependencies.jar"
