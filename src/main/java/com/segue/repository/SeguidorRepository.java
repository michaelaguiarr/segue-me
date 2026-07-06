
package com.segue.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.segue.filter.SeguidorFilter;
import com.segue.model.Circulo;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.model.Sexo;
import com.segue.model.SituacaoSeguidor;
import com.segue.service.NegocioException;
import com.segue.util.StringExtended;

import org.apache.commons.lang3.StringUtils;

public class SeguidorRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Seguidor findById(Integer id) {
		return manager.find(Seguidor.class, id);
	}

	public Seguidor guardar(Seguidor seguidor) {
		return seguidor = manager.merge(seguidor);
	}

	public void remover(Seguidor seguidor) throws NegocioException {
		try {
			seguidor = findById(seguidor.getId());
			manager.remove(seguidor);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Seguidor não pode ser excluído.");
		}
	}

	/**
	 * Lista todos
	 * 
	 * @return
	 */
	public List<Seguidor> findAll() {
		return manager.createQuery("FROM Seguidor", Seguidor.class).getResultList();
	}

	public List<Seguidor> filtrados(SeguidorFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Seguidor> root = criteriaQuery.from(Seguidor.class);
		// Projeção da listagem: joins (não fetch) somente dos dados exibidos na
		// tabela. Assim o blob "imagem" (foto) NÃO é carregado para todos os
		// seguidores — a foto é buscada sob demanda ao abrir o diálogo. O join
		// INNER de endereço é mantido apenas para preservar o mesmo conjunto de
		// resultados da consulta original.
		root.join("endereco", JoinType.INNER);
		From<?, ?> sexoJoin = (From<?, ?>) root.join("sexo", JoinType.INNER);
		From<?, ?> segueMeJoin = (From<?, ?>) root.join("segueMe", JoinType.INNER);
		segueMeJoin.join("numeroRomano", JoinType.INNER);
		From<?, ?> circuloJoin = (From<?, ?>) root.join("circulo", JoinType.LEFT);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (filtro.getCirculo() != null) {
			predicates.add(builder.equal(root.get("circulo"), filtro.getCirculo()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get("nomeSemAcento")),
							"%" + StringExtended.toASCII(filtro.getNome().toLowerCase()) + "%"),
					builder.like(builder.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"),
					builder.like(builder.lower(root.get("apelido")), "%" + filtro.getNome().toLowerCase() + "%")));
		}

		if (filtro.getSituacao().equals("ATIVO")) {
			predicates.add(builder.equal(root.get("situacaoSeguidor"), SituacaoSeguidor.ATIVO));
		} else {
			predicates.add(builder.equal(root.get("situacaoSeguidor"), SituacaoSeguidor.INATIVO));
		}

		// Oculta registros inativados (duplicados consolidados).
		predicates.add(builder.equal(root.get("ativo"), true));
		// predicates.add(builder.isNotNull(root.get("imagem")));
		// Projeção por tupla (não construct): as associações são selecionadas como
		// entidades hidratadas e remontadas via construtor de projeção do Seguidor,
		// sem trazer o blob "imagem" da listagem.
		criteriaQuery.multiselect(root.get("id"), root.get("nome"), root.get("apelido"), root.get("telefoneUm"),
				root.get("timestamp"), sexoJoin, segueMeJoin, circuloJoin);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("segueMe")));

		return montarProjecao(manager.createQuery(criteriaQuery).getResultList());
	}

	public List<Seguidor> filtradosPublic(SeguidorFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Seguidor> root = criteriaQuery.from(Seguidor.class);
		// Projeção da listagem (ver filtrados): não carrega o blob "imagem".
		root.join("endereco", JoinType.INNER);
		From<?, ?> sexoJoin = (From<?, ?>) root.join("sexo", JoinType.INNER);
		From<?, ?> segueMeJoin = (From<?, ?>) root.join("segueMe", JoinType.INNER);
		segueMeJoin.join("numeroRomano", JoinType.INNER);
		From<?, ?> circuloJoin = (From<?, ?>) root.join("circulo", JoinType.LEFT);

		if (filtro.getNome()!= null && filtro.getNome().length() > 4) {
			predicates.add(
					builder.like(builder.lower(root.get("nomeSemAcento")),
							"%" + StringExtended.toASCII(filtro.getNome().toLowerCase()) + "%"));
		}

		if (filtro.getTelefone() != null && !filtro.getTelefone().isEmpty()) {
			Predicate telefonePredicate = builder.equal(root.get("telefoneUm"), filtro.getTelefone());
			Predicate telefoneDoisPredicate = builder.equal(root.get("telefoneDois"), filtro.getTelefone());
			predicates.add(builder.or(telefonePredicate, telefoneDoisPredicate));
		}

		// Oculta registros inativados (duplicados consolidados).
		predicates.add(builder.equal(root.get("ativo"), true));
		// predicates.add(builder.isNotNull(root.get("imagem")));
		// Projeção por tupla (ver filtrados): não traz o blob "imagem".
		criteriaQuery.multiselect(root.get("id"), root.get("nome"), root.get("apelido"), root.get("telefoneUm"),
				root.get("timestamp"), sexoJoin, segueMeJoin, circuloJoin);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("segueMe")));

		return montarProjecao(manager.createQuery(criteriaQuery).getResultList());
	}

	/**
	 * Remonta as linhas da projeção (tupla) da listagem em {@link Seguidor},
	 * usando o construtor de projeção. Mantém o blob {@code imagem} fora da
	 * listagem — a foto é carregada sob demanda em {@link #findById(Integer)}.
	 */
	private List<Seguidor> montarProjecao(List<Object[]> linhas) {
		List<Seguidor> seguidores = new ArrayList<>(linhas.size());
		for (Object[] l : linhas) {
			seguidores.add(new Seguidor((Integer) l[0], (String) l[1], (String) l[2], (String) l[3], (Calendar) l[4],
					(Sexo) l[5], (SegueMe) l[6], (Circulo) l[7]));
		}
		return seguidores;
	}

	/** Predicados compartilhados pela listagem paginada e pela contagem. */
	private List<Predicate> predicadosSeguidor(CriteriaBuilder builder, Root<Seguidor> root, From<?, ?> segueMeJoin,
			SeguidorFilter filtro) {
		List<Predicate> predicates = new ArrayList<>();
		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}
		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (filtro.getCirculo() != null) {
			predicates.add(builder.equal(root.get("circulo"), filtro.getCirculo()));
		}
		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get("nomeSemAcento")),
							"%" + StringExtended.toASCII(filtro.getNome().toLowerCase()) + "%"),
					builder.like(builder.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"),
					builder.like(builder.lower(root.get("apelido")), "%" + filtro.getNome().toLowerCase() + "%")));
		}
		if (filtro.getSituacao().equals("ATIVO")) {
			predicates.add(builder.equal(root.get("situacaoSeguidor"), SituacaoSeguidor.ATIVO));
		} else {
			predicates.add(builder.equal(root.get("situacaoSeguidor"), SituacaoSeguidor.INATIVO));
		}
		predicates.add(builder.equal(root.get("ativo"), true));
		return predicates;
	}

	/** Conta o total de seguidores do filtro (usado pelo LazyDataModel). */
	public Long contar(SeguidorFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Seguidor> root = criteriaQuery.from(Seguidor.class);
		// Mesmos INNER joins que filtram o conjunto (endereco/sexo/segueMe/numeroRomano).
		root.join("endereco", JoinType.INNER);
		root.join("sexo", JoinType.INNER);
		From<?, ?> segueMeJoin = (From<?, ?>) root.join("segueMe", JoinType.INNER);
		segueMeJoin.join("numeroRomano", JoinType.INNER);
		criteriaQuery.select(builder.count(root));
		criteriaQuery.where(predicadosSeguidor(builder, root, segueMeJoin, filtro).toArray(new Predicate[0]));
		return manager.createQuery(criteriaQuery).getSingleResult();
	}

	/**
	 * Página da listagem (projeção sem o blob) para paginação server-side via
	 * LazyDataModel. {@code sortField} vem da coluna clicada no p:dataTable.
	 */
	public List<Seguidor> filtradosPaginado(SeguidorFilter filtro, int first, int pageSize, String sortField,
			boolean asc) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<Seguidor> root = criteriaQuery.from(Seguidor.class);
		root.join("endereco", JoinType.INNER);
		From<?, ?> sexoJoin = (From<?, ?>) root.join("sexo", JoinType.INNER);
		From<?, ?> segueMeJoin = (From<?, ?>) root.join("segueMe", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.join("numeroRomano", JoinType.INNER);
		From<?, ?> paroquiaJoin = (From<?, ?>) segueMeJoin.join("paroquia", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) root.join("circulo", JoinType.LEFT);

		criteriaQuery.multiselect(root.get("id"), root.get("nome"), root.get("apelido"), root.get("telefoneUm"),
				root.get("timestamp"), sexoJoin, segueMeJoin, circuloJoin);
		criteriaQuery.where(predicadosSeguidor(builder, root, segueMeJoin, filtro).toArray(new Predicate[0]));
		criteriaQuery.orderBy(ordenacaoSeguidor(builder, root, paroquiaJoin, numeroRomanoJoin, circuloJoin, sortField, asc));

		List<Object[]> linhas = manager.createQuery(criteriaQuery).setFirstResult(first).setMaxResults(pageSize)
				.getResultList();
		return montarProjecao(linhas);
	}

	/** Mapeia a coluna de ordenação do p:dataTable para a expressão da query (default: nome). */
	private Order ordenacaoSeguidor(CriteriaBuilder builder, Root<Seguidor> root, From<?, ?> paroquiaJoin,
			From<?, ?> numeroRomanoJoin, From<?, ?> circuloJoin, String sortField, boolean asc) {
		Expression<?> expr;
		switch (sortField == null ? "nome" : sortField) {
		case "telefoneUm":
			expr = root.get("telefoneUm");
			break;
		case "timestamp.time":
			expr = root.get("timestamp");
			break;
		case "segueMe.paroquia.descricao":
			expr = paroquiaJoin.get("descricao");
			break;
		case "segueMe.numeroRomano.numeroRomano":
			expr = numeroRomanoJoin.get("numeroRomano");
			break;
		case "circulo.corCirculo.descricao":
			expr = circuloJoin.get("corCirculo");
			break;
		case "nome":
		default:
			expr = root.get("nome");
			break;
		}
		return asc ? builder.asc(expr) : builder.desc(expr);
	}

	/**
	 * Procura nome e data nascimento
	 * 
	 * @param email
	 * @return
	 */
	public Seguidor findByNomeDtNascimentoAtualizar(String nome, Date dtnascimento) {
		try {
			return manager
					.createQuery("SELECT distinct(s) FROM Seguidor s " + "WHERE upper(s.nomeSemAcento) like :nome "
							+ "AND s.dataNascimento = :dtnascimento AND s.ativo = true", Seguidor.class)
					.setParameter("dtnascimento", dtnascimento).setParameter("nome", nome.toUpperCase())
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Seguidor findByNomeDtNascimento(String nome, Date dtnascimento) {
		try {
			return manager
					.createQuery("SELECT distinct(s) FROM Seguidor s " + "WHERE upper(s.nome) like :nome "
							+ "AND s.dataNascimento = :dtnascimento AND s.ativo = true", Seguidor.class)
					.setParameter("dtnascimento", dtnascimento).setParameter("nome", nome.toUpperCase())
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Seguidor> findByNome(String searchValue) {
		try {
			String nome = "%" + StringExtended.toASCII(searchValue.toUpperCase()) + "%";
			// Busca também por telefone: só os dígitos do que foi digitado (tira a
			// máscara) contra os telefones pessoais do seguidor, também normalizados no
			// banco. LIKE parcial permite achar por trechos do número.
			String digitos = searchValue.replaceAll("[^0-9]", "");
			boolean buscaTelefone = digitos.length() >= 3;

			StringBuilder jpql = new StringBuilder("select distinct(s) from Seguidor s where ("
					+ "s.nomeSemAcento LIKE :nome OR s.apelido LIKE :nome ");
			if (buscaTelefone) {
				jpql.append("OR function('regexp_replace', s.telefoneUm, '[^0-9]', '', 'g') LIKE :tel ");
				jpql.append("OR function('regexp_replace', s.telefoneDois, '[^0-9]', '', 'g') LIKE :tel ");
				jpql.append("OR function('regexp_replace', s.telefoneTres, '[^0-9]', '', 'g') LIKE :tel ");
				jpql.append("OR function('regexp_replace', s.telefoneQuatro, '[^0-9]', '', 'g') LIKE :tel ");
			}
			jpql.append(") AND s.situacaoSeguidor = :situacao AND s.segueMe != NULL AND s.ativo = true");

			TypedQuery<Seguidor> query = manager.createQuery(jpql.toString(), Seguidor.class)
					.setParameter("nome", nome).setParameter("situacao", SituacaoSeguidor.ATIVO);
			if (buscaTelefone) {
				query.setParameter("tel", "%" + digitos + "%");
			}
			return query.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Seguidor> findByNomeFicha(String nome) {
		try {
			return manager.createQuery("select distinct(s) from Seguidor s " + "where (s.nomeSemAcento LIKE :nome "
					+ "OR s.apelido LIKE :nome) AND  (s.situacaoSeguidor = :situacao  OR s.situacaoSeguidor = NULL) AND s.ativo = true",
					Seguidor.class).setParameter("nome", "%" + StringExtended.toASCII(nome.toUpperCase()) + "%")
					.setParameter("situacao", SituacaoSeguidor.ATIVO).setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
}