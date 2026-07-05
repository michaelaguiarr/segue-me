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

import com.segue.filter.PalestranteConvidadoFilter;
import com.segue.model.PalestranteConvidado;
import com.segue.model.Sexo;
import com.segue.service.NegocioException;

public class PalestranteConvidadoRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public PalestranteConvidado findById(Integer id) {
		return manager.find(PalestranteConvidado.class, id);
	}

	public PalestranteConvidado guardar(PalestranteConvidado palestranteConvidado) {
		return palestranteConvidado = manager.merge(palestranteConvidado);
	}

	public void remover(PalestranteConvidado palestranteConvidado) throws NegocioException {
		try {
			palestranteConvidado = findById(palestranteConvidado.getId());
			manager.remove(palestranteConvidado);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Palestrante Convidado não pode ser excluído.");
		}
	}

	/**
	 * Lista todos
	 * 
	 * @return
	 */
	public List<PalestranteConvidado> findAll() {
		return manager.createQuery("FROM PalestranteConvidado", PalestranteConvidado.class).getResultList();
	}

	public List<PalestranteConvidado> filtrados(PalestranteConvidadoFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		List<Predicate> predicates = new ArrayList<>();

		Root<PalestranteConvidado> root = criteriaQuery.from(PalestranteConvidado.class);
		// Projeção da listagem: joins (não fetch) só do que a tabela exibe, SEM o
		// blob imagem (foto). INNER em endereco/sexo preserva o conjunto de
		// resultados da consulta original.
		root.join("endereco", JoinType.INNER);
		Join<Object, Object> sexoJoin = root.join("sexo", JoinType.INNER);

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"),
					builder.like(builder.lower(root.get("apelido")), "%" + filtro.getNome().toLowerCase() + "%")));
		}
		// Oculta registros inativados (duplicados consolidados).
		predicates.add(builder.equal(root.get("ativo"), true));
		criteriaQuery.multiselect(root.get("id"), root.get("timestamp"), root.get("nome"), root.get("apelido"),
				root.get("telefoneUm"), sexoJoin.get("cod"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")));

		List<Tuple> tuples = manager.createQuery(criteriaQuery).getResultList();
		List<PalestranteConvidado> convidados = new ArrayList<>(tuples.size());
		for (Tuple t : tuples) {
			Integer sexoCod = (Integer) t.get(5);
			Sexo sexo = sexoCod != null ? new Sexo(sexoCod) : null;
			convidados.add(new PalestranteConvidado((Integer) t.get(0), (Calendar) t.get(1), (String) t.get(2),
					(String) t.get(3), (String) t.get(4), sexo));
		}
		return convidados;
	}

	/**
	 * Procura nome e data nascimento
	 * 
	 * @param email
	 * @return
	 */
	public PalestranteConvidado findByNomeDtNascimento(String nome, Date dtnascimento) {
		try {
			return manager
					.createQuery("SELECT distinct(s) FROM PalestranteConvidado s " + "WHERE s.nome like :nome "
							+ "AND s.dataNascimento = :dtnascimento AND s.ativo = true", PalestranteConvidado.class)
					.setParameter("dtnascimento", dtnascimento).setParameter("nome", nome).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<PalestranteConvidado> findByNome(String searchValue) {
		try {
			String nome = "%" + searchValue + "%";
			// Busca também por telefone: só os dígitos do que foi digitado (tira a
			// máscara) contra o telefone do convidado, também normalizado no banco.
			String digitos = searchValue.replaceAll("[^0-9]", "");
			boolean buscaTelefone = digitos.length() >= 3;

			StringBuilder jpql = new StringBuilder("select distinct(s) from PalestranteConvidado s where ("
					+ "lower(s.nome) LIKE lower(:nome) OR lower(s.apelido) LIKE lower(:nome) ");
			if (buscaTelefone) {
				jpql.append("OR function('regexp_replace', s.telefoneUm, '[^0-9]', '', 'g') LIKE :tel ");
			}
			// Fecha o grupo de OR e oculta registros inativados (duplicados consolidados).
			jpql.append(") AND s.ativo = true");

			TypedQuery<PalestranteConvidado> query = manager.createQuery(jpql.toString(), PalestranteConvidado.class)
					.setParameter("nome", nome);
			if (buscaTelefone) {
				query.setParameter("tel", "%" + digitos + "%");
			}
			return query.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
}