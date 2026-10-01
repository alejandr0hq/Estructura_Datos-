#!/usr/bin/env bash
# AIRCTRL - compila y abre la interfaz grafica (macOS / Linux).
# Uso: ./run.sh            -> ventana de conexion a PostgreSQL
#      ./run.sh --csv      -> solo CSV, sin base de datos
#      ./run.sh --auto     -> conecta directo con config/database.properties
set -euo pipefail
cd "$(dirname "$0")"

DRIVER_VERSION="42.7.4"
DRIVER="lib/postgresql-${DRIVER_VERSION}.jar"
DRIVER_URL="https://repo1.maven.org/maven2/org/postgresql/postgresql/${DRIVER_VERSION}/postgresql-${DRIVER_VERSION}.jar"

mkdir -p lib bin

if ! ls lib/postgresql-*.jar >/dev/null 2>&1; then
  echo "Descargando el driver JDBC de PostgreSQL (una sola vez)..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$DRIVER" "$DRIVER_URL" || rm -f "$DRIVER"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$DRIVER" "$DRIVER_URL" || rm -f "$DRIVER"
  fi
  if [ ! -f "$DRIVER" ]; then
    echo "No se pudo descargar el driver. Descargalo manualmente de:"
    echo "  $DRIVER_URL"
    echo "y guardalo en la carpeta lib/. (Mientras tanto puedes usar ./run.sh --csv)"
  fi
fi

echo "Compilando..."
rm -rf bin && mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java")

echo "Abriendo AIRCTRL..."
exec java -cp "bin:lib/*" airctrl.gui.AirCtrlApp "$@"
