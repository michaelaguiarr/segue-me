package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import com.segue.model.Paroquia;
import com.segue.service.NegocioException;
import com.segue.util.jpa.Transactional;

public class ParoquiaRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Paroquia porId(Long id) {
		return manager.find(Paroquia.class, id);
	}

	public List<Paroquia> listaParoquias() {
		return manager.createQuery("from Paroquia", Paroquia.class).getResultList();
	}

	public Paroquia guardar(Paroquia paroquia) {
		return paroquia = manager.merge(paroquia);
	}

	@Transactional
	public void remover(Paroquia paroquia) throws NegocioException {
		try {
			paroquia = porId(paroquia.getId());
			manager.remove(paroquia);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Círculo não pode ser excluído.");
		}
	}

	/**
	 * Busca mesma descricao
	 * 
	 * @param circulo
	 * @return
	 */
	public Paroquia findByDescricao(String descricao) {
		Paroquia c = null;
		try {
			c = manager
					.createQuery("SELECT c FROM Paroquia c " + "WHERE lower(c.descricao) = :descricao ", Paroquia.class)
					.setParameter("descricao", descricao.toLowerCase()).getSingleResult();
		} catch (NoResultException e) {

		}
		return c;
	}

	/**
	 * Busca segueme pelo paroquia, titulo e numero
	 * 
	 * @param cod
	 * @return
	 */
	public List<Paroquia> findByNome(String searchValue) {
		return manager
				.createQuery("SELECT p FROM Paroquia p " + "WHERE lower(p.descricao) LIKE lower(:descricao) "
						+ "OR lower(p.cidade) LIKE lower(:cidade) " + "OR lower(p.estado) LIKE lower(:estado) "
						+ "ORDER BY p.descricao DESC", Paroquia.class)
				.setParameter("descricao", "%" + searchValue + "%").setParameter("cidade", "%" + searchValue + "%")
				.setParameter("estado", "%" + searchValue + "%").getResultList();
	}

}