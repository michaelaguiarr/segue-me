package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Equipe;
import com.segue.repository.EquipeRepository;
import com.segue.util.jpa.Transactional;

public class CadastroEquipeService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EquipeRepository repository;

	@Transactional
	public Equipe salvar(Equipe equipe) throws NegocioException {
		Equipe equipeExistente = repository.findNomeSigle(equipe.getTitulo());
		if (equipeExistente != null && !equipeExistente.equals(equipe)) {
			throw new NegocioException("Já existe um equipe com o titulo informado.");
		}
		
		Equipe equipeExistenteOrdem = repository.findOrdemSigle(equipe.getOrdem());
		if (equipeExistenteOrdem != null && !equipeExistenteOrdem.equals(equipe)) {
			throw new NegocioException("Já existe um equipe nessa ORDEM informada.");
		}
		return repository.guardar(equipe);
	}

	@Transactional
	public void remover(Equipe equipe) throws NegocioException {
		repository.remover(equipe);
	}

}
