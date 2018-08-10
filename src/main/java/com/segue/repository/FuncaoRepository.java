package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import com.segue.model.Equipe;
import com.segue.model.Funcao;
import com.segue.service.NegocioException;

public class FuncaoRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Funcao porId(Long id) {
		return manager.find(Funcao.class, id);
	}

	public Funcao guardar(Funcao funcao) {
		return funcao = manager.merge(funcao);
	}

	public void remover(Funcao funcao) throws NegocioException {
		try {
			funcao = porId(funcao.getId());
			manager.remove(funcao);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Funcao não pode ser excluído.");
		}
	}

	public List<Funcao> listaALL() {
		return manager
				.createQuery("SELECT f FROM Funcao f " + "JOIN FETCH f.equipe e " + "ORDER BY e.ordem ASC, f.ordem ",
						Funcao.class)
				.getResultList();
	}

	/**
	 * Pesquisa por nome e equipe
	 * 
	 * @return
	 */
	public Funcao findNomeSigle(String nome, Equipe equipe) {
		Funcao Funcao = null;
		try {
			Funcao = manager.createQuery(
					"SELECT f FROM Funcao f " + "JOIN FETCH f.equipe e "
							+ "WHERE lower(f.descricao) = lower(:descricao) and f.equipe = :equipe ORDER BY f.equipe.ordem ASC, f.ordem",
					Funcao.class).setParameter("descricao", nome).setParameter("equipe", equipe).getSingleResult();
		} catch (NoResultException e) {
			// Nenhum Funcao encontrado
		}
		return Funcao;
	}
	
	
	/**
	 * Pesquisa por nome e equipe
	 * 
	 * @return
	 */
	public Funcao findEquipeOrdem(Equipe equipe , Integer ordem) {
		Funcao Funcao = null;
		try {
			Funcao = manager.createQuery(
					"SELECT f FROM Funcao f " + "JOIN FETCH f.equipe e "
							+ "WHERE f.equipe = :equipe AND f.ordem = :ordem ORDER BY f.equipe.ordem ASC, f.ordem",
					Funcao.class).setParameter("ordem", ordem).setParameter("equipe", equipe).getSingleResult();
		} catch (NoResultException e) {
			// Nenhum Funcao encontrado
		}
		return Funcao;
	}

	/**
	 * Busca segueme pelo nome
	 * 
	 * @param cod
	 * @return
	 */
	public List<Funcao> findByNome(String searchValue) {
		return manager
				.createQuery("SELECT b FROM Funcao b " + "WHERE lower(b.descricao) LIKE lower(:descricao) "
						+ "ORDER BY b.equipe.ordem ASC, b.ordem", Funcao.class)
				.setParameter("descricao", "%" + searchValue + "%").getResultList();
	}

	/**
	 * Busca segueme pelo paroquia
	 * 
	 * @param cod
	 * @return
	 */
	public List<Funcao> findByEquipe(Equipe equipe) {
		return manager
				.createQuery("SELECT distinct(f) FROM Funcao f " + "JOIN FETCH f.equipe e "
						+ "WHERE f.equipe = :equipe " + "ORDER BY f.ordem DESC", Funcao.class)
				.setParameter("equipe", equipe).getResultList();
	}

}