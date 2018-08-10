package com.segue.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import com.segue.model.Perfil;
import com.segue.repository.PerfilRepository;

public class PerfilService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PerfilRepository perfilRepository;

	@Transactional
	public Perfil save(Perfil perfil) throws NegocioException {
		perfil.setNome(perfil.getNome().toUpperCase());
		return perfilRepository.guardar(perfil);
	}

	@Transactional
	public void remover(Perfil perfil) throws NegocioException {
		perfilRepository.remover(perfil);
	}
	
	public List<Perfil> findAll() {
		return perfilRepository.listaALL();
	}

}
