package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.service.NegocioException;
import com.segue.util.jpa.Transactional;

public class SegueMeRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public SegueMe porId(Long id) {
		return manager.find(SegueMe.class, id);
	}

	@Transactional
	public List<SegueMe> listaEjcs() {
		return manager.createQuery("SELECT s FROM SegueMe s " 
				+ "JOIN FETCH s.paroquia p "
				+ "JOIN FETCH s.numeroRomano n  "
//				+ "WHERE "
//				+ "s.ativo = true "
				+ "ORDER BY p.descricao, n.numero DESC", SegueMe.class).getResultList();
		
	}
	
	public SegueMe guardar(SegueMe ejc) {
		return ejc = manager.merge(ejc);
	}

	@Transactional
	public void remover(SegueMe ejc) throws NegocioException {
		try {
			ejc = porId(ejc.getId());
			manager.remove(ejc);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Evento não pode ser excluído.");
		}
	}
	
	
	/**
	 * Busca segueme pelo paroquia, titulo e numero
	 * 
	 * @param cod
	 * @return
	 */
	public List<SegueMe> findByParoquiaAndTitulo(String searchValue) {
		return manager
				.createQuery("SELECT distinct(s) FROM SegueMe s " 
						+ "JOIN FETCH s.paroquia p "
						+ "JOIN FETCH s.numeroRomano n  " 
						+ "WHERE CONCAT(n.numero,'') LIKE :cod "
						+ "OR CONCAT(n.numeroRomano,'') LIKE :cod "
						+ "OR lower(s.titulo) LIKE lower(:titulo) "
						+ "OR lower(p.descricao) LIKE lower(:titulo) "
						+ "ORDER BY n.numero DESC", SegueMe.class)
				.setParameter("cod", "%" + searchValue + "%" )
				.setParameter("titulo", "%" +searchValue + "%")
				.getResultList();
	}
	
	
	/**
	 * Busca segueme pelo paroquia
	 * 
	 * @param cod
	 * @return
	 */
	public List<SegueMe> findByParoquia(Paroquia paroquia) {
		return manager
				.createQuery("SELECT distinct(s) FROM SegueMe s " 
						+ "JOIN FETCH s.paroquia p "
						+ "JOIN FETCH s.numeroRomano n  " 
						+ "WHERE s.paroquia = :paroquia "
//						+ "AND s.ativo = true "
						+ "ORDER BY s.titulo DESC, n.numero DESC", SegueMe.class)
				.setParameter("paroquia", paroquia)
				.getResultList();
	}
	
	


}