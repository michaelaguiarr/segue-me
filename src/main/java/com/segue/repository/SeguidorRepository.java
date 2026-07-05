
package com.segue.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.segue.filter.SeguidorFilter;
import com.segue.model.Seguidor;
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
		CriteriaQuery<Seguidor> criteriaQuery = builder.createQuery(Seguidor.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Seguidor> root = criteriaQuery.from(Seguidor.class);
		From<?, ?> enderecoJoin = (From<?, ?>) root.fetch("endereco", JoinType.INNER);
		From<?, ?> sexoJoin = (From<?, ?>) root.fetch("sexo", JoinType.INNER);
		From<?, ?> paroquiaJoin = (From<?, ?>) root.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);
		From<?, ?> circuloJoin = (From<?, ?>) root.fetch("circulo", JoinType.LEFT);

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
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("segueMe")));

		TypedQuery<Seguidor> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Seguidor> filtradosPublic(SeguidorFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Seguidor> criteriaQuery = builder.createQuery(Seguidor.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Seguidor> root = criteriaQuery.from(Seguidor.class);
		From<?, ?> enderecoJoin = (From<?, ?>) root.fetch("endereco", JoinType.INNER);
		From<?, ?> sexoJoin = (From<?, ?>) root.fetch("sexo", JoinType.INNER);
		From<?, ?> paroquiaJoin = (From<?, ?>) root.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);
		From<?, ?> circuloJoin = (From<?, ?>) root.fetch("circulo", JoinType.LEFT);

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
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("segueMe")));

		TypedQuery<Seguidor> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
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