package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Circulo;
import com.segue.repository.CirculoRepository;
import com.segue.util.jpa.Transactional;

public class CadastroCirculoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CirculoRepository repository;

	@Transactional
	public Circulo salvar(Circulo circulo) throws NegocioException {
		Circulo corExistente = repository.findByCorSegueMe(circulo.getSegueMe(), circulo.getCorCirculo());
		if (corExistente != null && !corExistente.equals(circulo)) {
			throw new NegocioException("Já existe um círculo com está cor informado.");
		}
		return repository.guardar(circulo);
	}

	@Transactional
	public void remover(Circulo circulo) throws NegocioException {
		repository.remover(circulo);
	}

}
