-- =====================================================================
-- 32 - Sincroniza o círculo do SEGUIMISTA em Seguidor.circulo/cor_circulo.
--
-- SINTOMA: na Lista de Seguimistas (evento-seguidor/pesquisa-seguimista) o
-- seguimista aparecia "sem círculo", mesmo estando num círculo (o dashboard,
-- a montagem de círculos e os crachás mostravam o círculo certo).
--
-- CAUSA: há dois vínculos de círculo — Evento.circulo (fk_circulo_id em
-- evento_segue_me) e Seguidor.circulo/cor_circulo. A Lista de Seguimistas e
-- os relatórios leem de Seguidor.circulo; o dashboard lê de Evento.circulo.
-- Ao adicionar um seguimista pelo cadastro de círculo, o
-- CadastroCirculoService.salvar só sincronizava Seguidor.circulo quando o
-- Evento tinha função ("if (e.getFuncao() != null)"). Mas SEGUIMISTA tem
-- funcao = NULL — então o Seguidor ficava sem círculo. Corrigido no código
-- (passa a sincronizar sempre); este script conserta os dados já existentes.
--
-- O QUE FAZ: para cada seguimista (evento com inscrição e SEM função) que
-- está num círculo, copia o círculo/cor do Evento para o Seguidor. Usa o
-- seguimista mais recente (maior evento.id) por pessoa, de forma determinística.
-- Só toca em quem está divergente. Idempotente.
--
-- NOMES DE COLUNA (atenção): circulo.corcirculo (sem "_") x seguidor.cor_circulo.
-- Ambas guardam o enum CorCirculo como texto.
--
-- ORDEM: só mexe em dados (não altera esquema); pode rodar com o app no ar.
-- =====================================================================

UPDATE public.seguidor s
SET fk_circulo_id = sub.circ,
    cor_circulo   = sub.cor
FROM (
    SELECT DISTINCT ON (e.fk_seguidor_id)
           e.fk_seguidor_id AS sid,
           e.fk_circulo_id  AS circ,
           c.corcirculo     AS cor
    FROM public.evento_segue_me e
    JOIN public.circulo c ON c.id = e.fk_circulo_id
    WHERE e.fk_funcao_id   IS NULL        -- seguimista (sem função)
      AND e.fk_inscricao_id IS NOT NULL   -- é inscrição (seguimista)
      AND e.fk_circulo_id  IS NOT NULL    -- está num círculo
    ORDER BY e.fk_seguidor_id, e.id DESC  -- círculo do seguimista mais recente
) sub
WHERE s.id = sub.sid
  AND s.fk_circulo_id IS DISTINCT FROM sub.circ;

-- Conferência: seguimistas em círculo cujo Seguidor ainda diverge (esperado ~0;
-- só sobra se a mesma pessoa for seguimista em círculos diferentes).
SELECT COUNT(*) AS seguidores_ainda_divergentes
FROM public.evento_segue_me e
JOIN public.seguidor s ON s.id = e.fk_seguidor_id
WHERE e.fk_funcao_id   IS NULL
  AND e.fk_inscricao_id IS NOT NULL
  AND e.fk_circulo_id  IS NOT NULL
  AND s.fk_circulo_id IS DISTINCT FROM e.fk_circulo_id;
