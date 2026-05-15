#!/usr/bin/env bash
# Deploy em produção — segue-me
# Uso: ./scripts/deploy.sh

set -e

# Diretório raiz do projeto (pai desta pasta scripts/)
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_FILE="docker-compose.prod.yml"

# ---------------------------------------------------------------------------
# 1. Valida que o arquivo .env existe
# ---------------------------------------------------------------------------
if [ ! -f ".env" ]; then
  echo ""
  echo "ERRO: arquivo .env não encontrado."
  echo ""
  echo "Crie o arquivo a partir do exemplo e preencha as credenciais:"
  echo "  cp .env.prod.example .env"
  echo "  nano .env"
  echo ""
  exit 1
fi

# ---------------------------------------------------------------------------
# 2. Valida que as variáveis obrigatórias estão definidas no .env
# ---------------------------------------------------------------------------
set -a
source .env
set +a

for var in DB_USER DB_PASSWORD DB_NAME; do
  if [ -z "${!var}" ]; then
    echo "ERRO: variável '$var' está vazia no arquivo .env. Preencha antes de continuar."
    exit 1
  fi
done

# ---------------------------------------------------------------------------
# 3. Instala dependências locais se ausentes do ~/.m2
# ---------------------------------------------------------------------------
needs_install=false
for dep_path in \
  "$HOME/.m2/repository/org/primefaces/themes/paradise/1.0.1/paradise-1.0.1.jar" \
  "$HOME/.m2/repository/com/outjected/simple-email/0.2.5-SNAPSHOT/simple-email-0.2.5-SNAPSHOT.jar" \
  "$HOME/.m2/repository/net/sf/jasperreports/ComicSans/1.0.0/ComicSans-1.0.0.jar"; do
  if [ ! -f "$dep_path" ]; then
    needs_install=true
    break
  fi
done

if [ "$needs_install" = true ]; then
  echo "==> Dependências locais não encontradas — instalando..."
  "$ROOT_DIR/scripts/install-local-deps.sh"
fi

# ---------------------------------------------------------------------------
# 4. Gera o WAR com Maven
# ---------------------------------------------------------------------------
echo "==> Compilando o projeto..."
mvn clean package -DskipTests

# ---------------------------------------------------------------------------
# 5. Copia o WAR para a pasta monitorada pelo Tomcat
# ---------------------------------------------------------------------------
echo "==> Publicando o WAR em deployments/..."
mkdir -p deployments
cp target/segue-me.war deployments/

# ---------------------------------------------------------------------------
# 6. Inicia apenas o PostgreSQL para verificação e eventual restore
# ---------------------------------------------------------------------------
echo "==> Iniciando PostgreSQL..."
docker compose -f "$COMPOSE_FILE" --env-file .env up -d postgres

echo "==> Aguardando PostgreSQL ficar disponível..."
until docker compose -f "$COMPOSE_FILE" --env-file .env exec -T postgres \
    pg_isready -U "$DB_USER" -d "$DB_NAME" > /dev/null 2>&1; do
  sleep 2
done
echo "    PostgreSQL pronto."

# ---------------------------------------------------------------------------
# 7. Restaura backup se existir arquivo em backup/ e banco estiver vazio
# ---------------------------------------------------------------------------
backup_file=$(find backup/ -maxdepth 1 \
  \( -name "*.sql" -o -name "*.dump" -o -name "*.backup" -o -name "*.tar" \) \
  2>/dev/null | sort | head -1)

if [ -n "$backup_file" ]; then
  echo "==> Backup encontrado: $backup_file"

  # Verifica quantas tabelas já existem no schema public
  table_count=$(docker compose -f "$COMPOSE_FILE" --env-file .env exec -T postgres \
    psql -U "$DB_USER" -d "$DB_NAME" -tAc \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" \
    2>/dev/null | tr -d '[:space:]')

  if [ "${table_count:-0}" = "0" ]; then
    # Cria os roles que o dump referencia em OWNER TO mas que não existem
    # no ambiente Docker. São roles somente de posse (sem login).
    echo "==> Criando roles necessárias para o restore..."
    docker compose -f "$COMPOSE_FILE" --env-file .env exec -T postgres \
      psql -U "$DB_USER" -d "$DB_NAME" << 'SQL'
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

    echo "==> Restaurando backup (isso pode demorar alguns minutos)..."
    # Filtra a saída: exibe apenas ERRORs e FAILs; os avisos de role são esperados
    docker compose -f "$COMPOSE_FILE" --env-file .env exec -T postgres \
      psql -U "$DB_USER" -d "$DB_NAME" < "$backup_file" 2>&1 \
      | grep -E '^(ERROR|FATAL|psql:)' | grep -v 'role.*does not exist' || true

    echo "==> Backup restaurado com sucesso."
  else
    echo "==> Banco já possui ${table_count} tabela(s) — restore ignorado."
  fi
else
  echo "==> Nenhum arquivo de backup encontrado em backup/ — banco iniciado vazio."
fi

# ---------------------------------------------------------------------------
# 8. Sobe todos os serviços (o postgres já está rodando, o Tomcat vai subir)
# ---------------------------------------------------------------------------
echo "==> Subindo todos os serviços..."
docker compose -f "$COMPOSE_FILE" --env-file .env up -d --build

echo ""
echo "Deploy concluído. Aplicação disponível na porta ${APP_PORT:-8080}."
