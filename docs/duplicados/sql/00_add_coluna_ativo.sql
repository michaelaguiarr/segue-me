-- =====================================================================
-- 00 - Mecanismo de inativação (soft-delete)
-- Adiciona a coluna `ativo` em seguidor, casal e palestrante_convidado,
-- seguindo a convenção que a tabela `padre` já usa (ativo boolean).
--
-- Idempotente (IF NOT EXISTS). Rode UMA vez antes de 10_ e 11_.
--
-- ATENÇÃO app: após adicionar a coluna, o código Java/JPA precisa passar a
-- filtrar por ativo=true nas consultas de listagem (Seguidor, Casal,
-- PalestranteConvidado) e mapear o campo nas entidades. Enquanto isso não
-- for feito, os registros inativados continuarão aparecendo na aplicação.
-- =====================================================================

ALTER TABLE seguidor              ADD COLUMN IF NOT EXISTS ativo boolean NOT NULL DEFAULT true;
ALTER TABLE casal                 ADD COLUMN IF NOT EXISTS ativo boolean NOT NULL DEFAULT true;
ALTER TABLE palestrante_convidado ADD COLUMN IF NOT EXISTS ativo boolean NOT NULL DEFAULT true;

-- Conferência:
SELECT 'seguidor' t, count(*) FILTER (WHERE ativo) ativos, count(*) FILTER (WHERE NOT ativo) inativos FROM seguidor
UNION ALL SELECT 'casal', count(*) FILTER (WHERE ativo), count(*) FILTER (WHERE NOT ativo) FROM casal
UNION ALL SELECT 'palestrante_convidado', count(*) FILTER (WHERE ativo), count(*) FILTER (WHERE NOT ativo) FROM palestrante_convidado;
