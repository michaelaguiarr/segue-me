-- Índices de performance para as telas de pesquisa/listagem.
--
-- Motivação: no PostgreSQL as colunas de chave estrangeira NÃO são indexadas
-- automaticamente. As pesquisas (seguidor, casal, padre, convidado, inscrição)
-- fazem múltiplos JOINs e filtram por FK (ex.: fk_segue_id) — sem índice, viram
-- seq scans. A tabela evento_segue_me é a mais crítica (join com quase todas).
--
-- Observação: com o volume atual (poucos milhares de linhas) o planner ainda
-- pode preferir seq scan; o ganho cresce com os dados. São todos idempotentes
-- (IF NOT EXISTS) e não-destrutivos.
--
-- Colunas de baixa cardinalidade (ativo, situacaoseguidor, statusinscricao,
-- cracha) foram deixadas de fora de propósito: btree simples não ajuda nelas.
-- Busca por nome usa LIKE '%...%' (curinga à esquerda), que btree também não
-- cobre — para isso seria necessário extensão pg_trgm + índice GIN (fora deste
-- escopo).

BEGIN;

-- evento_segue_me: tabela central do quadrante, join-pesada em todas as consultas de evento/inscrição
CREATE INDEX IF NOT EXISTS idx_evento_fk_seguidor_id ON evento_segue_me (fk_seguidor_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_casal_id ON evento_segue_me (fk_casal_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_padre_id ON evento_segue_me (fk_padre_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_palestrante_convidado_id ON evento_segue_me (fk_palestrante_convidado_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_funcao_id ON evento_segue_me (fk_funcao_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_segue_me_id ON evento_segue_me (fk_segue_me_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_equipe_id ON evento_segue_me (fk_equipe_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_inscricao_id ON evento_segue_me (fk_inscricao_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_palestra_id ON evento_segue_me (fk_palestra_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_usuario_id ON evento_segue_me (fk_usuario_id);
CREATE INDEX IF NOT EXISTS idx_evento_fk_circulo_id ON evento_segue_me (fk_circulo_id);

-- seguidor
CREATE INDEX IF NOT EXISTS idx_seguidor_fk_segue_id ON seguidor (fk_segue_id);
CREATE INDEX IF NOT EXISTS idx_seguidor_fk_paroquia_id ON seguidor (fk_paroquia_id);
CREATE INDEX IF NOT EXISTS idx_seguidor_fk_circulo_id ON seguidor (fk_circulo_id);
CREATE INDEX IF NOT EXISTS idx_seguidor_fk_sexo_id ON seguidor (fk_sexo_id);
CREATE INDEX IF NOT EXISTS idx_seguidor_fk_endereco_id ON seguidor (fk_endereco_id);

-- casal
CREATE INDEX IF NOT EXISTS idx_casal_fk_paroquia_id ON casal (fk_paroquia_id);
CREATE INDEX IF NOT EXISTS idx_casal_fk_ecc_id ON casal (fk_ecc_id);
CREATE INDEX IF NOT EXISTS idx_casal_fk_endereco_id ON casal (fk_endereco_id);

-- padre
CREATE INDEX IF NOT EXISTS idx_padre_fk_paroquia_id ON padre (fk_paroquia_id);
CREATE INDEX IF NOT EXISTS idx_padre_fk_endereco_id ON padre (fk_endereco_id);

-- palestrante_convidado
CREATE INDEX IF NOT EXISTS idx_convidado_fk_sexo_id ON palestrante_convidado (fk_sexo_id);
CREATE INDEX IF NOT EXISTS idx_convidado_fk_endereco_id ON palestrante_convidado (fk_endereco_id);

-- inscricao (numero é usado em filtro por código e na ordenação da listagem)
CREATE INDEX IF NOT EXISTS idx_inscricao_fk_segue_me ON inscricao (fk_segue_me);
CREATE INDEX IF NOT EXISTS idx_inscricao_fk_paroquia_id ON inscricao (fk_paroquia_id);
CREATE INDEX IF NOT EXISTS idx_inscricao_fk_endereco_id ON inscricao (fk_endereco_id);
CREATE INDEX IF NOT EXISTS idx_inscricao_fk_responsavel_inscricao ON inscricao (fk_responsavel_inscricao);
CREATE INDEX IF NOT EXISTS idx_inscricao_numero ON inscricao (numero);

-- circulo / segue_me
CREATE INDEX IF NOT EXISTS idx_circulo_fk_segue_id ON circulo (fk_segue_id);
CREATE INDEX IF NOT EXISTS idx_segue_me_fk_numero_cod ON segue_me (fk_numero_cod);

COMMIT;
