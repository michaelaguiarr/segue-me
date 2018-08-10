package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.segue.model.AplicacaoModulo;

public class AplicacaoModuloRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	/**
	 * Lista todos
	 * 
	 * @return
	 */
	public List<AplicacaoModulo> findAll() {
		return manager.createQuery("SELECT r FROM AplicacaoModulo r", AplicacaoModulo.class).getResultList();
	}

	/**
	 * Carrega Modulo com id (necessario utilizar converter)
	 * 
	 * @param id
	 * @return
	 */
	public AplicacaoModulo findById(Integer id) {
		return manager.find(AplicacaoModulo.class, id);
	}

}
