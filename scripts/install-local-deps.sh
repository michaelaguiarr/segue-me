#!/usr/bin/env bash
# Instala dependências locais no repositório Maven.
# Execute antes do primeiro build: ./scripts/install-local-deps.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIBS_DIR="$SCRIPT_DIR/../libs"

echo "==> Instalando dependências locais..."

mvn install:install-file \
  -Dfile="$LIBS_DIR/paradise-1.0.1.jar" \
  -DgroupId=org.primefaces.themes \
  -DartifactId=paradise \
  -Dversion=1.0.1 \
  -Dpackaging=jar \
  -q

mvn install:install-file \
  -Dfile="$LIBS_DIR/simple-email-0.2.5-SNAPSHOT.jar" \
  -DgroupId=com.outjected \
  -DartifactId=simple-email \
  -Dversion=0.2.5-SNAPSHOT \
  -Dpackaging=jar \
  -q

mvn install:install-file \
  -Dfile="$LIBS_DIR/ComicSans-1.0.0.jar" \
  -DgroupId=net.sf.jasperreports \
  -DartifactId=ComicSans \
  -Dversion=1.0.0 \
  -Dpackaging=jar \
  -q

echo "==> Dependências instaladas com sucesso!"
