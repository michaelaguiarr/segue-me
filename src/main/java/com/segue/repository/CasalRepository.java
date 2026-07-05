
package com.segue.repository;

import com.segue.filter.CasalFilter;
import com.segue.model.Casal;
import com.segue.service.NegocioException;
import com.segue.util.StringExtended;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CasalRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Casal findById(Integer id) {
		return manager.find(Casal.class, id);
	}

	public Casal guardar(Casal casal) {
		return casal = manager.merge(casal);
	}

	public void remover(Casal casal) throws NegocioException {
		try {
			casal = findById(casal.getId());
			manager.remove(casal);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Casal não pode ser excluído.");
		}
	}

	/**
	 * Lista todos
	 * 
	 * @return
	 */
	public List<Casal> findAll() {
		return manager.createQuery("FROM Casal", Casal.class).getResultList();
	}

	public List<Casal> filtradosInscricao(CasalFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Casal> criteriaQuery = builder.createQuery(Casal.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Casal> root = criteriaQuery.from(Casal.class);
		From<?, ?> enderecoJoin = (From<?, ?>) root.fetch("endereco", JoinType.INNER);
		From<?, ?> paroquiaJoin = (From<?, ?>) root.fetch("paroquia", JoinType.INNER);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(root.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("segueMe")));

		TypedQuery<Casal> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Casal> filtrados(CasalFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Casal> criteriaQuery = builder.createQuery(Casal.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Casal> root = criteriaQuery.from(Casal.class);
		From<?, ?> enderecoJoin = (From<?, ?>) root.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) root.fetch("paroquia", JoinType.LEFT);
		From<?, ?> eccJoin = (From<?, ?>) root.fetch("ecc", JoinType.LEFT);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) eccJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(root.get("paroquia"), filtro.getParoquia()));
		}
		if (StringUtils.isNotBlank(filtro.getNomeEle())) {
			predicates.add(
					builder.like(builder.lower(root.get("nomeEleSemAcento")), "%" + StringExtended.toASCII(filtro.getNomeEle().toLowerCase()) + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelidoEle())) {
			predicates.add(builder.like(builder.lower(root.get("apelidoEle")),
					"%" + filtro.getApelidoEle().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getNomeEla())) {
			predicates.add(
					builder.like(builder.lower(root.get("nomeElaSemAcento")), "%" + StringExtended.toASCII(filtro.getNomeEla().toLowerCase()) + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelidoEla())) {
			predicates.add(builder.like(builder.lower(root.get("apelidoEla")),
					"%" + filtro.getApelidoEla().toLowerCase() + "%"));
		}
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nomeEle")), builder.asc(root.get("paroquia")));

		TypedQuery<Casal> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}
	
	public List<Casal> filtradosPublic(CasalFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Casal> criteriaQuery = builder.createQuery(Casal.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Casal> root = criteriaQuery.from(Casal.class);
		From<?, ?> enderecoJoin = (From<?, ?>) root.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) root.fetch("paroquia", JoinType.LEFT);
		From<?, ?> eccJoin = (From<?, ?>) root.fetch("ecc", JoinType.LEFT);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) eccJoin.fetch("numeroRomano", JoinType.LEFT);

		if (filtro.getTelefone() != null && !filtro.getTelefone().isEmpty()) {
			Predicate telefoneEleUmPredicate = builder.equal(root.get("telefoneEleUm"), filtro.getTelefone());
			Predicate telefoneEleDoisPredicate = builder.equal(root.get("telefoneEleDois"), filtro.getTelefone());
			Predicate telefoneElaUmPredicate = builder.equal(root.get("telefoneElaUm"), filtro.getTelefone());
			Predicate telefoneElaDoisPredicate = builder.equal(root.get("telefoneElaDois"), filtro.getTelefone());
			predicates.add(builder.or(telefoneEleUmPredicate, telefoneEleDoisPredicate, telefoneElaUmPredicate, telefoneElaDoisPredicate));
		}

//		Predicate orClause =
//			    builder.or(
//			    		builder.like(
//			    				builder.lower(root.get("nomeElaSemAcento")), StringExtended.toASCII(filtro.getNomeEle().toLowerCase())),
//			    		builder.like(
//			    				builder.lower(root.get("nomeEleSemAcento")), StringExtended.toASCII(filtro.getNomeEle().toLowerCase())));
//		predicates.add(orClause);

//		if (filtro.getNomeEla() == null) {
//			predicates.add(
//					builder.equal(root.get("dataNascimentoEle").as(java.sql.Date.class), filtro.getDtNascimento()));
//		} else {
//			predicates.add(
//					builder.equal(root.get("dataNascimentoEla").as(java.sql.Date.class), filtro.getDtNascimento()));
//		}
		
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nomeEle")), builder.asc(root.get("paroquia")));

		TypedQuery<Casal> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}


	/**
	 * Procura nome e data nascimento
	 * 
	 * @param email
	 * @return
	 */
	public Casal findByNomeDtNascEle(String nomeEle, Date dataNascimentoEle) {
		Casal casal = null;
		try {
			return manager.createQuery("SELECT c FROM Casal c "
					+ "WHERE lower(c.nomeEleSemAcento) LIKE lower(:nomeEle)  AND c.dataNascimentoEle = :dataNascimentoEle",
					Casal.class).setParameter("nomeEle", StringExtended.toASCII(nomeEle.toUpperCase())).setParameter("dataNascimentoEle", dataNascimentoEle)
					.getSingleResult();
		} catch (NoResultException e) {
			// Nenhum Equipe encontrado
		}
		return casal;
	}

	public Casal findByNomeDtNascEla(String nomeEla, Date dataNascimentoEla) {
		Casal casal = null;
		try {
			return manager.createQuery("SELECT c FROM Casal c "
					+ "WHERE lower(c.nomeEleSemAcento) LIKE lower(:nomeEla)  AND c.dataNascimentoEla = :dataNascimentoEla",
					Casal.class).setParameter("nomeEla", StringExtended.toASCII(nomeEla.toUpperCase())).setParameter("dataNascimentoEla", dataNascimentoEla)
					.getSingleResult();
		} catch (NoResultException e) {
			// Nenhum Equipe encontrado
		}
		return casal;
	}

	public List<Casal> findByNome(String searchValue) {
		try {
			String nome = "%" + StringExtended.toASCII(searchValue.toUpperCase()) + "%";
			String apelido = "%" + searchValue.toUpperCase() + "%";
			// Busca também por telefone: só os dígitos do que foi digitado (tira a
			// máscara) contra os telefones pessoais do casal, também normalizados no
			// banco. LIKE parcial permite achar por trechos do número.
			String digitos = searchValue.replaceAll("[^0-9]", "");
			boolean buscaTelefone = digitos.length() >= 3;

			StringBuilder jpql = new StringBuilder("select distinct(c) from Casal c where "
					+ "lower(c.nomeEleSemAcento) LIKE lower(:nome) "
					+ "OR lower(c.apelidoEle) LIKE lower(:apelido) "
					+ "OR lower(c.nomeElaSemAcento) LIKE lower(:nome) "
					+ "OR lower(c.apelidoEla) LIKE lower(:apelido) ");
			if (buscaTelefone) {
				jpql.append("OR function('regexp_replace', c.telefoneEleUm, '[^0-9]', '', 'g') LIKE :tel ");
				jpql.append("OR function('regexp_replace', c.telefoneEleDois, '[^0-9]', '', 'g') LIKE :tel ");
				jpql.append("OR function('regexp_replace', c.telefoneElaUm, '[^0-9]', '', 'g') LIKE :tel ");
				jpql.append("OR function('regexp_replace', c.telefoneElaDois, '[^0-9]', '', 'g') LIKE :tel ");
			}

			TypedQuery<Casal> query = manager.createQuery(jpql.toString(), Casal.class)
					.setParameter("nome", nome).setParameter("apelido", apelido);
			if (buscaTelefone) {
				query.setParameter("tel", "%" + digitos + "%");
			}
			return query.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
}