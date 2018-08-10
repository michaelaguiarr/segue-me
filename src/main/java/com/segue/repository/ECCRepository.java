package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import com.segue.model.ECC;
import com.segue.model.Paroquia;
import com.segue.service.NegocioException;
import com.segue.util.jpa.Transactional;

public class ECCRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public ECC porId(Long id) {
		return manager.find(ECC.class, id);
	}

	@Transactional
	public List<ECC> listaAll() {
		return manager.createQuery(
						  "SELECT s FROM ECC s " 
						+ "JOIN FETCH s.paroquia p " 
						+ "JOIN FETCH s.numeroRomano n  "
						+ "ORDER BY s.titulo DESC, n.numero DESC",
				ECC.class).getResultList();
	}

	public ECC guardar(ECC ecc) {
		return ecc = manager.merge(ecc);
	}

	@Transactional
	public void remover(ECC ecc) throws NegocioException {
		try {
			ecc = porId(ecc.getId());
			manager.remove(ecc);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Evento não pode ser excluído.");
		}
	}

	/**
	 * Busca ecc pelo paroquia, titulo e numero
	 * 
	 * @param cod
	 * @return
	 */
	public List<ECC> findByParoquiaAndTitulo(String searchValue) {
		return manager
				.createQuery("SELECT s FROM ECC s " + "JOIN FETCH s.paroquia p "
						+ "JOIN FETCH s.numeroRomano n  " + "WHERE CONCAT(n.numero,'') LIKE :cod "
						+ "OR CONCAT(n.numeroRomano,'') LIKE :cod " + "OR lower(s.titulo) LIKE lower(:titulo) "
						+ "OR lower(p.descricao) LIKE lower(:titulo) " + "ORDER BY n.numero DESC", ECC.class)
				.setParameter("cod", "%" + searchValue + "%").setParameter("titulo", "%" + searchValue + "%")
				.getResultList();
	}

	/**
	 * Busca segueme pelo paroquia
	 * 
	 * @param cod
	 * @return
	 */
	public List<ECC> findByParoquia(Paroquia paroquia) {
		return manager
				.createQuery(
						"SELECT s FROM ECC s " + "JOIN FETCH s.paroquia p " + "JOIN FETCH s.numeroRomano n  "
								+ "WHERE s.paroquia = :paroquia " + "ORDER BY s.titulo DESC, n.numero DESC",
						ECC.class)
				.setParameter("paroquia", paroquia).getResultList();
	}

}