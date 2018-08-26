package com.segue.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.segue.filter.CirculoFilter;
import com.segue.model.Circulo;
import com.segue.model.CorCirculo;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.service.NegocioException;
import com.segue.util.jpa.Transactional;

public class CirculoRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Circulo porId(Integer id) {
		return manager.find(Circulo.class, id);
	}

	@Transactional
	public List<Circulo> listaAll() {
		return manager.createQuery("from Circulo Order by corCirculo desc", Circulo.class).getResultList();
	}

	public Circulo guardar(Circulo circulo) {
		return circulo = manager.merge(circulo);
	}

	@Transactional
	public void remover(Circulo circulo) throws NegocioException {
		try {
			circulo = porId(circulo.getId());
			manager.remove(circulo);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Círculo não pode ser excluído.");
		}
	}

	/**
	 * Busca Cículo por nome
	 * 
	 * @param circulo
	 * @return
	 */
	public List<Circulo> findByParoquia(Paroquia paroquia) {
		return manager
				.createQuery("SELECT c FROM Circulo c " + "JOIN FETCH c.segueMe s  "
						+ "WHERE c.segueMe.paroquia = :paroquia " + "ORDER BY c.segueMe", Circulo.class)
				.setParameter("paroquia", paroquia).getResultList();
	}

	/**
	 * Busca Cículo por Segue-me
	 * 
	 * @param circulo
	 * @return
	 */
	public List<Circulo> findBySegueMe(SegueMe segueMe) {
		return manager
				.createQuery("SELECT c FROM Circulo c WHERE c.segueMe = :segueMe " + "ORDER BY c.corCirculo",
						Circulo.class)
				.setParameter("segueMe", segueMe).getResultList();
	}

	/**
	 * Busca Cículo por cor and segue-me
	 * 
	 * @param circulo
	 * @return
	 */
	public Circulo findByCorSegueMe(SegueMe segueMe, CorCirculo cor) {
		Circulo c = null;
		try {
			c = manager.createQuery("SELECT c FROM Circulo c " + "WHERE c.segueMe = :segueMe AND c.corCirculo = :cor ",
					Circulo.class).setParameter("segueMe", segueMe).setParameter("cor", cor).getSingleResult();
		} catch (NoResultException e) {

		}
		return c;
	}

	/**
	 * Lista por filtros
	 * 
	 * @return
	 */
	public List<Circulo> filtrados(CirculoFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Circulo> criteriaQuery = builder.createQuery(Circulo.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Circulo> root = criteriaQuery.from(Circulo.class);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.like(builder.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}
		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (!filtro.getStatus().equals("TODOS")) {
			if (filtro.getStatus().equals("ATIVO")) {
				predicates.add(builder.equal(root.get("ativo"), true));
			} else {
				predicates.add(builder.equal(root.get("ativo"), false));
			}
		}
		if (filtro.getCor() != null && filtro.getCor().length > 0) {
			predicates.add(root.get("corCirculo").in(Arrays.asList(filtro.getCor())));
		}

		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("segueMe")));

		TypedQuery<Circulo> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

}