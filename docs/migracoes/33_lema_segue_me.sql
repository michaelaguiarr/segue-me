-- =====================================================================
-- 33 - Lema (tema) do encontro, usado no "santinho" do quadrante.
--
-- Coluna texto opcional em segue_me. Preenchida pela coordenação na
-- edição do encontro (dashboard); se vazia, a linha do lema some do
-- cartão do quadrante (nunca é gerada automático).
--
-- Idempotente (IF NOT EXISTS).
--
-- ORDEM: rode ESTE script ANTES de subir o WAR com o novo mapeamento —
-- o SegueMe passa a referenciar "lema", e sem a coluna qualquer SELECT
-- em segue_me quebraria.
-- =====================================================================

ALTER TABLE segue_me ADD COLUMN IF NOT EXISTS lema varchar(180);

-- Conferência:
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'segue_me' AND column_name = 'lema';
