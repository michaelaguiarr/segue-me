
package com.segue.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.segue.filter.EventoCasalFilter;
import com.segue.filter.EventoFilter;
import com.segue.filter.EventoPadreFilter;
import com.segue.filter.EventoSeguidorFilter;
import com.segue.filter.EventoSeguimistaFilter;
import com.segue.filter.PalestraCasalFilter;
import com.segue.filter.PalestraConvidadoFilter;
import com.segue.filter.PalestraPadreFilter;
import com.segue.filter.PalestraSeguidorFilter;
import com.segue.model.Casal;
import com.segue.model.Circulo;
import com.segue.model.CorCirculo;
import com.segue.model.Equipe;
import com.segue.model.Evento;
import com.segue.model.Funcao;
import com.segue.model.Inscricao;
import com.segue.model.NumeroRomano;
import com.segue.model.Padre;
import com.segue.model.Palestra;
import com.segue.model.PalestranteConvidado;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.model.Sexo;
import com.segue.model.StatusConvite;
import com.segue.model.StatusInscricao;
import com.segue.service.NegocioException;

public class EventoRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Evento porId(Long id) {
		return manager.find(Evento.class, id);
	}

	public Evento guardar(Evento evento) {
		Evento novo = manager.merge(evento);
		manager.flush();
		return novo;
	}

	/**
	 * Atualiza em lote apenas a flag {@code cracha} dos eventos informados, via
	 * JPQL UPDATE (não carrega entidades nem imagens). Deve ser chamado dentro de
	 * uma transação (ver serviços @Transactional).
	 */
	public int atualizarCracha(List<Long> ids, boolean valor) {
		if (ids == null || ids.isEmpty()) {
			return 0;
		}
		return manager.createQuery("UPDATE Evento e SET e.cracha = :valor WHERE e.id IN :ids")
				.setParameter("valor", valor).setParameter("ids", ids).executeUpdate();
	}

	public void remover(Evento evento) throws NegocioException {
		try {
			evento = porId(evento.getId());
			manager.remove(evento);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Não pode ser excluído.");
		}
	}

	public List<Evento> listaALL() {
		return manager.createQuery("SELECT e FROM Evento e " + "JOIN FETCH e.seguidor s " + "JOIN FETCH e.casal c "
				+ "JOIN FETCH e.padre p " + "JOIN FETCH e.palestranteConvidado pc " + "JOIN FETCH e.segueMe sm "
				+ "JOIN FETCH e.equipe e " + "JOIN FETCH e.funcao f " + "JOIN FETCH e.inscricao i "
				+ "JOIN FETCH e.palestra p ", Evento.class).getResultList();
	}

	public List<Evento> listaALLCrachaSeguidor() {
		return manager.createQuery("SELECT e FROM Evento e " + "JOIN FETCH e.seguidor s " + "JOIN FETCH e.segueMe sm "
				+ "JOIN FETCH e.equipe e " + "JOIN FETCH e.funcao f " + "WHERE e.cracha = true AND e.seguidor != null ",
				Evento.class).getResultList();
	}

	// ===== Dashboard da Equipe Gráfica: agregações por Segue-Me =====
	// A flag e.cracha = true significa crachá PENDENTE de impressão.

	/** Resumo de crachás [total, pendentes] de uma população de eventos do Segue-Me. */
	private long[] resumoCracha(String jpqlFrom, SegueMe segueMe) {
		Object[] r = (Object[]) manager
				.createQuery("SELECT COUNT(e), COALESCE(SUM(CASE WHEN e.cracha = true THEN 1 ELSE 0 END), 0) "
						+ jpqlFrom + " WHERE e.segueMe = :sm")
				.setParameter("sm", segueMe).getSingleResult();
		return new long[] { ((Number) r[0]).longValue(), ((Number) r[1]).longValue() };
	}

	/** [total, pendentes] dos crachás de seguidores de serviço (equipe/função). */
	public long[] resumoCrachaSeguidores(SegueMe sm) {
		return resumoCracha("FROM Evento e JOIN e.seguidor s JOIN e.equipe eq JOIN e.funcao f", sm);
	}

	/** [total, pendentes] dos crachás de seguimistas (inscrição aprovada). */
	public long[] resumoCrachaSeguimistas(SegueMe sm) {
		Object[] r = (Object[]) manager
				.createQuery("SELECT COUNT(e), COALESCE(SUM(CASE WHEN e.cracha = true THEN 1 ELSE 0 END), 0) "
						+ "FROM Evento e JOIN e.seguidor s JOIN e.inscricao i "
						+ "WHERE e.segueMe = :sm AND i.statusInscricao = :aprovado")
				.setParameter("sm", sm).setParameter("aprovado", StatusInscricao.APROVADO).getSingleResult();
		return new long[] { ((Number) r[0]).longValue(), ((Number) r[1]).longValue() };
	}

	/** [total, pendentes] dos crachás de casais de serviço. */
	public long[] resumoCrachaCasais(SegueMe sm) {
		return resumoCracha("FROM Evento e JOIN e.casal c JOIN e.equipe eq JOIN e.funcao f", sm);
	}

	/** [total, pendentes] dos crachás de padres/bispos de serviço. */
	public long[] resumoCrachaPadres(SegueMe sm) {
		return resumoCracha("FROM Evento e JOIN e.padre p JOIN e.equipe eq JOIN e.funcao f", sm);
	}

	/** [total, pendentes] dos crachás de palestrantes convidados. */
	public long[] resumoCrachaConvidados(SegueMe sm) {
		Object[] r = (Object[]) manager
				.createQuery("SELECT COUNT(e), COALESCE(SUM(CASE WHEN e.cracha = true THEN 1 ELSE 0 END), 0) "
						+ "FROM Evento e JOIN e.palestranteConvidado pc JOIN e.palestra pl "
						+ "WHERE e.segueMe = :sm AND e.equipe IS NULL AND e.funcao IS NULL")
				.setParameter("sm", sm).getSingleResult();
		return new long[] { ((Number) r[0]).longValue(), ((Number) r[1]).longValue() };
	}

	// ===== Ids de crachás PENDENTES (cracha = true) por papel, para o botão
	// "Marcar como impresso" do Dashboard. Mesma condição de papel dos
	// resumoCracha* — o que é contado é o que é marcado. =====

	// cracha=true → PENDENTES (a imprimir); cracha=false → JÁ IMPRESSOS (para desmarcar).
	private List<Long> idsCracha(String jpqlFrom, SegueMe sm, boolean cracha) {
		return manager.createQuery("SELECT e.id " + jpqlFrom + " WHERE e.segueMe = :sm AND e.cracha = :cracha", Long.class)
				.setParameter("sm", sm).setParameter("cracha", cracha).getResultList();
	}

	private static final String FROM_SEGUIDOR = "FROM Evento e JOIN e.seguidor s JOIN e.equipe eq JOIN e.funcao f";
	private static final String FROM_CASAL = "FROM Evento e JOIN e.casal c JOIN e.equipe eq JOIN e.funcao f";
	private static final String FROM_PADRE = "FROM Evento e JOIN e.padre p JOIN e.equipe eq JOIN e.funcao f";

	public List<Long> idsCrachaPendenteSeguidores(SegueMe sm) { return idsCracha(FROM_SEGUIDOR, sm, true); }
	public List<Long> idsCrachaImpressoSeguidores(SegueMe sm) { return idsCracha(FROM_SEGUIDOR, sm, false); }
	public List<Long> idsCrachaPendenteCasais(SegueMe sm) { return idsCracha(FROM_CASAL, sm, true); }
	public List<Long> idsCrachaImpressoCasais(SegueMe sm) { return idsCracha(FROM_CASAL, sm, false); }
	public List<Long> idsCrachaPendentePadres(SegueMe sm) { return idsCracha(FROM_PADRE, sm, true); }
	public List<Long> idsCrachaImpressoPadres(SegueMe sm) { return idsCracha(FROM_PADRE, sm, false); }

	public List<Long> idsCrachaPendenteSeguimistas(SegueMe sm) { return idsCrachaSeguimistas(sm, true); }
	public List<Long> idsCrachaImpressoSeguimistas(SegueMe sm) { return idsCrachaSeguimistas(sm, false); }

	private List<Long> idsCrachaSeguimistas(SegueMe sm, boolean cracha) {
		return manager.createQuery("SELECT e.id FROM Evento e JOIN e.seguidor s JOIN e.inscricao i "
				+ "WHERE e.segueMe = :sm AND i.statusInscricao = :aprovado AND e.cracha = :cracha", Long.class)
				.setParameter("sm", sm).setParameter("aprovado", StatusInscricao.APROVADO).setParameter("cracha", cracha)
				.getResultList();
	}

	public List<Long> idsCrachaPendenteConvidados(SegueMe sm) { return idsCrachaConvidados(sm, true); }
	public List<Long> idsCrachaImpressoConvidados(SegueMe sm) { return idsCrachaConvidados(sm, false); }

	private List<Long> idsCrachaConvidados(SegueMe sm, boolean cracha) {
		return manager.createQuery("SELECT e.id FROM Evento e JOIN e.palestranteConvidado pc JOIN e.palestra pl "
				+ "WHERE e.segueMe = :sm AND e.equipe IS NULL AND e.funcao IS NULL AND e.cracha = :cracha", Long.class)
				.setParameter("sm", sm).setParameter("cracha", cracha).getResultList();
	}

	/** Crachás de seguidor PENDENTES de UMA equipe (para marcar impresso por equipe). */
	public List<Long> idsCrachaPendenteSeguidoresEquipe(SegueMe sm, Integer equipeId) {
		return manager.createQuery("SELECT e.id FROM Evento e JOIN e.seguidor s JOIN e.equipe eq JOIN e.funcao f "
				+ "WHERE e.segueMe = :sm AND eq.id = :equipeId AND e.cracha = true", Long.class)
				.setParameter("sm", sm).setParameter("equipeId", equipeId).getResultList();
	}

	/** Crachás de seguidor JÁ IMPRESSOS (cracha=false) de UMA equipe (para desmarcar/reimprimir). */
	public List<Long> idsCrachaImpressoSeguidoresEquipe(SegueMe sm, Integer equipeId) {
		return manager.createQuery("SELECT e.id FROM Evento e JOIN e.seguidor s JOIN e.equipe eq JOIN e.funcao f "
				+ "WHERE e.segueMe = :sm AND eq.id = :equipeId AND e.cracha = false", Long.class)
				.setParameter("sm", sm).setParameter("equipeId", equipeId).getResultList();
	}

	/**
	 * Crachás de seguimista de UM círculo por estado: {@code pendente=true} devolve
	 * os pendentes (cracha=true), {@code pendente=false} os já impressos (cracha=false).
	 */
	public List<Long> idsCrachaSeguimistasCirculo(SegueMe sm, Integer circuloId, boolean pendente) {
		return manager.createQuery("SELECT e.id FROM Evento e JOIN e.seguidor s JOIN e.inscricao i JOIN e.circulo c "
				+ "WHERE e.segueMe = :sm AND i.statusInscricao = :aprovado AND c.id = :circuloId AND e.cracha = :cracha",
				Long.class).setParameter("sm", sm).setParameter("aprovado", StatusInscricao.APROVADO)
				.setParameter("circuloId", circuloId).setParameter("cracha", pendente).getResultList();
	}

	/**
	 * Seguidores por equipe no quadrante: linhas [equipeId, titulo,
	 * previstos(Equipe.pessoas), alocados, pendentes], ordenado por crachás a
	 * imprimir (desc). Usado no Dashboard da Gráfica.
	 */
	public List<Object[]> resumoSeguidoresPorEquipe(SegueMe sm) {
		return manager.createQuery("SELECT eq.id, eq.titulo, eq.pessoas, COUNT(e), "
				+ "COALESCE(SUM(CASE WHEN e.cracha = true THEN 1 ELSE 0 END), 0) "
				+ "FROM Evento e JOIN e.seguidor s JOIN e.equipe eq JOIN e.funcao f "
				+ "WHERE e.segueMe = :sm GROUP BY eq.id, eq.titulo, eq.pessoas "
				+ "ORDER BY COALESCE(SUM(CASE WHEN e.cracha = true THEN 1 ELSE 0 END), 0) DESC, COUNT(e) DESC",
				Object[].class).setParameter("sm", sm).getResultList();
	}

	/**
	 * Casais de serviço por equipe: linhas [equipeId, titulo, previstos, totalCasais].
	 * Traz titulo/previstos para o Dashboard poder incluir também as equipes que
	 * têm SÓ casais (sem seguidores) na tabela e nos totais. Usado para descontar
	 * os casais (cada casal = 2 pessoas) no "faltam", já que {@code Equipe.pessoas}
	 * planeja seguidores + casais.
	 */
	public List<Object[]> resumoCasaisPorEquipe(SegueMe sm) {
		return manager.createQuery("SELECT eq.id, eq.titulo, eq.pessoas, COUNT(e), "
				+ "COALESCE(SUM(CASE WHEN e.cracha = true THEN 1 ELSE 0 END), 0) "
				+ "FROM Evento e JOIN e.casal c JOIN e.equipe eq JOIN e.funcao f "
				+ "WHERE e.segueMe = :sm GROUP BY eq.id, eq.titulo, eq.pessoas", Object[].class)
				.setParameter("sm", sm).getResultList();
	}

	/** Crachás de casal de UMA equipe por estado ({@code pendente=true} → cracha=true; impresso → false). */
	public List<Long> idsCrachaCasaisEquipe(SegueMe sm, Integer equipeId, boolean pendente) {
		return manager.createQuery("SELECT e.id FROM Evento e JOIN e.casal c JOIN e.equipe eq JOIN e.funcao f "
				+ "WHERE e.segueMe = :sm AND eq.id = :equipeId AND e.cracha = :cracha", Long.class)
				.setParameter("sm", sm).setParameter("equipeId", equipeId).setParameter("cracha", pendente).getResultList();
	}

	/**
	 * [temCapa, temHistoria] do Segue-Me — consulta leve (só checa IS NULL, não
	 * transfere os blobs de PDF). Usado no Dashboard para o aviso do livro e para
	 * refletir o estado ATUAL do banco (o SegueMe do usuário logado é snapshot do
	 * login e pode não ter os arquivos enviados depois).
	 */
	public boolean[] segueMeTemArquivos(Long segueMeId) {
		Object[] r = (Object[]) manager
				.createQuery("SELECT CASE WHEN s.arquivoCapa IS NULL THEN false ELSE true END, "
						+ "CASE WHEN s.arquivoHistoria IS NULL THEN false ELSE true END FROM SegueMe s WHERE s.id = :id")
				.setParameter("id", segueMeId).getSingleResult();
		return new boolean[] { (Boolean) r[0], (Boolean) r[1] };
	}

	/** Bytes do PDF da capa (fresco do banco), ou null. */
	public byte[] arquivoCapa(Long segueMeId) {
		return (byte[]) manager.createQuery("SELECT s.arquivoCapa FROM SegueMe s WHERE s.id = :id")
				.setParameter("id", segueMeId).getSingleResult();
	}

	/** Bytes do PDF da história (fresco do banco), ou null. */
	public byte[] arquivoHistoria(Long segueMeId) {
		return (byte[]) manager.createQuery("SELECT s.arquivoHistoria FROM SegueMe s WHERE s.id = :id")
				.setParameter("id", segueMeId).getSingleResult();
	}

	/** Inscritos por situação: linhas [StatusInscricao, total] do Segue-Me. */
	public List<Object[]> resumoInscritosPorStatus(SegueMe sm) {
		return manager.createQuery("SELECT i.statusInscricao, COUNT(i) FROM Evento e JOIN e.inscricao i "
				+ "WHERE e.segueMe = :sm GROUP BY i.statusInscricao", Object[].class).setParameter("sm", sm)
				.getResultList();
	}

	/**
	 * Seguimistas (inscrição aprovada) por círculo: linhas [circuloId, nome,
	 * corCirculo(CorCirculo), total, pendentes]. Agrupa por {@code Evento.circulo}
	 * — a MESMA ligação usada pelos relatórios Jasper (relacaoSeguimistaCirculo,
	 * quadranteSeguimista) — para que os números batam com o impresso. Ordenado
	 * por crachás a imprimir (desc). Usado no Dashboard da Gráfica.
	 */
	public List<Object[]> resumoSeguimistasPorCirculo(SegueMe sm) {
		return manager.createQuery("SELECT c.id, c.nome, c.corCirculo, COUNT(e), "
				+ "COALESCE(SUM(CASE WHEN e.cracha = true THEN 1 ELSE 0 END), 0) "
				+ "FROM Evento e JOIN e.seguidor s JOIN e.inscricao i JOIN e.circulo c "
				+ "WHERE e.segueMe = :sm AND i.statusInscricao = :aprovado "
				+ "GROUP BY c.id, c.nome, c.corCirculo "
				+ "ORDER BY COALESCE(SUM(CASE WHEN e.cracha = true THEN 1 ELSE 0 END), 0) DESC, c.corCirculo",
				Object[].class).setParameter("sm", sm).setParameter("aprovado", StatusInscricao.APROVADO)
				.getResultList();
	}

	/**
	 * Seguimistas ({@code seguidor} SEM função — é assim que se identifica um
	 * seguimista) com inscrição APROVADA que ainda NÃO estão em nenhum círculo
	 * ({@code e.circulo IS NULL}). Devolve [nome, apelido] para o dashboard mostrar
	 * QUEM precisa ser colocado num círculo. Recusados/sem-inscrição ficam de fora.
	 */
	public List<Object[]> seguimistasSemCirculo(SegueMe sm) {
		return manager.createQuery("SELECT s.nome, s.apelido FROM Evento e JOIN e.seguidor s JOIN e.inscricao i "
				+ "WHERE e.segueMe = :sm AND e.funcao IS NULL AND e.circulo IS NULL AND i.statusInscricao = :aprovado "
				+ "ORDER BY s.nome", Object[].class)
				.setParameter("sm", sm).setParameter("aprovado", StatusInscricao.APROVADO).getResultList();
	}

	/**
	 * Seguimistas com data de nascimento preenchida: linhas [nome, dataNascimento,
	 * corCirculo(CorCirculo), circuloNome]. O filtro por mês/dia é feito no bean
	 * (volume pequeno, evita função de data específica do banco). "Seguimista" =
	 * Evento com inscrição aprovada; nome/nascimento vêm do {@code Seguidor}.
	 */
	public List<Object[]> aniversariantesSeguimistas(SegueMe sm) {
		return manager.createQuery("SELECT s.nome, s.apelido, s.dataNascimento, c.corCirculo, c.nome "
				+ "FROM Evento e JOIN e.seguidor s JOIN e.inscricao i LEFT JOIN e.circulo c "
				+ "WHERE e.segueMe = :sm AND i.statusInscricao = :aprovado AND s.dataNascimento IS NOT NULL",
				Object[].class).setParameter("sm", sm).setParameter("aprovado", StatusInscricao.APROVADO)
				.getResultList();
	}

	/**
	 * Seguidores de serviço (equipe + função) com data de nascimento preenchida:
	 * linhas [nome, apelido, dataNascimento, equipeTitulo]. Filtro por mês/dia no
	 * bean.
	 */
	public List<Object[]> aniversariantesSeguidoresServico(SegueMe sm) {
		return manager.createQuery("SELECT s.nome, s.apelido, s.dataNascimento, eq.titulo "
				+ "FROM Evento e JOIN e.seguidor s JOIN e.equipe eq JOIN e.funcao f "
				+ "WHERE e.segueMe = :sm AND s.dataNascimento IS NOT NULL", Object[].class)
				.setParameter("sm", sm).getResultList();
	}

	/**
	 * Casais de serviço (equipe + função): linhas [nomeEle, nomeEla,
	 * dataNascimentoEle, dataNascimentoEla, dataCasamento, equipeTitulo]. Cada
	 * casal pode render até 3 aniversários no mês (nascimento dele, dela e
	 * casamento) — o filtro por mês/dia e o cálculo de anos de casados são feitos
	 * no bean.
	 */
	public List<Object[]> aniversariantesCasais(SegueMe sm) {
		return manager.createQuery("SELECT c.nomeEle, c.nomeEla, c.dataNascimentoEle, c.dataNascimentoEla, "
				+ "c.dataCasamento, eq.titulo "
				+ "FROM Evento e JOIN e.casal c JOIN e.equipe eq JOIN e.funcao f "
				+ "WHERE e.segueMe = :sm", Object[].class).setParameter("sm", sm).getResultList();
	}

	public List<Evento> filtradosInscricao(EventoFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		// Projeção leve da listagem de inscritos: NÃO seleciona as fotos (byte[]) de
		// seguidor/casal/padre/convidado — carregar as imagens de todos os inscritos
		// estourava a memória (OOM). Só dados textuais exibidos na tabela. Mantém os
		// INNER joins (segueMe, inscricao, usuario, numeroRomano) para preservar o
		// mesmo conjunto de resultados da consulta original.
		Join<Object, Object> seguidorJoin = root.join("seguidor", JoinType.LEFT);
		Join<Object, Object> sexoJoin = seguidorJoin.join("sexo", JoinType.LEFT);
		Join<Object, Object> segueMeJoin = root.join("segueMe", JoinType.INNER);
		Join<Object, Object> numeroRomanoJoin = segueMeJoin.join("numeroRomano", JoinType.INNER);
		Join<Object, Object> inscricaoJoin = root.join("inscricao", JoinType.INNER);
		Join<Object, Object> inscricaoParoquiaJoin = inscricaoJoin.join("paroquia", JoinType.LEFT);
		root.join("usuario", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (filtro.getId() != null) {
			predicates.add(builder.equal(inscricaoJoin.get("numero"), filtro.getId()));
		}

		if (filtro.getStatusInscricao() != null) {
			predicates.add(builder.equal(inscricaoJoin.get("statusInscricao"), filtro.getStatusInscricao()));
		}

		criteriaQuery.multiselect(root.get("id"), root.get("idade"), seguidorJoin.get("id"), seguidorJoin.get("nome"),
				seguidorJoin.get("telefoneUm"), sexoJoin.get("cod"), inscricaoJoin.get("numero"),
				inscricaoJoin.get("dataInsccricao"), inscricaoJoin.get("fichaDoPadre"),
				inscricaoJoin.get("statusInscricao"), inscricaoParoquiaJoin.get("descricao"), segueMeJoin.get("id"),
				segueMeJoin.get("titulo"), numeroRomanoJoin.get("numeroRomano"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(inscricaoJoin.get("numero")), builder.asc(root.get("segueMe")));

		List<Tuple> tuples = manager.createQuery(criteriaQuery).getResultList();
		List<Evento> eventos = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			eventos.add(montarEventoInscricao(t));
		}
		return eventos;
	}

	/**
	 * Monta um {@link Evento} "leve" (sem fotos) a partir da projeção usada em
	 * {@link #filtradosInscricao(EventoFilter)}. Os índices seguem a ordem do
	 * multiselect.
	 */
	private Evento montarEventoInscricao(Tuple t) {
		Evento evento = new Evento();
		evento.setId((Long) t.get(0));
		evento.setIdade((Integer) t.get(1));

		// Só monta o seguidor quando existe (inscrição pode ser uma ficha pendente
		// sem seguidor vinculado — fk_seguidor_id nulo). Assim a coluna Nome fica
		// vazia sem ícone, como na consulta original (seguidor == null).
		Integer seguidorId = (Integer) t.get(2);
		if (seguidorId != null) {
			Seguidor seguidor = new Seguidor();
			seguidor.setId(seguidorId);
			seguidor.setNome((String) t.get(3));
			seguidor.setTelefoneUm((String) t.get(4));
			Integer sexoCod = (Integer) t.get(5);
			if (sexoCod != null) {
				seguidor.setSexo(new Sexo(sexoCod));
			}
			evento.setSeguidor(seguidor);
		}

		Inscricao inscricao = new Inscricao();
		inscricao.setNumero((Integer) t.get(6));
		inscricao.setDataInsccricao((Date) t.get(7));
		inscricao.setFichaDoPadre((Boolean) t.get(8));
		inscricao.setStatusInscricao((StatusInscricao) t.get(9));
		Paroquia inscricaoParoquia = new Paroquia();
		inscricaoParoquia.setDescricao((String) t.get(10));
		inscricao.setParoquia(inscricaoParoquia);
		evento.setInscricao(inscricao);

		SegueMe segueMe = new SegueMe();
		segueMe.setId((Long) t.get(11));
		segueMe.setTitulo((String) t.get(12));
		NumeroRomano numeroRomano = new NumeroRomano();
		numeroRomano.setNumeroRomano((String) t.get(13));
		segueMe.setNumeroRomano(numeroRomano);
		evento.setSegueMe(segueMe);

		return evento;
	}

	/** Predicados compartilhados da listagem de inscritos (página e contagem). */
	private List<Predicate> predicadosInscricao(CriteriaBuilder builder, Root<Evento> root,
			Join<Object, Object> seguidorJoin, Join<Object, Object> segueMeJoin, Join<Object, Object> inscricaoJoin,
			EventoFilter filtro) {
		List<Predicate> predicates = new ArrayList<>();
		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}
		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}
		if (filtro.getId() != null) {
			predicates.add(builder.equal(inscricaoJoin.get("numero"), filtro.getId()));
		}
		if (filtro.getStatusInscricao() != null) {
			predicates.add(builder.equal(inscricaoJoin.get("statusInscricao"), filtro.getStatusInscricao()));
		}
		return predicates;
	}

	/** Conta o total de inscritos do filtro (usado pelo LazyDataModel). */
	public Long contarInscricao(EventoFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Evento> root = criteriaQuery.from(Evento.class);
		Join<Object, Object> seguidorJoin = root.join("seguidor", JoinType.LEFT);
		Join<Object, Object> segueMeJoin = root.join("segueMe", JoinType.INNER);
		segueMeJoin.join("numeroRomano", JoinType.INNER);
		Join<Object, Object> inscricaoJoin = root.join("inscricao", JoinType.INNER);
		root.join("usuario", JoinType.INNER);
		criteriaQuery.select(builder.count(root));
		criteriaQuery.where(predicadosInscricao(builder, root, seguidorJoin, segueMeJoin, inscricaoJoin, filtro)
				.toArray(new Predicate[0]));
		return manager.createQuery(criteriaQuery).getSingleResult();
	}

	/** Página da listagem de inscritos (projeção sem foto) para paginação server-side. */
	public List<Evento> filtradosInscricaoPaginado(EventoFilter filtro, int first, int pageSize, String sortField,
			boolean asc) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		Root<Evento> root = criteriaQuery.from(Evento.class);
		Join<Object, Object> seguidorJoin = root.join("seguidor", JoinType.LEFT);
		Join<Object, Object> sexoJoin = seguidorJoin.join("sexo", JoinType.LEFT);
		Join<Object, Object> segueMeJoin = root.join("segueMe", JoinType.INNER);
		Join<Object, Object> numeroRomanoJoin = segueMeJoin.join("numeroRomano", JoinType.INNER);
		Join<Object, Object> inscricaoJoin = root.join("inscricao", JoinType.INNER);
		Join<Object, Object> inscricaoParoquiaJoin = inscricaoJoin.join("paroquia", JoinType.LEFT);
		root.join("usuario", JoinType.INNER);
		criteriaQuery.multiselect(root.get("id"), root.get("idade"), seguidorJoin.get("id"), seguidorJoin.get("nome"),
				seguidorJoin.get("telefoneUm"), sexoJoin.get("cod"), inscricaoJoin.get("numero"),
				inscricaoJoin.get("dataInsccricao"), inscricaoJoin.get("fichaDoPadre"),
				inscricaoJoin.get("statusInscricao"), inscricaoParoquiaJoin.get("descricao"), segueMeJoin.get("id"),
				segueMeJoin.get("titulo"), numeroRomanoJoin.get("numeroRomano"));
		criteriaQuery.where(predicadosInscricao(builder, root, seguidorJoin, segueMeJoin, inscricaoJoin, filtro)
				.toArray(new Predicate[0]));
		criteriaQuery.orderBy(ordenacaoInscricao(builder, root, seguidorJoin, inscricaoJoin, inscricaoParoquiaJoin,
				numeroRomanoJoin, sortField, asc));
		List<Tuple> tuples = manager.createQuery(criteriaQuery).setFirstResult(first).setMaxResults(pageSize)
				.getResultList();
		List<Evento> eventos = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			eventos.add(montarEventoInscricao(t));
		}
		return eventos;
	}

	/** Mapeia a coluna de ordenação do p:dataTable para a expressão (default: nº da inscrição). */
	private Order ordenacaoInscricao(CriteriaBuilder builder, Root<Evento> root, Join<Object, Object> seguidorJoin,
			Join<Object, Object> inscricaoJoin, Join<Object, Object> inscricaoParoquiaJoin,
			Join<Object, Object> numeroRomanoJoin, String sortField, boolean asc) {
		Expression<?> expr;
		switch (sortField == null ? "" : sortField) {
		case "id":
			expr = root.get("id");
			break;
		case "seguidor.nome":
			expr = seguidorJoin.get("nome");
			break;
		case "seguidor.telefoneUm":
			expr = seguidorJoin.get("telefoneUm");
			break;
		case "inscricao.statusInscricao.nome":
			expr = inscricaoJoin.get("statusInscricao");
			break;
		case "inscricao.paroquia.descricao":
			expr = inscricaoParoquiaJoin.get("descricao");
			break;
		case "segueMe.numeroRomano.numeroRomano":
			expr = numeroRomanoJoin.get("numeroRomano");
			break;
		default:
			expr = inscricaoJoin.get("numero");
			break;
		}
		return asc ? builder.asc(expr) : builder.desc(expr);
	}

	public List<Evento> filtradosSeguidor(EventoSeguidorFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		Join<Object, Object> seguidorJoin = root.join("seguidor", JoinType.INNER);
		Join<Object, Object> sexoJoin = seguidorJoin.join("sexo", JoinType.LEFT);
		Join<Object, Object> equipeJoin = root.join("equipe", JoinType.INNER);
		Join<Object, Object> funcaoJoin = root.join("funcao", JoinType.INNER);
		Join<Object, Object> segueMeJoin = root.join("segueMe", JoinType.INNER);
		Join<Object, Object> paroquiaJoin = segueMeJoin.join("paroquia", JoinType.LEFT);
		Join<Object, Object> numeroRomanoJoin = segueMeJoin.join("numeroRomano", JoinType.LEFT);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}
		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}
		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(seguidorJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}
		if (filtro.getEquipe() != null) {
			predicates.add(builder.equal(root.get("equipe"), filtro.getEquipe()));
		}
		if (filtro.getFuncao() != null) {
			predicates.add(builder.equal(root.get("funcao"), filtro.getFuncao()));
		}
		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		// Projeção leve: NÃO seleciona a foto (byte[]) do seguidor. A listagem só
		// exibe dados textuais; carregar as imagens de todos os seguidores estourava
		// a memória (OOM). O INNER join em equipe/funcao substitui o isNotNull.
		criteriaQuery.multiselect(root.get("id"), root.get("idade"), root.get("cracha"), root.get("statusConvite"),
				seguidorJoin.get("id"), seguidorJoin.get("nome"), seguidorJoin.get("telefoneUm"), sexoJoin.get("cod"),
				equipeJoin.get("id"), equipeJoin.get("titulo"), equipeJoin.get("ordem"), funcaoJoin.get("descricao"),
				funcaoJoin.get("ordem"), segueMeJoin.get("id"), paroquiaJoin.get("descricao"),
				numeroRomanoJoin.get("numeroRomano"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("id")), builder.asc(equipeJoin.get("ordem")),
				builder.asc(funcaoJoin.get("ordem")), builder.asc(seguidorJoin.get("nome")));

		List<Tuple> tuples = manager.createQuery(criteriaQuery).getResultList();
		List<Evento> eventos = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			eventos.add(montarEventoSeguidor(t));
		}
		return eventos;
	}

	/**
	 * Monta um {@link Evento} "leve" (sem foto) a partir da projeção usada em
	 * {@link #filtradosSeguidor(EventoSeguidorFilter)}. Os índices seguem a ordem
	 * do multiselect.
	 */
	private Evento montarEventoSeguidor(Tuple t) {
		Evento evento = new Evento();
		evento.setId((Long) t.get(0));
		evento.setIdade((Integer) t.get(1));
		evento.setCracha((Boolean) t.get(2));
		evento.setStatusConvite((StatusConvite) t.get(3));

		Seguidor seguidor = new Seguidor();
		seguidor.setId((Integer) t.get(4));
		seguidor.setNome((String) t.get(5));
		seguidor.setTelefoneUm((String) t.get(6));
		Integer sexoCod = (Integer) t.get(7);
		if (sexoCod != null) {
			Sexo sexo = new Sexo();
			sexo.setCod(sexoCod);
			seguidor.setSexo(sexo);
		}
		evento.setSeguidor(seguidor);

		Equipe equipe = new Equipe();
		equipe.setId((Integer) t.get(8));
		equipe.setTitulo((String) t.get(9));
		equipe.setOrdem((Integer) t.get(10));
		evento.setEquipe(equipe);

		Funcao funcao = new Funcao();
		funcao.setDescricao((String) t.get(11));
		funcao.setOrdem((Integer) t.get(12));
		evento.setFuncao(funcao);

		SegueMe segueMe = new SegueMe();
		segueMe.setId((Long) t.get(13));
		Paroquia paroquia = new Paroquia();
		paroquia.setDescricao((String) t.get(14));
		segueMe.setParoquia(paroquia);
		NumeroRomano numeroRomano = new NumeroRomano();
		numeroRomano.setNumeroRomano((String) t.get(15));
		segueMe.setNumeroRomano(numeroRomano);
		evento.setSegueMe(segueMe);

		return evento;
	}

	public List<Evento> filtradosCasal(EventoCasalFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		Join<Object, Object> casalJoin = root.join("casal", JoinType.INNER);
		Join<Object, Object> equipeJoin = root.join("equipe", JoinType.INNER);
		Join<Object, Object> funcaoJoin = root.join("funcao", JoinType.INNER);
		Join<Object, Object> segueMeJoin = root.join("segueMe", JoinType.INNER);
		Join<Object, Object> paroquiaJoin = segueMeJoin.join("paroquia", JoinType.LEFT);
		Join<Object, Object> numeroRomanoJoin = segueMeJoin.join("numeroRomano", JoinType.LEFT);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}
		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (StringUtils.isNotBlank(filtro.getNomeEle())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("nomeEle")),
					"%" + filtro.getNomeEle().toLowerCase() + "%"));
		}
		if (StringUtils.isNotBlank(filtro.getApelidoEle())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("apelidoEle")),
					"%" + filtro.getApelidoEle().toLowerCase() + "%"));
		}
		if (StringUtils.isNotBlank(filtro.getNomeEla())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("nomeEla")),
					"%" + filtro.getNomeEla().toLowerCase() + "%"));
		}
		if (StringUtils.isNotBlank(filtro.getApelidoEla())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("apelidoEla")),
					"%" + filtro.getApelidoEla().toLowerCase() + "%"));
		}
		if (filtro.getEquipe() != null) {
			predicates.add(builder.equal(root.get("equipe"), filtro.getEquipe()));
		}
		if (filtro.getFuncao() != null) {
			predicates.add(builder.equal(root.get("funcao"), filtro.getFuncao()));
		}
		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		// Projeção leve: NÃO seleciona as fotos (byte[]) do casal — evita OOM.
		criteriaQuery.multiselect(root.get("id"), root.get("cracha"), root.get("statusConvite"), casalJoin.get("id"),
				casalJoin.get("apelidoEle"), casalJoin.get("apelidoEla"), casalJoin.get("telefoneEleUm"),
				casalJoin.get("telefoneElaUm"), equipeJoin.get("id"), equipeJoin.get("titulo"), equipeJoin.get("ordem"),
				funcaoJoin.get("descricao"), segueMeJoin.get("id"), paroquiaJoin.get("descricao"),
				numeroRomanoJoin.get("numeroRomano"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("id")), builder.asc(equipeJoin.get("ordem")),
				builder.asc(funcaoJoin.get("ordem")), builder.asc(casalJoin.get("nomeEle")));

		List<Tuple> tuples = manager.createQuery(criteriaQuery).getResultList();
		List<Evento> eventos = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			eventos.add(montarEventoCasal(t));
		}
		return eventos;
	}

	/** Monta um Evento "leve" (sem fotos) a partir da projeção de filtradosCasal. */
	private Evento montarEventoCasal(Tuple t) {
		Evento evento = new Evento();
		evento.setId((Long) t.get(0));
		evento.setCracha((Boolean) t.get(1));
		evento.setStatusConvite((StatusConvite) t.get(2));

		Casal casal = new Casal();
		casal.setId((Integer) t.get(3));
		casal.setApelidoEle((String) t.get(4));
		casal.setApelidoEla((String) t.get(5));
		casal.setTelefoneEleUm((String) t.get(6));
		casal.setTelefoneElaUm((String) t.get(7));
		evento.setCasal(casal);

		Equipe equipe = new Equipe();
		equipe.setId((Integer) t.get(8));
		equipe.setTitulo((String) t.get(9));
		equipe.setOrdem((Integer) t.get(10));
		evento.setEquipe(equipe);

		Funcao funcao = new Funcao();
		funcao.setDescricao((String) t.get(11));
		evento.setFuncao(funcao);

		SegueMe segueMe = new SegueMe();
		segueMe.setId((Long) t.get(12));
		Paroquia paroquia = new Paroquia();
		paroquia.setDescricao((String) t.get(13));
		segueMe.setParoquia(paroquia);
		NumeroRomano numeroRomano = new NumeroRomano();
		numeroRomano.setNumeroRomano((String) t.get(14));
		segueMe.setNumeroRomano(numeroRomano);
		evento.setSegueMe(segueMe);

		return evento;
	}

	public List<Evento> filtradosSeguimista(EventoSeguimistaFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		Join<Object, Object> seguidorJoin = root.join("seguidor", JoinType.INNER);
		Join<Object, Object> sexoJoin = seguidorJoin.join("sexo", JoinType.LEFT);
		Join<Object, Object> circuloJoin = seguidorJoin.join("circulo", JoinType.LEFT);
		Join<Object, Object> segueMeJoin = root.join("segueMe", JoinType.INNER);
		Join<Object, Object> paroquiaJoin = segueMeJoin.join("paroquia", JoinType.LEFT);
		Join<Object, Object> numeroRomanoJoin = segueMeJoin.join("numeroRomano", JoinType.LEFT);
		Join<Object, Object> inscricaoJoin = root.join("inscricao", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}
		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}
		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(seguidorJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}
		if (filtro.getCirculo() != null) {
			predicates.add(builder.equal(seguidorJoin.get("circulo"), filtro.getCirculo()));
		}
		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}
		predicates.add(builder.equal(inscricaoJoin.get("statusInscricao"), StatusInscricao.APROVADO));

		// Projeção leve: NÃO seleciona a foto (byte[]) do seguidor — evita OOM.
		criteriaQuery.multiselect(root.get("id"), root.get("idade"), root.get("cracha"), seguidorJoin.get("id"),
				seguidorJoin.get("nome"), seguidorJoin.get("telefoneUm"), seguidorJoin.get("dataNascimento"),
				sexoJoin.get("cod"), circuloJoin.get("id"), circuloJoin.get("corCirculo"), segueMeJoin.get("id"),
				paroquiaJoin.get("descricao"), numeroRomanoJoin.get("numeroRomano"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("id")), builder.asc(circuloJoin.get("corCirculo")),
				builder.asc(seguidorJoin.get("nome")));

		List<Tuple> tuples = manager.createQuery(criteriaQuery).getResultList();
		List<Evento> eventos = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			eventos.add(montarEventoSeguimista(t));
		}
		return eventos;
	}

	/** Monta um Evento "leve" (sem foto) a partir da projeção de filtradosSeguimista. */
	private Evento montarEventoSeguimista(Tuple t) {
		Evento evento = new Evento();
		evento.setId((Long) t.get(0));
		evento.setIdade((Integer) t.get(1));
		evento.setCracha((Boolean) t.get(2));

		Seguidor seguidor = new Seguidor();
		seguidor.setId((Integer) t.get(3));
		seguidor.setNome((String) t.get(4));
		seguidor.setTelefoneUm((String) t.get(5));
		seguidor.setDataNascimento((Date) t.get(6));
		Integer sexoCod = (Integer) t.get(7);
		if (sexoCod != null) {
			Sexo sexo = new Sexo();
			sexo.setCod(sexoCod);
			seguidor.setSexo(sexo);
		}
		Integer circuloId = (Integer) t.get(8);
		if (circuloId != null) {
			Circulo circulo = new Circulo();
			circulo.setId(circuloId);
			circulo.setCorCirculo((CorCirculo) t.get(9));
			seguidor.setCirculo(circulo);
		}
		evento.setSeguidor(seguidor);

		SegueMe segueMe = new SegueMe();
		segueMe.setId((Long) t.get(10));
		Paroquia paroquia = new Paroquia();
		paroquia.setDescricao((String) t.get(11));
		segueMe.setParoquia(paroquia);
		NumeroRomano numeroRomano = new NumeroRomano();
		numeroRomano.setNumeroRomano((String) t.get(12));
		segueMe.setNumeroRomano(numeroRomano);
		evento.setSegueMe(segueMe);

		return evento;
	}

	public List<Evento> filtradosPadre(EventoPadreFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		Join<Object, Object> padreJoin = root.join("padre", JoinType.INNER);
		Join<Object, Object> equipeJoin = root.join("equipe", JoinType.INNER);
		Join<Object, Object> funcaoJoin = root.join("funcao", JoinType.INNER);
		Join<Object, Object> segueMeJoin = root.join("segueMe", JoinType.INNER);
		Join<Object, Object> paroquiaJoin = segueMeJoin.join("paroquia", JoinType.LEFT);
		Join<Object, Object> numeroRomanoJoin = segueMeJoin.join("numeroRomano", JoinType.LEFT);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}
		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(padreJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}
		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(padreJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}
		if (filtro.getEquipe() != null) {
			predicates.add(builder.equal(root.get("equipe"), filtro.getEquipe()));
		}
		if (filtro.getFuncao() != null) {
			predicates.add(builder.equal(root.get("funcao"), filtro.getFuncao()));
		}
		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		// Projeção leve: NÃO seleciona a foto (byte[]) do padre. INNER em equipe/funcao
		// substitui o isNotNull.
		criteriaQuery.multiselect(root.get("id"), root.get("cracha"), root.get("statusConvite"), padreJoin.get("id"),
				padreJoin.get("nome"), padreJoin.get("apelido"), padreJoin.get("telefoneUm"), equipeJoin.get("id"),
				equipeJoin.get("titulo"), equipeJoin.get("ordem"), funcaoJoin.get("descricao"), segueMeJoin.get("id"),
				paroquiaJoin.get("descricao"), numeroRomanoJoin.get("numeroRomano"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("id")), builder.asc(equipeJoin.get("ordem")),
				builder.asc(funcaoJoin.get("ordem")), builder.asc(padreJoin.get("nome")));

		List<Tuple> tuples = manager.createQuery(criteriaQuery).getResultList();
		List<Evento> eventos = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			eventos.add(montarEventoPadre(t));
		}
		return eventos;
	}

	/** Monta um Evento "leve" (sem foto) a partir da projeção de filtradosPadre. */
	private Evento montarEventoPadre(Tuple t) {
		Evento evento = new Evento();
		evento.setId((Long) t.get(0));
		evento.setCracha((Boolean) t.get(1));
		evento.setStatusConvite((StatusConvite) t.get(2));

		Padre padre = new Padre();
		padre.setId((Integer) t.get(3));
		padre.setNome((String) t.get(4));
		padre.setApelido((String) t.get(5));
		padre.setTelefoneUm((String) t.get(6));
		evento.setPadre(padre);

		Equipe equipe = new Equipe();
		equipe.setId((Integer) t.get(7));
		equipe.setTitulo((String) t.get(8));
		equipe.setOrdem((Integer) t.get(9));
		evento.setEquipe(equipe);

		Funcao funcao = new Funcao();
		funcao.setDescricao((String) t.get(10));
		evento.setFuncao(funcao);

		SegueMe segueMe = new SegueMe();
		segueMe.setId((Long) t.get(11));
		Paroquia paroquia = new Paroquia();
		paroquia.setDescricao((String) t.get(12));
		segueMe.setParoquia(paroquia);
		NumeroRomano numeroRomano = new NumeroRomano();
		numeroRomano.setNumeroRomano((String) t.get(13));
		segueMe.setNumeroRomano(numeroRomano);
		evento.setSegueMe(segueMe);

		return evento;
	}

	public List<Evento> filtradosPalestraCasal(PalestraCasalFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.INNER);
		From<?, ?> enderecoJoin = (From<?, ?>) casalJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> eccJoin = (From<?, ?>) casalJoin.fetch("ecc", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.INNER);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNomeEle())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("nomeEle")),
					"%" + filtro.getNomeEle().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelidoEle())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("apelidoEle")),
					"%" + filtro.getApelidoEle().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getNomeEla())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("nomeEla")),
					"%" + filtro.getNomeEla().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelidoEla())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("apelidoEla")),
					"%" + filtro.getApelidoEla().toLowerCase() + "%"));
		}

		if (filtro.getPalestra() != null) {
			predicates.add(builder.equal(root.get("palestra"), filtro.getPalestra()));
		}

		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNull(root.get("funcao")));
		predicates.add(builder.isNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(palestraJoin.get("ordem")),
				builder.asc(casalJoin.get("nomeEle")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosPalestraSeguidor(PalestraSeguidorFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.INNER);
		From<?, ?> enderecoJoin = (From<?, ?>) seguidorJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) seguidorJoin.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeSeguidorJoin = (From<?, ?>) seguidorJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) seguidorJoin.fetch("circulo", JoinType.LEFT);
		From<?, ?> sexoJoin = (From<?, ?>) seguidorJoin.fetch("sexo", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.INNER);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(seguidorJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}

		if (filtro.getPalestra() != null) {
			predicates.add(builder.equal(root.get("palestra"), filtro.getPalestra()));
		}

		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNull(root.get("funcao")));
		predicates.add(builder.isNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(palestraJoin.get("ordem")),
				builder.asc(seguidorJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosPalestraPadre(PalestraPadreFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> enderecoJoin = (From<?, ?>) seguidorJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) seguidorJoin.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeSeguidorJoin = (From<?, ?>) seguidorJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) seguidorJoin.fetch("circulo", JoinType.LEFT);
		From<?, ?> sexoJoin = (From<?, ?>) seguidorJoin.fetch("sexo", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.INNER);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.INNER);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(padreJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(padreJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}

		if (filtro.getPalestra() != null) {
			predicates.add(builder.equal(root.get("palestra"), filtro.getPalestra()));
		}

		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNull(root.get("funcao")));
		predicates.add(builder.isNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(palestraJoin.get("ordem")),
				builder.asc(padreJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}
	
	

	public List<Evento> filtradosPalestraConvidado(PalestraConvidadoFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		Join<Object, Object> convidadoJoin = root.join("palestranteConvidado", JoinType.INNER);
		Join<Object, Object> sexoJoin = convidadoJoin.join("sexo", JoinType.LEFT);
		Join<Object, Object> palestraJoin = root.join("palestra", JoinType.INNER);
		Join<Object, Object> segueMeJoin = root.join("segueMe", JoinType.INNER);
		Join<Object, Object> paroquiaJoin = segueMeJoin.join("paroquia", JoinType.LEFT);
		Join<Object, Object> numeroRomanoJoin = segueMeJoin.join("numeroRomano", JoinType.LEFT);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}
		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.like(builder.lower(convidadoJoin.get("nome")),
					"%" + filtro.getNome().toLowerCase() + "%"));
		}
		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(convidadoJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}
		if (filtro.getPalestra() != null) {
			predicates.add(builder.equal(root.get("palestra"), filtro.getPalestra()));
		}
		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}
		predicates.add(builder.isNull(root.get("funcao")));
		predicates.add(builder.isNull(root.get("equipe")));

		// Projeção leve: NÃO seleciona a foto (byte[]) do convidado.
		criteriaQuery.multiselect(root.get("id"), root.get("idade"), root.get("cracha"), root.get("statusConvite"),
				convidadoJoin.get("id"), convidadoJoin.get("nome"), convidadoJoin.get("telefoneUm"), sexoJoin.get("cod"),
				palestraJoin.get("id"), palestraJoin.get("nome"), palestraJoin.get("ordem"), segueMeJoin.get("id"),
				paroquiaJoin.get("descricao"), numeroRomanoJoin.get("numeroRomano"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("id")), builder.asc(palestraJoin.get("ordem")),
				builder.asc(convidadoJoin.get("nome")));

		List<Tuple> tuples = manager.createQuery(criteriaQuery).getResultList();
		List<Evento> eventos = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			eventos.add(montarEventoConvidado(t));
		}
		return eventos;
	}

	/** Monta um Evento "leve" (sem foto) a partir da projeção de filtradosPalestraConvidado. */
	private Evento montarEventoConvidado(Tuple t) {
		Evento evento = new Evento();
		evento.setId((Long) t.get(0));
		evento.setIdade((Integer) t.get(1));
		evento.setCracha((Boolean) t.get(2));
		evento.setStatusConvite((StatusConvite) t.get(3));

		PalestranteConvidado convidado = new PalestranteConvidado();
		convidado.setId((Integer) t.get(4));
		convidado.setNome((String) t.get(5));
		convidado.setTelefoneUm((String) t.get(6));
		Integer sexoCod = (Integer) t.get(7);
		if (sexoCod != null) {
			Sexo sexo = new Sexo();
			sexo.setCod(sexoCod);
			convidado.setSexo(sexo);
		}
		evento.setPalestranteConvidado(convidado);

		Palestra palestra = new Palestra();
		palestra.setId((Long) t.get(8));
		palestra.setNome((String) t.get(9));
		palestra.setOrdem((Integer) t.get(10));
		evento.setPalestra(palestra);

		SegueMe segueMe = new SegueMe();
		segueMe.setId((Long) t.get(11));
		Paroquia paroquia = new Paroquia();
		paroquia.setDescricao((String) t.get(12));
		segueMe.setParoquia(paroquia);
		NumeroRomano numeroRomano = new NumeroRomano();
		numeroRomano.setNumeroRomano((String) t.get(13));
		segueMe.setNumeroRomano(numeroRomano);
		evento.setSegueMe(segueMe);

		return evento;
	}

	public List<Evento> filtradosHistorico(Seguidor seguidor) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.INNER);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);

		predicates.add(builder.equal(root.get("seguidor"), seguidor));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("numeroRomano")), builder.asc(segueMeJoin.get("dtInicio")),
				builder.asc(equipeJoin.get("ordem")), builder.asc(funcaoJoin.get("ordem")),
				builder.asc(seguidorJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosHistoricoCasal(Casal casal) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.INNER);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);

		predicates.add(builder.equal(root.get("casal"), casal));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("numeroRomano")), builder.asc(segueMeJoin.get("dtInicio")),
				builder.asc(equipeJoin.get("ordem")), builder.asc(funcaoJoin.get("ordem")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosHistoricoPadre(Padre padre) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.INNER);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);

		predicates.add(builder.equal(root.get("padre"), padre));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("numeroRomano")), builder.asc(segueMeJoin.get("dtInicio")),
				builder.asc(equipeJoin.get("ordem")), builder.asc(funcaoJoin.get("ordem")),
				builder.asc(padreJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}
	
	
	public List<Evento> filtradosHistoricoConvidado(PalestranteConvidado palestranteConvidado) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.INNER);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);

		predicates.add(builder.equal(root.get("palestranteConvidado"), palestranteConvidado));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("numeroRomano")), builder.asc(segueMeJoin.get("dtInicio")),
				builder.asc(equipeJoin.get("ordem")), builder.asc(funcaoJoin.get("ordem")),
				builder.asc(seguidorJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	/**
	 * Procura seguidor por SegueMe, nome e Dt Nascimento
	 * 
	 * @param email
	 * @return
	 */
	public Evento findByNomeSegueMeDtNascimento(String nome, SegueMe segueMe, Date dtnascimento) {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s "
							+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "LEFT JOIN e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE e.segueMe = :segue "
							+ "AND s.nome like :nome " + " AND s.dataNascimento = :dtnascimento", Evento.class)
					.setParameter("dtnascimento", dtnascimento).setParameter("segue", segueMe)
					.setParameter("nome", nome).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByIdInscrito(Integer id, Paroquia paroquia) throws NegocioException {
		try {
			return manager.createQuery(
					"SELECT distinct(e) FROM Evento e " 
							+ "LEFT JOIN e.seguidor s " 
							+ "JOIN FETCH e.segueMe sm " 
							+ "LEFT JOIN e.funcao f "
							+ "JOIN FETCH e.inscricao i " 
							+ "WHERE i.numero = :id AND sm.paroquia = :paroquia ",
					Evento.class).setParameter("id", id).setParameter("paroquia", paroquia).getSingleResult();
		} catch (NoResultException nr) {
			throw new NegocioException("Ficha não pode ser encontrada.");
		}
	}

	public Evento findBySeguidorEventoEquipe(Seguidor seguidor, SegueMe segueMe, Equipe equipe)
			throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s "
							+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "JOIN FETCH e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE e.seguidor = :seguidor "
							+ "AND e.segueMe = :segueme " + "AND e.equipe = :equipe ", Evento.class)
					.setParameter("seguidor", seguidor).setParameter("segueme", segueMe).setParameter("equipe", equipe)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByCasalEventoEquipe(Casal casal, SegueMe segueMe, Equipe equipe) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "JOIN FETCH e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "JOIN FETCH e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE e.casal = :casal "
							+ "AND e.segueMe = :segueme " + "AND e.equipe = :equipe ", Evento.class)
					.setParameter("casal", casal).setParameter("segueme", segueMe).setParameter("equipe", equipe)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByPadreEventoEquipe(Padre padre, SegueMe segueMe, Equipe equipe) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "LEFT JOIN e.casal c " + "JOIN FETCH e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "JOIN FETCH e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE e.padre = :padre "
							+ "AND e.segueMe = :segueme " + "AND e.equipe = :equipe ", Evento.class)
					.setParameter("padre", padre).setParameter("segueme", segueMe).setParameter("equipe", equipe)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByCasalEventoPalestra(Casal casal, SegueMe segueMe, Palestra palestra) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "JOIN FETCH e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "JOIN FETCH e.palestra p " + "WHERE e.casal = :casal "
							+ "AND e.segueMe = :segueme " + "AND e.palestra = :palestra ", Evento.class)
					.setParameter("casal", casal).setParameter("segueme", segueMe).setParameter("palestra", palestra)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findBySeguidorEventoPalestra(Seguidor seguidor, SegueMe segueMe, Palestra palestra)
			throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s "
							+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "JOIN FETCH e.palestra p " + "WHERE e.seguidor = :seguidor "
							+ "AND e.segueMe = :segueme " + "AND e.palestra = :palestra ", Evento.class)
					.setParameter("seguidor", seguidor).setParameter("segueme", segueMe)
					.setParameter("palestra", palestra).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByPadreEventoPalestra(Padre padre, SegueMe segueMe, Palestra palestra) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "LEFT JOIN e.casal c " + "JOIN FETCH e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "JOIN FETCH e.palestra p " + "WHERE e.padre = :padre "
							+ "AND e.segueMe = :segueme " + "AND e.palestra = :palestra ", Evento.class)
					.setParameter("padre", padre).setParameter("segueme", segueMe).setParameter("palestra", palestra)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}
	
	
	public Evento findByConvidadoEventoPalestra(PalestranteConvidado convidado, SegueMe segueMe, Palestra palestra)
			throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "JOIN FETCH e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "JOIN FETCH e.palestra p " + "WHERE e.palestranteConvidado = :palestranteConvidado "
							+ "AND e.segueMe = :segueme " + "AND e.palestra = :palestra ", Evento.class)
					.setParameter("palestranteConvidado", convidado).setParameter("segueme", segueMe)
					.setParameter("palestra", palestra).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Evento> findBySeguidorNome(String nome, SegueMe segueMe) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s "
					+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " + "JOIN FETCH e.equipe eq " + "LEFT JOIN e.funcao f "
					+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p "
					+ "where (s.apelido LIKE :apelido OR s.nome LIKE :nome OR eq.titulo LIKE :apelido) AND e.segueMe = :segueme ",
					Evento.class).setParameter("nome", "%" + nome + "%")
					.setParameter("apelido", "%" + nome.toUpperCase() + "%").setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Evento> findByCasalNome(String nome, SegueMe segueMe) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " 
					+ "LEFT JOIN e.seguidor s "
					+ "JOIN FETCH e.casal c " 
					+ "LEFT JOIN e.padre p " 
					+ "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " 
					+ "JOIN FETCH e.equipe eq " 
					+ "LEFT JOIN e.funcao f "
					+ "LEFT JOIN e.inscricao i " 
					+ "LEFT JOIN e.palestra p "
					+ "WHERE (lower(c.nomeEle) LIKE lower(:nomeEle) "
					+ "	OR lower(c.apelidoEle) LIKE lower(:apelidoEle) "
					+ " OR eq.titulo LIKE :apelido) AND e.segueMe = :segueme ",
					Evento.class)
					.setParameter("nomeEle", "%" + nome + "%")
					.setParameter("apelidoEle", "%" + nome + "%")
					.setParameter("apelido", "%" + nome.toUpperCase() + "%")
					.setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
	
	public List<Evento> findByPadreNome(String nome, SegueMe segueMe) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " 
					+ "LEFT JOIN e.seguidor s "
					+ "LEFT JOIN e.casal c " 
					+ "JOIN FETCH e.padre p " 
					+ "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " 
					+ "JOIN FETCH e.equipe eq " 
					+ "LEFT JOIN e.funcao f "
					+ "LEFT JOIN e.inscricao i " 
					+ "LEFT JOIN e.palestra pp "
					+ "WHERE (lower(p.nome) LIKE lower(:nome) "
					+ "	OR lower(p.apelido) LIKE lower(:nome) "
					+ " OR eq.titulo LIKE :apelido) AND e.segueMe = :segueme ",
					Evento.class)
					.setParameter("nome", "%" + nome + "%")
					.setParameter("apelido", "%" + nome.toUpperCase() + "%")
					.setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
	
	public List<Evento> findBySeguimistaNome(String nome, SegueMe segueMe) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " 
					+ "JOIN FETCH e.seguidor s "
					+ "LEFT JOIN e.casal c " 
					+ "LEFT JOIN e.padre p " 
					+ "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " 
					+ "LEFT JOIN e.equipe eq " 
					+ "LEFT JOIN e.funcao f "
					+ "JOIN FETCH e.inscricao i " 
					+ "LEFT JOIN e.palestra pp "
					+ "WHERE (lower(s.nome) LIKE lower(:nome) "
					+ "	OR lower(s.apelido) LIKE lower(:nome)) "
					+ "AND e.inscricao != null "
					+ "AND e.segueMe = :segueme ",
					Evento.class)
					.setParameter("nome", "%" + nome + "%")
					.setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
	
	public List<Evento> findBySeguimistaNomeSituacao(String nome, SegueMe segueMe, StatusInscricao statusInscricao) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " 
					+ "JOIN FETCH e.seguidor s "
					+ "LEFT JOIN e.casal c " 
					+ "LEFT JOIN e.padre p " 
					+ "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " 
					+ "LEFT JOIN e.equipe eq " 
					+ "LEFT JOIN e.funcao f "
					+ "JOIN FETCH e.inscricao i " 
					+ "LEFT JOIN e.palestra pp "
					+ "WHERE (lower(s.nome) LIKE lower(:nome) "
					+ "	OR lower(s.apelido) LIKE lower(:nome)) "
					+ "AND e.inscricao != null "
					+ "AND i.statusInscricao = :statusInscricao "
					+ "AND e.segueMe = :segueme ",
					Evento.class)
					.setParameter("nome", "%" + nome + "%")
					.setParameter("statusInscricao", statusInscricao)
					.setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
	
	
	public List<Evento> findByNumeroDeInscricao(Paroquia paroquia) {
		try {
			return manager.createQuery("SELECT e FROM Evento e " 
					+ "JOIN FETCH e.segueMe sm " 
					+ "JOIN FETCH e.inscricao i " 
					+ "WHERE sm.paroquia = :paroquia "
					+ "AND e.inscricao != null "
					+ "ORDER BY e.id desc", Evento.class).setParameter("paroquia", paroquia)
					.setMaxResults(1).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}

}