-- =====================================================================
-- 30 - "Livro do Encontro": PDFs externos de Capa e História por retiro.
--
-- Duas colunas bytea em segue_me para guardar os PDFs (capa e história)
-- que a coordenação desenha à parte e sobe no cadastro do Segue-Me. O
-- ExecutorLivroEncontro concatena esses PDFs com os relatórios Jasper.
--
-- Idempotente (IF NOT EXISTS). Tipo bytea = mesmo das imagens (imagem,
-- imagem_fundo, imagem_roda), pois o mapeamento JPA é byte[].
--
-- ORDEM: rode ESTE script ANTES de subir o WAR com o novo mapeamento —
-- o SegueMe passa a referenciar arquivo_capa/arquivo_historia, e sem as
-- colunas qualquer SELECT em segue_me quebraria.
-- =====================================================================

ALTER TABLE segue_me ADD COLUMN IF NOT EXISTS arquivo_capa bytea;
ALTER TABLE segue_me ADD COLUMN IF NOT EXISTS arquivo_historia bytea;

-- Conferência:
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'segue_me' AND column_name IN ('arquivo_capa', 'arquivo_historia');
