package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Paroquia;
import com.segue.repository.ParoquiaRepository;
import com.segue.util.jpa.Transactional;

public class CadastroParoquiaService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ParoquiaRepository repository;

	@Transactional
	public Paroquia salvar(Paroquia paroquia) throws NegocioException {
		Paroquia paroquiaExistente = repository.findByDescricao(paroquia.getDescricao());
		if (paroquiaExistente != null && !paroquiaExistente.equals(paroquia)) {
			throw new NegocioException("Já existe um Paróquia com este nome.");
		}
		return repository.guardar(paroquia);
	}

	@Transactional
	public void remover(Paroquia paroquia) throws NegocioException {
		repository.remover(paroquia);
	}

}
