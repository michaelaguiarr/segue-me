# Relatório de cadastros duplicados

Análise dos cadastros com **nome repetido** nas tabelas `seguidor`, `casal`,
`palestrante_convidado` e `padre`, com recomendação de ação por registro,
cruzando com o histórico de participação em `evento_segue_me`.

> Gerado a partir do banco de produção (snapshot). Os IDs referenciam as
> chaves primárias das respectivas tabelas.

## Como a duplicata foi detectada

Chave de agrupamento = nome **normalizado** (minúsculas, sem acento, espaços
colapsados), usando as colunas que o próprio sistema mantém
(`nome_sem_acento`, `nome_ele_sem_acento`, `nome_ela_sem_acento`), com
fallback para o nome original. Para `casal` a chave é o **par** `ele + ela`.

## Como a recomendação foi decidida

Para cada registro contamos a participação real em `evento_segue_me`
(`n_eventos`) e em quais retiros (`retiros_participou` = lista de
`fk_segue_me_id`). Dentro de cada grupo de nome repetido, o registro com mais
participação é o **canônico**.

| Ação | Significado | Seguro? |
|---|---|---|
| **MANTER** | Registro canônico do grupo (o que concentra o histórico), ou único com participação. | — |
| **INATIVAR** | "Casca" sem nenhuma participação própria — o histórico da pessoa está preservado no registro canônico. Pode ser inativado **agora**. | ✅ seguro |
| **MERGE** | Registro que **tem** histórico próprio e duplica o canônico. Antes de inativar é preciso **re-apontar** as linhas de `evento_segue_me` para o canônico, senão perde-se histórico. | ⚠️ requer script de merge |

Regra de domínio (CLAUDE.md): *o histórico de serviço é a memória do
movimento — nunca remover registros de participação, apenas inativar.*
Por isso nenhum registro com participação é marcado para inativação direta.

## Placar

| Tabela | MANTER | MERGE | INATIVAR | Total |
|---|---:|---:|---:|---:|
| seguidor | 55 | 23 | 36 | 114 |
| casal | 24 | 19 | 6 | 49 |
| palestrante_convidado | 1 | 0 | 1 | 2 |
| **Total** | **80** | **42** | **43** | **165** |

- **43 inativações seguras** (imediatas).
- **42 registros de merge** (consolidar histórico e então inativar o não-canônico).
- `padre`: nenhum duplicado.

## Arquivos

| Arquivo | Conteúdo |
|---|---|
| `01_resumo_duplicados.csv` | 1 linha por grupo: `qtd`, `manter`, `merge`, `inativar`, `acao_grupo`. |
| `02_seguidor_detalhe.csv` | 1 linha por seguidor duplicado, com `acao` e `motivo`. |
| `03_casal_detalhe.csv` | 1 linha por casal duplicado, com `acao` e `motivo`. |
| `04_convidado_detalhe.csv` | Palestrante convidado duplicado. |
| `05_anomalias_casal.csv` | Casal com `nome_ele` = `nome_ela` (erro de cadastro). |
| `06_candidatos_inativar.csv` | Lista acionável: só os registros `INATIVAR` (43). |
| `07_candidatos_merge.csv` | Lista acionável: só os registros `MERGE` (42). |

## Ressalvas

1. **Bug de fuso no `dt_nascimento`** — 16 grupos de seguidores têm o mesmo
   nome e nascimento diferindo em **exatamente 1 dia** (ex.: `felipe da silva
   tomaz de oliveira` 1997-12-16 vs 1997-12-15). É a meia-noite em
   `America/Sao_Paulo` virando o dia em UTC no campo `timestamp with time
   zone`. É a **causa-raiz** que multiplica cadastros; vale corrigir o
   mapeamento JPA / tipo de coluna.
2. **Possíveis homônimos reais** — 4 grupos têm nascimento bem diferente
   (`gabriella bentes marques` 1999 vs 1988; `lucas tavora minotto` mês
   trocado; etc.). Conferir antes de tratar como duplicata.
3. **Anomalia `casal` 145** — `Maria Do Ceu De Sena Moura` nas duas colunas
   (`05_anomalias_casal.csv`). Erro de digitação, não é duplicata de nome.
4. **Mecanismo de inativação ainda não existe no schema** —
   `seguidor.situacaoseguidor` só usa `ATIVO`/null (nunca `INATIVO`), e a
   tabela `casal` **não tem** coluna de status. Inativar exige, antes,
   definir esse mecanismo (novo valor de status e/ou coluna `ativo`).

## Scripts SQL (`sql/`)

Mecanismo escolhido: coluna `ativo` boolean (convenção do `padre`).
Todos transacionais e reversíveis. **Validados em dry-run** (rodados numa
transação e revertidos com ROLLBACK).

| Script | O que faz |
|---|---|
| `sql/00_add_coluna_ativo.sql` | Adiciona `ativo boolean NOT NULL DEFAULT true` em seguidor, casal e palestrante_convidado (idempotente). Rodar **primeiro**. |
| `sql/10_inativar_cascas.sql` | `ativo=false` nos 43 registros sem participação (36 seg + 6 casal + 1 conv). |
| `sql/11_merge_duplicados.sql` | Re-aponta `evento_segue_me` do duplicado → canônico (25 seg + 21 casal) e inativa os 42 duplicados. |

Ordem: `00` → `10` → `11`. Cada script roda dentro de `BEGIN;`; revise as
verificações e então `COMMIT;` (ou `ROLLBACK;`). Rodar via `psql -f` deixa a
transação aberta e reverte ao sair — ou seja, funciona como dry-run.

**Conferência manual pós-merge:** o script `11` isola (verificação *b*) as
participações repetidas geradas no canônico por sobreposição de retiro. No
snapshot atual são 2 linhas (canônicos 431 e 915, retiro 50) — revisar se são
duplicidade a remover ou papéis legítimos distintos. **Nunca apagar sem conferir.**

> ⚠️ Pendência de aplicação: adicionar a coluna `ativo` **não** faz a app
> escondê-los. É preciso mapear o campo nas entidades JPA (`Seguidor`,
> `Casal`, `PalestranteConvidado`) e filtrar por `ativo=true` nas listagens.

## Próximos passos

- Aplicar `00`/`10`/`11` no ambiente desejado (dev → prod), com backup antes.
- Mapear `ativo` nas entidades JPA e filtrar as consultas.
- Corrigir a causa-raiz do fuso no `dt_nascimento` (tipo/mapeamento).
- Tratar os 4 possíveis homônimos e a anomalia do casal 145.
