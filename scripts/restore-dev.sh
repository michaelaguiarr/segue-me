#!/usr/bin/env bash
# Restaura o banco de desenvolvimento a partir do backup em backup/
# Apaga o banco existente e recria do zero.
#
# Uso: ./scripts/restore-dev.sh
# Pré-requisito: docker compose up -d (container segue-me-postgres-1 rodando)

set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

CONTAINER="segue-me-postgres-1"
DB_USER="segueme"
DB_PASSWORD="segueme123"
DB_NAME="segueme"

# ---------------------------------------------------------------------------
# 1. Localiza o arquivo de backup
# ---------------------------------------------------------------------------
backup_file=$(find backup/ -maxdepth 1 \
  \( -name "*.sql" -o -name "*.dump" -o -name "*.backup" -o -name "*.tar" \) \
  2>/dev/null | sort | head -1)

if [ -z "$backup_file" ]; then
  echo "ERRO: nenhum arquivo de backup encontrado em backup/"
  exit 1
fi

echo "==> Backup selecionado: $backup_file"

# ---------------------------------------------------------------------------
# 2. Valida que o container está rodando
# ---------------------------------------------------------------------------
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  echo "ERRO: container '$CONTAINER' não está rodando."
  echo "Suba o ambiente primeiro: docker compose up -d"
  exit 1
fi

# ---------------------------------------------------------------------------
# 3. Aguarda o postgres estar pronto
# ---------------------------------------------------------------------------
echo "==> Verificando disponibilidade do PostgreSQL..."
until docker exec "$CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" > /dev/null 2>&1; do
  sleep 2
done

# ---------------------------------------------------------------------------
# 4. Encerra conexões abertas no banco (Tomcat pode ter conexões ativas)
# ---------------------------------------------------------------------------
echo "==> Encerrando conexões abertas em '$DB_NAME'..."
docker exec "$CONTAINER" psql -U "$DB_USER" -d postgres -c "
  SELECT pg_terminate_backend(pid)
  FROM pg_stat_activity
  WHERE datname = '$DB_NAME'
    AND pid <> pg_backend_pid();
" > /dev/null

# ---------------------------------------------------------------------------
# 5. Apaga e recria o banco
# ---------------------------------------------------------------------------
echo "==> Apagando banco '$DB_NAME'..."
docker exec "$CONTAINER" psql -U "$DB_USER" -d postgres -c "DROP DATABASE IF EXISTS \"$DB_NAME\";"

echo "==> Criando banco '$DB_NAME'..."
docker exec "$CONTAINER" psql -U "$DB_USER" -d postgres -c "CREATE DATABASE \"$DB_NAME\" OWNER \"$DB_USER\";"

# ---------------------------------------------------------------------------
# 6. Cria os roles referenciados no dump (OWNER TO) que não existem no Docker
# ---------------------------------------------------------------------------
echo "==> Criando roles auxiliares do dump..."
docker exec "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'SQL'
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT FROM pg_catalog.pg_roles WHERE rolname = 'michaels_segueme'
  ) THEN
    CREATE ROLE "michaels_segueme";
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT FROM pg_catalog.pg_roles WHERE rolname = 'michaels_segue-me'
  ) THEN
    CREATE ROLE "michaels_segue-me";
  END IF;
END $$;
SQL

# ---------------------------------------------------------------------------
# 7. Restaura o backup
# ---------------------------------------------------------------------------
echo "==> Restaurando '$backup_file' (pode demorar alguns minutos)..."
docker exec -i "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" \
  < "$backup_file" 2>&1 \
  | grep -E '^(ERROR|FATAL|psql:)' \
  | grep -v 'role.*does not exist' \
  || true

# ---------------------------------------------------------------------------
# 8. Confirma o resultado
# ---------------------------------------------------------------------------
echo ""
echo "==> Tabelas restauradas:"
docker exec "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "\dt"

table_count=$(docker exec "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -tAc \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" \
  | tr -d '[:space:]')

echo ""
echo "Restore concluído — $table_count tabelas no banco '$DB_NAME'."
