-- =====================================================================
-- 20 - Causa-raiz do fuso: dt_nascimento como `date` (não timestamptz)
--
-- Uma data de nascimento é um dia de calendário puro — não tem hora nem
-- fuso. Armazenada como `timestamp with time zone`, cada escrita/leitura
-- em fuso diferente desloca o dia (bug do "±1 dia" entre cadastros).
-- Aqui convertemos as colunas para o tipo `date`, o que elimina de vez o
-- componente de hora/fuso e para o drift.
--
-- Regra de conversão: (col AT TIME ZONE 'UTC')::date
--   Recupera a data como o instante armazenado em UTC — ou seja, a data
--   "que o import trazia". Registros digitados no app (gravados à meia-
--   noite de São Paulo) NÃO mudam; os ~557 registros gravados à meia-noite
--   UTC (imports), que o app exibia 1 dia a menos, passam a mostrar o dia
--   real digitado.
--
-- Colunas afetadas:
--   seguidor.dt_nascimento
--   casal.dt_nascimento_ele, casal.dt_nascimento_ela
--   padre.dt_nascimento
--   palestrante_convidado.dt_nascimento
--
-- OBS: dt_casament (casal) e dt_ordenacao (padre) têm o MESMO problema
-- latente, mas ficam fora deste script (escopo = dt_nascimento).
--
-- Requer deploy conjunto: o mapeamento das entidades deve passar a
-- @Temporal(TemporalType.DATE) + columnDefinition = "date" (mesmo commit).
--
-- Rodar UMA vez. Não é idempotente (ALTER ... TYPE).
--
-- SEGURANÇA: roda dentro de uma transação. Revise a verificação e então
-- COMMIT (ou ROLLBACK). Via `psql -f` a transação é revertida ao sair.
-- Faça BACKUP antes de efetivar — é uma mudança de tipo em produção.
-- =====================================================================

BEGIN;

ALTER TABLE seguidor
  ALTER COLUMN dt_nascimento TYPE date
  USING (dt_nascimento AT TIME ZONE 'UTC')::date;

ALTER TABLE casal
  ALTER COLUMN dt_nascimento_ele TYPE date
    USING (dt_nascimento_ele AT TIME ZONE 'UTC')::date,
  ALTER COLUMN dt_nascimento_ela TYPE date
    USING (dt_nascimento_ela AT TIME ZONE 'UTC')::date;

ALTER TABLE padre
  ALTER COLUMN dt_nascimento TYPE date
  USING (dt_nascimento AT TIME ZONE 'UTC')::date;

ALTER TABLE palestrante_convidado
  ALTER COLUMN dt_nascimento TYPE date
  USING (dt_nascimento AT TIME ZONE 'UTC')::date;

-- Verificação: todas as colunas devem estar como `date`.
SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE column_name LIKE 'dt_nascimento%'
  AND table_name IN ('seguidor','casal','padre','palestrante_convidado')
ORDER BY table_name, column_name;

-- >>> Revise. Para EFETIVAR: COMMIT;   Para DESCARTAR: ROLLBACK;
