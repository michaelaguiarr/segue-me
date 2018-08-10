package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.segue.model.Sexo;

public class SexoRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;
	
	public Sexo porId(Integer id) {
		return manager.find(Sexo.class, id);
	}

	/**
	 * Lista todos
	 * 
	 * @return
	 */
	public List<Sexo> findAll() {
		return manager.createQuery("FROM Sexo", Sexo.class).getResultList();
	}
}
