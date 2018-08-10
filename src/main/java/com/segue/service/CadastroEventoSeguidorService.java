package com.segue.service;

import java.io.Serializable;
import java.text.ParseException;

import javax.inject.Inject;

import com.segue.model.Evento;
import com.segue.model.StatusConvite;
import com.segue.repository.EventoRepository;
import com.segue.util.CalcularIdade;
import com.segue.util.jpa.Transactional;

public class CadastroEventoSeguidorService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Transactional
	public Evento salvar(Evento evento) throws NegocioException {
		Evento eventoJaExiste = repository.findBySeguidorEventoEquipe(evento.getSeguidor(), evento.getSegueMe(),
				evento.getEquipe());
		if (eventoJaExiste != null && !eventoJaExiste.equals(evento)) {
			throw new NegocioException("Seguidor já cadastrado na Equipe!");
		}
		if (evento.getSegueMe().getDtFim() != null) {
			try {
				evento.setIdade(CalcularIdade.calculaIdadeSegueMe(evento.getSeguidor().getDataNascimento(),
						evento.getSegueMe().getDtFim()));
			} catch (ParseException e) {
				throw new NegocioException("Idade / Data de nascimento invalida.");
			}
		} else {
			throw new NegocioException("Informe a data do Segue-me!");
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
