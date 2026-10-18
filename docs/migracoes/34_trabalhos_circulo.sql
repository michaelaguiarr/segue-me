-- =====================================================================
-- 34 - "Trabalhos" do círculo: 3 PDFs externos (atividade/dinâmica da
-- sessão), preparados pela coordenação e anexados separadamente.
--
-- Três colunas bytea em circulo — cada círculo tem EXATAMENTE 3
-- trabalhos, cada um anexado/baixado à parte (não é 1 upload só).
-- Mesmo tipo das demais colunas de anexo (segue_me.arquivo_capa /
-- arquivo_historia), pois o mapeamento JPA é byte[].
--
-- Idempotente (IF NOT EXISTS).
--
-- ORDEM: rode ESTE script ANTES de subir o WAR com o novo mapeamento —
-- o Circulo passa a referenciar trabalho1/trabalho2/trabalho3, e sem as
-- colunas qualquer SELECT em circulo quebraria.
-- =====================================================================

ALTER TABLE circulo ADD COLUMN IF NOT EXISTS trabalho1 bytea;
ALTER TABLE circulo ADD COLUMN IF NOT EXISTS trabalho2 bytea;
ALTER TABLE circulo ADD COLUMN IF NOT EXISTS trabalho3 bytea;

-- Conferência:
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'circulo' AND column_name IN ('trabalho1', 'trabalho2', 'trabalho3');
