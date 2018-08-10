package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Evento;
import com.segue.model.StatusConvite;
import com.segue.repository.EventoRepository;
import com.segue.util.jpa.Transactional;

public class CadastroPalestraPadreService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Transactional
	public Evento salvar(Evento evento) throws NegocioException {
		Evento eventoJaExiste = repository.findByPadreEventoPalestra(evento.getPadre(), evento.getSegueMe(),
				evento.getPalestra());
		if (eventoJaExiste != null && !eventoJaExiste.equals(evento)) {
			throw new NegocioException("Padre/Bispo já cadastrado na Palestra!");
		}
		if (evento.getId() == null) {
			evento.setStatusConvite(StatusConvite.PARTICIPANDO);
		}

		return repository.guardar(evento);
	}

	@Transactional
	public void remover(Evento equipe) throws NegocioException {
		repository.remover(equipe);
	}

}
