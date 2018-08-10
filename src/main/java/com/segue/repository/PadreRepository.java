
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

import org.apache.commons.lang3.StringUtils;

import com.segue.filter.PadreFilter;
import com.segue.model.Padre;
import com.segue.service.NegocioException;

public class PadreRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Padre findById(Integer id) {
		return manager.find(Padre.class, id);
	}

	public Padre guardar(Padre padre) {
		return padre = manager.merge(padre);
	}

	public void remover(Padre padre) throws NegocioException {
		try {
			padre = findById(padre.getId());
			manager.remove(padre);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Padre não pode ser excluído.");
		}
	}

	/**
	 * Lista todos
	 * 
	 * @return
	 */
	public List<Padre> findAll() {
		return manager.createQuery("FROM Padre", Padre.class).getResultList();
	}

	/**
	 * Procura nome e data nascimento
	 * 
	 * @param email
	 * @return
	 */
	public Padre findByNomeDtNascimento(String nome, Date dtnascimento) {
		try {
			return manager
					.createQuery("SELECT distinct(s) FROM Padre s " + "WHERE s.nome like :nome "
							+ "AND s.dataNascimento = :dtnascimento", Padre.class)
					.setParameter("dtnascimento", dtnascimento).setParameter("nome", nome).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Padre> findByNome(String nome) {
		try {
			return manager
					.createQuery("select distinct(s) from Padre s " + "where (s.nome LIKE :nome "
							+ "OR s.apelido LIKE :nome) ", Padre.class)
					.setParameter("nome", "%" + nome + "%").setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Padre> filtrados(PadreFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Padre> criteriaQuery = builder.createQuery(Padre.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Padre> root = criteriaQuery.from(Padre.class);
		From<?, ?> enderecoJoin = (From<?, ?>) root.fetch("endereco", JoinType.INNER);
		From<?, ?> paroquiaJoin = (From<?, ?>) root.fetch("paroquia", JoinType.LEFT);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(root.get("paroquia"), filtro.getParoquia()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.like(builder.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(
					builder.like(builder.lower(root.get("apelido")), "%" + filtro.getApelido().toLowerCase() + "%"));
		}

		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("paroquia")));

		TypedQuery<Padre> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}
}