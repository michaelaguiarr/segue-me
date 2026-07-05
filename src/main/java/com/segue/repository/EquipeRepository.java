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

	public Equipe porId(Integer id) {
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

	/**
	 * Lista para exibição (pesquisa, dropdowns, relatórios): projeção SEM o blob
	 * {@code imagem}, que não é exibido nesses contextos e pesava ~9,7 MB por
	 * chamada. Quando a foto for necessária (regravar em disco), use
	 * {@link #listaComImagem()}.
	 */
	public List<Equipe> listaALL() {
		return manager.createQuery(
				"SELECT new com.segue.model.Equipe(b.id, b.titulo, b.ordem, b.pessoas, b.obs, b.ativo) "
						+ "FROM Equipe b ORDER BY b.ordem",
				Equipe.class).getResultList();
	}

	/**
	 * Lista completa COM o blob {@code imagem}. Usar só quando a foto é necessária
	 * (ex.: {@code FotoService} regrava os arquivos de foto das equipes em disco).
	 */
	public List<Equipe> listaComImagem() {
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