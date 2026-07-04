#!/usr/bin/env bash
# Release — Segue-Me
# Sobe a versão no pom.xml, cria o commit e a tag anotada num passo só.
# A versão é a fonte única (pom.xml) e alimenta o rodapé do login via filtering.
#
# Uso:  ./scripts/release.sh <X.Y.Z[-qualificador]>
# Ex.:  ./scripts/release.sh 3.0.2
#       ./scripts/release.sh 3.1.0-beta
#
# SemVer: PATCH (3.0.x) = correções | MINOR (3.x.0) = features compatíveis |
#         MAJOR (x.0.0) = mudanças que quebram compatibilidade.

set -euo pipefail

# ---------------------------------------------------------------------------
# 1. Valida o argumento (formato SemVer)
# ---------------------------------------------------------------------------
if [ $# -ne 1 ]; then
  echo "Uso: ./scripts/release.sh <X.Y.Z[-qualificador]>"
  echo "Ex.: ./scripts/release.sh 3.0.2"
  exit 1
fi

VERSION="$1"

if ! echo "$VERSION" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?$'; then
  echo "ERRO: versão inválida '$VERSION'."
  echo "      Use o formato SemVer X.Y.Z (ex.: 3.0.2) ou X.Y.Z-qualificador (ex.: 3.1.0-beta)."
  exit 1
fi

TAG="v$VERSION"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# ---------------------------------------------------------------------------
# 2. Árvore de trabalho precisa estar limpa (não misturar o bump com outras mudanças)
# ---------------------------------------------------------------------------
if [ -n "$(git status --porcelain)" ]; then
  echo "ERRO: há mudanças não commitadas. Faça commit/stash antes de gerar o release."
  exit 1
fi

# ---------------------------------------------------------------------------
# 3. Impede tag duplicada
# ---------------------------------------------------------------------------
if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null; then
  echo "ERRO: a tag '$TAG' já existe."
  exit 1
fi

CURRENT=$(grep -m1 -oE '<version>[^<]+</version>' pom.xml | sed -E 's:</?version>::g')
echo "==> Branch atual: $(git branch --show-current)"
echo "==> Versão: $CURRENT  ->  $VERSION"

# ---------------------------------------------------------------------------
# 4. Atualiza SÓ a versão do projeto no pom.xml (offline, sem plugin).
#    Ancorado no artifactId do projeto para não tocar versões de dependências.
# ---------------------------------------------------------------------------
perl -0777 -i.bak -pe \
  's{(<artifactId>segue-me</artifactId>\s*<version>)[^<]+(</version>)}{${1}'"$VERSION"'${2}}' \
  pom.xml

if ! grep -q "<version>$VERSION</version>" pom.xml; then
  mv -f pom.xml.bak pom.xml          # rollback
  echo "ERRO: não foi possível atualizar a versão do projeto no pom.xml."
  exit 1
fi
rm -f pom.xml.bak

# ---------------------------------------------------------------------------
# 5. Commit + tag anotada
# ---------------------------------------------------------------------------
git add pom.xml
git commit -m "chore(release): $VERSION"
git tag -a "$TAG" -m "Release $VERSION"

echo ""
echo "Release $VERSION preparado: commit + tag '$TAG' criados localmente."
echo ""
echo "Próximos passos (o push na main dispara o deploy de produção via CI/CD):"
echo "  git push origin $(git branch --show-current)"
echo "  git push origin $TAG"
