package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import com.segue.model.Palestra;
import com.segue.service.NegocioException;
import com.segue.util.jpa.Transactional;

public class PalestraRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Palestra porId(Long id) {
		return manager.find(Palestra.class, id);
	}

	@Transactional
	public List<Palestra> listaAll() {
		return manager.createQuery("from Palestra Order by id desc", Palestra.class).getResultList();
	}

	public Palestra guardar(Palestra palestra) {
		return palestra = manager.merge(palestra);
	}

	@Transactional
	public void remover(Palestra palestra) throws NegocioException {
		try {
			palestra = porId(palestra.getId());
			manager.remove(palestra);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Evento não pode ser excluído.");
		}
	}

	/**
	 * Palestra por nome
	 * 
	 * @param cod
	 * @return
	 */
	public List<Palestra> findByNome(String searchValue) {
		return manager
				.createQuery("SELECT distinct(p) FROM Palestra p " + "WHERE lower(p.nome) LIKE lower(:nome) "
						+ "ORDER BY p.ativo ", Palestra.class)
				.setParameter("nome", "%" + searchValue + "%").getResultList();
	}

	/**
	 * Busca palestra ativas
	 * 
	 * @param cod
	 * @return
	 */
	public List<Palestra> listaAtivo() {
		return manager.createQuery("SELECT distinct(p) FROM Palestra p " + "WHERE p.ativo = true ", Palestra.class)
				.getResultList();
	}

	public Palestra findNomeSigle(String nome) {
		Palestra palestra = null;
		try {
			palestra = manager
					.createQuery("SELECT p FROM Palestra p " + "WHERE lower(p.nome) = lower(:nome) ORDER BY p.nome ASC",
							Palestra.class)
					.setParameter("nome", nome).getSingleResult();
		} catch (NoResultException e) {
			// Nenhum Palestra encontrado
		}
		return palestra;
	}
	
	
	public Palestra findOrdem(Integer ordem) {
		Palestra palestra = null;
		try {
			palestra = manager
					.createQuery("SELECT p FROM Palestra p " + "WHERE p.ordem = :ordem and p.ativo = true ORDER BY p.nome ASC",
							Palestra.class)
					.setParameter("ordem", ordem).getSingleResult();
		} catch (NoResultException e) {
			// Nenhum Palestra encontrado
		}
		return palestra;
	}

}