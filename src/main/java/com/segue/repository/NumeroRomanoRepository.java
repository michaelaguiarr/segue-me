package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.segue.model.NumeroRomano;

public class NumeroRomanoRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public NumeroRomano porId(Long id) {
		return manager.find(NumeroRomano.class, id);
	}

	public List<NumeroRomano> listaNumerosRomanos() {
		return manager.createQuery("from NumeroRomano", NumeroRomano.class).getResultList();
	}

}