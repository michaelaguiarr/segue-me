#!/usr/bin/env bash
# =====================================================================
# db-prod.sh — roda scripts SQL no banco de PRODUÇÃO sem SGBD externo.
#
# Em prod o PostgreSQL não expõe porta (só `expose: 5432` na rede do
# Docker), então não dá para conectar um SGBD/GUI de fora. O acesso é
# pelo próprio host, via `docker compose exec postgres psql` — o mesmo
# caminho que o deploy.sh usa para restaurar backup.
#
# Uso (rode no HOST de produção, na raiz do projeto):
#   ./scripts/db-prod.sh backup                 # pg_dump antes de mexer
#   ./scripts/db-prod.sh run   <arquivo.sql>    # aplica script que se
#                                               # auto-commita (índices,
#                                               # add_coluna) — idempotente
#   ./scripts/db-prod.sh dry   <arquivo.sql>    # DRY-RUN dos scripts que
#                                               # abrem BEGIN sem COMMIT
#                                               # (10/11/20): roda, mostra
#                                               # as verificações e REVERTE
#   ./scripts/db-prod.sh apply <arquivo.sql>    # aplica de fato (anexa
#                                               # COMMIT) — só depois de
#                                               # revisar o dry-run
#   ./scripts/db-prod.sh psql                   # abre um psql interativo
#
# Regras de segurança embutidas:
#   * ON_ERROR_STOP=1 — aborta no 1º erro (nunca aplica pela metade).
#   * `apply` roda tudo + COMMIT numa passada só; se algo falhar, o
#     COMMIT não é alcançado e a transação é revertida.
#   * `run`/`apply` sempre lembram de fazer `backup` antes.
# =====================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_FILE="docker-compose.prod.yml"

# --- valida .env e credenciais (mesmo contrato do deploy.sh) ---
if [ ! -f ".env" ]; then
  echo "ERRO: .env não encontrado. Copie de .env.prod.example e preencha." >&2
  exit 1
fi
set -a; source .env; set +a
: "${DB_USER:?defina DB_USER no .env}"
: "${DB_NAME:?defina DB_NAME no .env}"

COMPOSE=(docker compose -f "$COMPOSE_FILE" --env-file .env)
# psql não-interativo, abortando no primeiro erro
PSQL=("${COMPOSE[@]}" exec -T postgres psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1)

require_file() {
  [ -n "${1:-}" ] || { echo "ERRO: informe o arquivo .sql" >&2; exit 1; }
  [ -f "$1" ]     || { echo "ERRO: arquivo não encontrado: $1" >&2; exit 1; }
}

case "${1:-}" in
  backup)
    mkdir -p backup
    out="backup/pre-sql-$(date +%Y%m%d_%H%M%S).sql"
    echo "==> Gerando backup em $out ..."
    "${COMPOSE[@]}" exec -T postgres pg_dump -U "$DB_USER" -d "$DB_NAME" > "$out"
    echo "    OK ($(du -h "$out" | cut -f1))."
    ;;

  run)     # scripts que se auto-commitam (índices / add_coluna)
    require_file "${2:-}"
    echo "==> Aplicando (auto-commit): $2"
    "${PSQL[@]}" < "$2"
    echo "    OK."
    ;;

  dry)     # scripts BEGIN-sem-COMMIT: roda e REVERTE (mostra verificações)
    require_file "${2:-}"
    echo "==> DRY-RUN (será revertido): $2"
    echo "    Revise a saída das verificações abaixo antes de dar 'apply'."
    echo "----------------------------------------------------------------"
    "${PSQL[@]}" < "$2"
    echo "----------------------------------------------------------------"
    echo "    Dry-run concluído e REVERTIDO. Nada foi gravado."
    ;;

  apply)   # scripts BEGIN-sem-COMMIT: aplica de fato (anexa COMMIT)
    require_file "${2:-}"
    echo "==> APPLY (COMMIT) de: $2"
    read -r -p "    Confirma aplicar em PRODUÇÃO? (digite APLICAR): " ans
    [ "$ans" = "APLICAR" ] || { echo "    Cancelado."; exit 1; }
    { cat "$2"; printf '\nCOMMIT;\n'; } | "${PSQL[@]}"
    echo "    Aplicado."
    ;;

  psql)
    "${COMPOSE[@]}" exec postgres psql -U "$DB_USER" -d "$DB_NAME"
    ;;

  *)
    sed -n '2,33p' "$0"   # imprime o cabeçalho de ajuda
    exit 1
    ;;
esac
