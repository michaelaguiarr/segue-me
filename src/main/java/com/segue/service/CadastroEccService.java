package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.ECC;
import com.segue.repository.ECCRepository;
import com.segue.util.jpa.Transactional;

public class CadastroEccService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ECCRepository repository;

	@Transactional
	public ECC salvar(ECC ecc) throws NegocioException {
		return repository.guardar(ecc);
	}

	@Transactional
	public void remover(ECC ecc) throws NegocioException {
		repository.remover(ecc);
	}

}
