package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Circulo;
import com.segue.model.Evento;
import com.segue.repository.CirculoRepository;
import com.segue.repository.EventoRepository;
import com.segue.util.jpa.Transactional;

public class CadastroCirculoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CirculoRepository repository;

	@Inject
	private EventoRepository eventoRepository;
	
	@Inject
	private CadastroSeguidorService cadastroSeguidorService;

	@Transactional
	public Circulo salvar(Circulo circulo) throws NegocioException {
		Circulo corExistente = repository.findByCorSegueMe(circulo.getSegueMe(), circulo.getCorCirculo());
		if (corExistente != null && !corExistente.equals(circulo)) {
			throw new NegocioException("Já existe um círculo com está cor informado.");
		}
		circulo = repository.guardar(circulo);
		for (Evento e : circulo.getEventos()) {
			if (e.getCasal() == null) {
				e.setCirculo(circulo);
				if (e.getFuncao() != null) {
					e.getSeguidor().setCirculo(circulo);
					cadastroSeguidorService.salvar(e.getSeguidor());
				}
				eventoRepository.guardar(e);
			}
		}
		return circulo;
	}

	@Transactional
	public void remover(Circulo circulo) throws NegocioException {
		for (Evento e : circulo.getEventos()) {
			if (e.getCasal() == null || e.getFuncao() == null) {
				e.setCirculo(null);
				e.getSeguidor().setCirculo(null);
				eventoRepository.guardar(e);
			}
		}
		repository.remover(circulo);
	}

	@Transactional
	public void removerSeguimista(Evento e) throws NegocioException {
		if (e.getCasal() == null || e.getFuncao() == null) {
			e.setCirculo(null);
			e.getSeguidor().setCirculo(null);
			eventoRepository.guardar(e);
		}
	}

}
