# Runbook — rodar os scripts SQL em PRODUÇÃO (sem SGBD externo)

## Por que assim

Em produção o PostgreSQL **não expõe porta** (`docker-compose.prod.yml` usa
`expose: 5432`, sem `ports:`), então nenhum SGBD/GUI (DBeaver, pgAdmin) alcança
o banco de fora — de propósito, por segurança. O acesso é pelo **host de
produção**, via `docker compose exec postgres psql`, exatamente como o
`deploy.sh` já faz para restaurar backup.

O helper `scripts/db-prod.sh` encapsula esse caminho. Rode **no host de prod, na
raiz do projeto**, com o `.env` preenchido (mesmo do deploy).

## As duas naturezas de script

| Script | O que faz | Aplica como |
|---|---|---|
| `docs/indices/00_indices_fk_performance.sql` | 30 índices de FK (`IF NOT EXISTS`) | **`run`** — auto-commita, idempotente |
| `docs/duplicados/sql/00_add_coluna_ativo.sql` | adiciona coluna `ativo` (`IF NOT EXISTS`) | **`run`** — auto-commita, idempotente |
| `docs/duplicados/sql/10_inativar_cascas.sql` | inativa 43 duplicados-casca | **`dry` → revisar → `apply`** |
| `docs/duplicados/sql/11_merge_duplicados.sql` | re-aponta participação + inativa duplicados | **`dry` → revisar → `apply`** |
| `docs/duplicados/sql/20_fix_dt_nascimento_tipo_date.sql` | muda `dt_nascimento` para `date` | **`dry` → revisar → `apply`** |

Os 3 últimos abrem `BEGIN` **sem `COMMIT`** de propósito: rodados direto (`dry`)
eles mostram as verificações e **revertem** (dry-run). Só o `apply` grava.

## Sequência recomendada

```bash
# 0) SEMPRE um backup antes de mexer em dados
./scripts/db-prod.sh backup

# 1) Índices (seguro, idempotente, pode rodar a qualquer momento)
./scripts/db-prod.sh run docs/indices/00_indices_fk_performance.sql

# 2) Coluna `ativo` (pré-requisito dos passos 3 e 4)
./scripts/db-prod.sh run docs/duplicados/sql/00_add_coluna_ativo.sql

# 3) Inativar cascas — DRY-RUN, revisar a verificação, então aplicar
./scripts/db-prod.sh dry   docs/duplicados/sql/10_inativar_cascas.sql
./scripts/db-prod.sh apply docs/duplicados/sql/10_inativar_cascas.sql

# 4) Merge de duplicados — DRY-RUN e revisar em especial a verificação (b)
#    (participação repetida no canônico: pode exigir limpeza manual)
./scripts/db-prod.sh dry   docs/duplicados/sql/11_merge_duplicados.sql
./scripts/db-prod.sh apply docs/duplicados/sql/11_merge_duplicados.sql

# 5) Corrigir tipo de dt_nascimento — DRY-RUN, revisar, aplicar
./scripts/db-prod.sh dry   docs/duplicados/sql/20_fix_dt_nascimento_tipo_date.sql
./scripts/db-prod.sh apply docs/duplicados/sql/20_fix_dt_nascimento_tipo_date.sql
```

`apply` pede confirmação (`digite APLICAR`) e roda tudo + `COMMIT` numa passada,
com `ON_ERROR_STOP=1` — se qualquer statement falhar, o `COMMIT` não é alcançado
e nada é gravado.

## Depende do app (importante)

O `00_add_coluna_ativo` só tem efeito visível se o app **filtrar por
`ativo = true`** nas listagens (Seguidor, Casal, PalestranteConvidado). Enquanto
o código não filtrar, os registros inativados/merjados continuam aparecendo na
aplicação — mas o banco já estará correto. Alinhe o deploy do WAR com esses
passos.

## Reexecução / segurança

- Índices e `add_coluna`: idempotentes (`IF NOT EXISTS`), seguros re-rodar.
- `inativar`/`merge`: idempotentes **após o commit** (o `apply` só re-aponta o
  que ainda aponta para um duplicado e só inativa o que ainda está ativo) — rodar
  de novo não duplica efeito. Mesmo assim, faça `backup` antes.
- Nunca rode `apply` sem antes olhar o `dry`.

## Alternativa (futuro)

Para migrações **recorrentes e versionadas** (schema evoluindo), vale integrar
**Flyway** ao WAR (scripts em `db/migration`, roda no startup, tabela de
histórico). Não é o ideal para ESTES scripts, que são pontuais e feitos para
revisão manual antes do commit — por isso aqui o caminho `docker exec psql`.
