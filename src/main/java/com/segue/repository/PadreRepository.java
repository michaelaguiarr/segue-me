
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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.segue.filter.PadreFilter;
import com.segue.model.Padre;
import com.segue.model.Paroquia;
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

	public List<Padre> findByNome(String searchValue) {
		try {
			String nome = "%" + searchValue + "%";
			// Busca também por telefone: só os dígitos do que foi digitado (tira a
			// máscara) contra o telefone do padre, também normalizado no banco.
			String digitos = searchValue.replaceAll("[^0-9]", "");
			boolean buscaTelefone = digitos.length() >= 3;

			StringBuilder jpql = new StringBuilder("select distinct(s) from Padre s where ("
					+ "lower(s.nome) LIKE lower(:nome) OR lower(s.apelido) LIKE lower(:nome) ");
			if (buscaTelefone) {
				jpql.append("OR function('regexp_replace', s.telefoneUm, '[^0-9]', '', 'g') LIKE :tel ");
			}
			jpql.append(")");

			TypedQuery<Padre> query = manager.createQuery(jpql.toString(), Padre.class)
					.setParameter("nome", nome);
			if (buscaTelefone) {
				query.setParameter("tel", "%" + digitos + "%");
			}
			return query.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Padre> filtrados(PadreFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		List<Predicate> predicates = new ArrayList<>();

		Root<Padre> root = criteriaQuery.from(Padre.class);
		// Projeção da listagem: joins (não fetch) só do que a tabela exibe, SEM o
		// blob imagem (foto). O INNER em endereco preserva o conjunto de resultados
		// da consulta original.
		root.join("endereco", JoinType.INNER);
		Join<Object, Object> paroquiaJoin = root.join("paroquia", JoinType.LEFT);

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

		criteriaQuery.multiselect(root.get("id"), root.get("nome"), root.get("apelido"), paroquiaJoin.get("descricao"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("paroquia")));

		List<Tuple> tuples = manager.createQuery(criteriaQuery).getResultList();
		List<Padre> padres = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			Paroquia paroquia = new Paroquia();
			paroquia.setDescricao((String) t.get(3));
			padres.add(new Padre((Integer) t.get(0), (String) t.get(1), (String) t.get(2), paroquia));
		}
		return padres;
	}
}