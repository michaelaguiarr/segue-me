package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import com.segue.model.Equipe;
import com.segue.service.NegocioException;

public class EquipeRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Equipe porId(Long id) {
		return manager.find(Equipe.class, id);
	}

	public Equipe guardar(Equipe equipe) {
		return equipe = manager.merge(equipe);
	}

	public void remover(Equipe equipe) throws NegocioException {
		try {
			equipe = porId(equipe.getId());
			manager.remove(equipe);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Equipe não pode ser excluído.");
		}
	}

	public List<Equipe> listaALL() {
		return manager.createQuery("SELECT b FROM Equipe b ORDER BY b.ordem ", Equipe.class).getResultList();
	}

	/**
	 * Pesquisa por nome
	 * 
	 * @return
	 */
	public Equipe findNomeSigle(String nome) {
		Equipe Equipe = null;
		try {
			Equipe = manager
					.createQuery("SELECT b FROM Equipe b " + "WHERE lower(b.titulo) = lower(:titulo) ", Equipe.class)
					.setParameter("titulo", nome).getSingleResult();
		} catch (NoResultException e) {
			// Nenhum Equipe encontrado
		}
		return Equipe;
	}

	/**
	 * Pesquisa por Ordem
	 * 
	 * @return
	 */
	public Equipe findOrdemSigle(Integer ordem) {
		Equipe Equipe = null;
		try {
			Equipe = manager.createQuery("SELECT b FROM Equipe b " + "WHERE b.ordem = :ordem AND b.ativo = true ", Equipe.class)
					.setParameter("ordem", ordem).getSingleResult();
		} catch (NoResultException e) {
			// Nenhum Equipe encontrado
		}
		return Equipe;
	}

	/**
	 * Busca segueme pelo nome
	 * 
	 * @param cod
	 * @return
	 */
	public List<Equipe> findByNome(String searchValue) {
		return manager
				.createQuery("SELECT b FROM Equipe b " + "WHERE lower(b.titulo) LIKE lower(:titulo) "
						+ "ORDER BY b.titulo ", Equipe.class)
				.setParameter("titulo", "%" + searchValue + "%").getResultList();
	}
}