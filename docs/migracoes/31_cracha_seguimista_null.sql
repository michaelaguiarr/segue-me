-- =====================================================================
-- 31 - Conserta crachás de seguimista com cracha = NULL.
--
-- SINTOMA: no Dashboard da Gráfica o botão "Não impresso" do seguimista
-- (por círculo / global) "não funcionava" — dava a impressão de já estar
-- impresso, mas não voltava para a fila de impressão.
--
-- CAUSA: a inscrição do seguimista é criada sem setar a flag `cracha`
-- (CadastroEventoService.salvar e CadastroEventoInscritoService.salvar só
-- montam Evento + Inscricao). A coluna tem DEFAULT true, mas o Hibernate
-- inclui a coluna no INSERT (Evento não usa @DynamicInsert) e grava NULL,
-- ignorando o DEFAULT. Os demais papéis (seguidor, casal, padre, convidado)
-- chamam setCracha(true) no cadastro, por isso só o seguimista adoecia.
--
-- EFEITO do NULL nas contas do dashboard:
--   pendentes = SUM(CASE WHEN cracha = true THEN 1 ELSE 0 END)  -> NULL não conta
--   impressos = total - pendentes                               -> NULL entra aqui
-- ou seja, um crachá nunca impresso (NULL) aparecia como IMPRESSO. E o
-- "Não impresso" usa "WHERE cracha = false", que NÃO casa NULL -> 0 linhas
-- atualizadas -> "não funciona". O "Marcar impresso" nem aparecia (pendentes
-- = 0 para esses).
--
-- CORREÇÃO: NULL = crachá nunca impresso = PENDENTE (a imprimir) = true.
-- O código passou a garantir o invariante em EventoRepository.guardar()
-- (null -> true) para novas gravações; este script conserta os já existentes.
--
-- ORDEM: pode rodar ANTES ou junto ao deploy (não altera o esquema, só dados).
-- Idempotente (só toca em linhas NULL). Backup do dump 2026-07-04: 49 linhas.
-- =====================================================================

UPDATE public.evento_segue_me
SET cracha = true
WHERE cracha IS NULL;

-- (Opcional, blindagem) impedir NULL no banco daqui pra frente. Seguro após
-- o UPDATE acima; o código já grava sempre true/false. Descomente se quiser
-- que o banco rejeite qualquer NULL futuro (falha explícita em vez de bug mudo):
-- ALTER TABLE public.evento_segue_me ALTER COLUMN cracha SET NOT NULL;

-- Conferência: deve retornar 0.
SELECT COUNT(*) AS crachas_null_restantes
FROM public.evento_segue_me
WHERE cracha IS NULL;
