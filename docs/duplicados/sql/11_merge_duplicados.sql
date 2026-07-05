-- =====================================================================
-- 11 - Merge (consolidação) dos duplicados COM participação própria.
--
-- Para cada grupo de nome repetido, o registro canônico (o de maior
-- participação) recebe as linhas de evento_segue_me dos duplicados; em
-- seguida os duplicados são inativados (ativo=false). Como SOMENTE
-- evento_segue_me referencia casal/seguidor/convidado, basta re-apontar
-- essa tabela — nenhum histórico é apagado.
--
-- Mapeamento (canonical, dup):  ver os VALUES abaixo.
--   seguidor: 21 grupos / 23 duplicados
--   casal:    19 grupos / 19 duplicados
--   convidado: nenhum (só havia casca — ver 10_).
--
-- ⚠️ SOBREPOSIÇÃO DE RETIRO (colisao): nestes grupos o duplicado
--    participou do MESMO retiro que o canônico, então o re-apontamento
--    pode gerar participação repetida no canônico. Confira na verificação:
--      seguidor: 592<-611, 968<-974, 915<-935/936, 431<-855, 921<-951/997
--    (casal: nenhum caso de sobreposição).
--
-- Pré-requisito: rodar 00_add_coluna_ativo.sql antes.
--
-- SEGURANÇA: roda dentro de uma transação. Revise as verificações e então
-- execute COMMIT (ou ROLLBACK). Via `psql -f` a transação é revertida ao
-- sair (dry-run).
-- =====================================================================

BEGIN;

-- ---------- SEGUIDOR ----------
-- 1) re-aponta a participação dos duplicados para o canônico
WITH map(canonical, dup) AS (VALUES
  (592,611),(968,974),(988,1004),(915,935),(915,936),(177,1041),(195,583),
  (289,1010),(262,1056),(275,864),(59,617),(917,932),(361,1192),(431,855),
  (913,925),(731,697),(922,937),(565,730),(995,1003),(921,951),(921,997),
  (15,589),(926,958))
UPDATE evento_segue_me e SET fk_seguidor_id = m.canonical
FROM map m WHERE e.fk_seguidor_id = m.dup;

-- 2) inativa os duplicados
WITH map(canonical, dup) AS (VALUES
  (592,611),(968,974),(988,1004),(915,935),(915,936),(177,1041),(195,583),
  (289,1010),(262,1056),(275,864),(59,617),(917,932),(361,1192),(431,855),
  (913,925),(731,697),(922,937),(565,730),(995,1003),(921,951),(921,997),
  (15,589),(926,958))
UPDATE seguidor s SET ativo = false FROM map m WHERE s.id = m.dup;

-- ---------- CASAL ----------
-- 1) re-aponta a participação dos duplicados para o canônico
WITH map(canonical, dup) AS (VALUES
  (48,188),(159,214),(20,137),(125,197),(164,192),(178,227),(27,64),
  (187,136),(47,185),(52,128),(36,158),(175,122),(38,205),(42,206),
  (160,199),(18,138),(156,13),(6,165),(196,176))
UPDATE evento_segue_me e SET fk_casal_id = m.canonical
FROM map m WHERE e.fk_casal_id = m.dup;

-- 2) inativa os duplicados
WITH map(canonical, dup) AS (VALUES
  (48,188),(159,214),(20,137),(125,197),(164,192),(178,227),(27,64),
  (187,136),(47,185),(52,128),(36,158),(175,122),(38,205),(42,206),
  (160,199),(18,138),(156,13),(6,165),(196,176))
UPDATE casal c SET ativo = false FROM map m WHERE c.id = m.dup;

-- =====================================================================
-- VERIFICAÇÕES
-- =====================================================================

-- (a) Nenhuma participação deve ter sobrado apontando para um duplicado (=0):
SELECT 'evento ainda em seguidor duplicado (0 esperado)' chk, count(*)
FROM evento_segue_me WHERE fk_seguidor_id IN (611,974,1004,935,936,1041,583,1010,1056,864,617,932,1192,855,925,697,937,730,1003,951,997,589,958)
UNION ALL
SELECT 'evento ainda em casal duplicado (0 esperado)', count(*)
FROM evento_segue_me WHERE fk_casal_id IN (188,214,137,197,192,227,64,136,185,128,158,122,205,206,199,138,13,165,176);

-- (b) Participação REPETIDA gerada no canônico (mesma pessoa + mesmo retiro
--     + mesma função/equipe/círculo/palestra). Revise MANUALMENTE — pode ser
--     duplicidade real a limpar, ou papéis legítimos distintos. NÃO apague
--     sem conferir (histórico é sagrado).
SELECT 'seguidor' tipo, fk_seguidor_id id_canonico, fk_segue_me_id retiro,
       fk_funcao_id, fk_equipe_id, fk_circulo_id, fk_palestra_id, count(*) qtd
FROM evento_segue_me
WHERE fk_seguidor_id IN (592,968,915,431,921)   -- canônicos dos grupos com sobreposição
GROUP BY 1,2,3,4,5,6,7 HAVING count(*) > 1
ORDER BY id_canonico, retiro;

-- (c) Resumo:
SELECT 'seguidor inativados (merge)' t, count(*) FROM seguidor WHERE id IN (611,974,1004,935,936,1041,583,1010,1056,864,617,932,1192,855,925,697,937,730,1003,951,997,589,958) AND NOT ativo
UNION ALL
SELECT 'casal inativados (merge)', count(*) FROM casal WHERE id IN (188,214,137,197,192,227,64,136,185,128,158,122,205,206,199,138,13,165,176) AND NOT ativo;

-- >>> Revise TUDO acima, em especial a verificação (b). <<<
-- Para EFETIVAR:  COMMIT;
-- Para DESCARTAR: ROLLBACK;
