package com.segue.service;

import java.io.Serializable;
import java.text.ParseException;

import javax.inject.Inject;

import com.segue.model.Evento;
import com.segue.model.StatusConvite;
import com.segue.repository.EventoRepository;
import com.segue.util.CalcularIdade;
import com.segue.util.jpa.Transactional;

public class CadastroPalestraConvidadoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Transactional
	public Evento salvar(Evento evento) throws NegocioException {
		Evento eventoJaExiste = repository.findByConvidadoEventoPalestra(evento.getPalestranteConvidado(), evento.getSegueMe(),
				evento.getPalestra());
		if (eventoJaExiste != null && !eventoJaExiste.equals(evento)) {
			throw new NegocioException("Convidado já cadastrado na Palestra!");
		}
		if (evento.getSegueMe().getDtFim() != null) {
			try {
				evento.setIdade(CalcularIdade.calculaIdadeSegueMe(evento.getPalestranteConvidado().getDataNascimento(),
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
