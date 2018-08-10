package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import com.segue.model.Perfil;
import com.segue.service.NegocioException;

public class PerfilRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Perfil porId(Long id) {
		return manager.find(Perfil.class, id);
	}

	public Perfil guardar(Perfil perfil) {
		return perfil = manager.merge(perfil);
	}

	public void remover(Perfil perfil) throws NegocioException {
		try {
			perfil = porId(perfil.getId());
			manager.remove(perfil);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Perfil não pode ser excluído.");
		}
	}

	public List<Perfil> listaALL() {
		return manager.createQuery("SELECT p FROM Perfil p ORDER BY p.nome", Perfil.class)
				.getResultList();
	}

}