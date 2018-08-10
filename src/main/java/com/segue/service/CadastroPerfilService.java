package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Perfil;
import com.segue.repository.PerfilRepository;
import com.segue.util.jpa.Transactional;

public class CadastroPerfilService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PerfilRepository repository;

	@Transactional
	public Perfil salvar(Perfil perfil) throws NegocioException {
		return repository.guardar(perfil);
	}

	@Transactional
	public void remover(Perfil perfil) throws NegocioException {
		repository.remover(perfil);
	}

}
