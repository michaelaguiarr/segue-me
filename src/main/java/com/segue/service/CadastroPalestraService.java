package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Palestra;
import com.segue.repository.PalestraRepository;
import com.segue.util.jpa.Transactional;

public class CadastroPalestraService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PalestraRepository repository;

	@Transactional
	public Palestra salvar(Palestra palestra) throws NegocioException {
		Palestra palestraExistente = repository.findNomeSigle(palestra.getNome());
		if (palestraExistente != null && !palestraExistente.equals(palestra)) {
			throw new NegocioException("Já existe um Palestra com esse TITULO informado.");
		}
		
		Palestra palestraOrdem = repository.findOrdem(palestra.getOrdem());
		if (palestraOrdem != null && !palestraOrdem.equals(palestra)) {
			throw new NegocioException("Já existe um Palestra com ness ORDEM informada.");
		}
		return repository.guardar(palestra);
	}

	@Transactional
	public void remover(Palestra palestra) throws NegocioException {
		repository.remover(palestra);
	}

}
