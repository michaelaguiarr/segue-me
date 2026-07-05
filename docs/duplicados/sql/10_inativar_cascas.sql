-- =====================================================================
-- 10 - Inativação segura das "cascas" (registros duplicados SEM
--      participação própria em evento_segue_me).
--
-- São 43 registros: 36 seguidores + 6 casais + 1 convidado.
-- O histórico de cada pessoa está preservado no registro canônico do
-- grupo, portanto nada de participação é perdido aqui.
--
-- Pré-requisito: rodar 00_add_coluna_ativo.sql antes.
--
-- SEGURANÇA: roda dentro de uma transação. Revise a verificação ao final
-- e então execute COMMIT (ou ROLLBACK para descartar). Se rodar via
-- `psql -f`, a transação fica aberta e é revertida ao sair (dry-run).
-- =====================================================================

BEGIN;

-- SEGUIDOR (36 cascas)
UPDATE seguidor SET ativo = false
WHERE id IN (173,218,263,278,306,454,521,720,733,809,822,824,833,918,923,924,
             978,980,981,984,986,993,1194,1196,1197,1198,1199,1200,1204,1205,
             1206,1207,1208,1209,1283,1284)
  AND ativo;   -- guarda contra reexecução

-- CASAL (6 cascas)
UPDATE casal SET ativo = false
WHERE id IN (7,126,147,149,237,305)
  AND ativo;

-- PALESTRANTE_CONVIDADO (1 casca)
UPDATE palestrante_convidado SET ativo = false
WHERE id IN (10)
  AND ativo;

-- Verificação: nenhum destes registros pode ter participação (deve dar 0).
SELECT 'seguidor c/ participacao (deveria ser 0)' chk, count(*)
FROM evento_segue_me WHERE fk_seguidor_id IN (173,218,263,278,306,454,521,720,733,809,822,824,833,918,923,924,978,980,981,984,986,993,1194,1196,1197,1198,1199,1200,1204,1205,1206,1207,1208,1209,1283,1284)
UNION ALL
SELECT 'casal c/ participacao (deveria ser 0)', count(*)
FROM evento_segue_me WHERE fk_casal_id IN (7,126,147,149,237,305)
UNION ALL
SELECT 'convidado c/ participacao (deveria ser 0)', count(*)
FROM evento_segue_me WHERE fk_palestrante_convidado_id IN (10);

-- Conferência do resultado:
SELECT 'seguidor inativados' t, count(*) FROM seguidor WHERE NOT ativo
UNION ALL SELECT 'casal inativados', count(*) FROM casal WHERE NOT ativo
UNION ALL SELECT 'convidado inativados', count(*) FROM palestrante_convidado WHERE NOT ativo;

-- >>> Revise os resultados acima. <<<
-- Para EFETIVAR:  COMMIT;
-- Para DESCARTAR: ROLLBACK;
