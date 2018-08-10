package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Evento;
import com.segue.model.Inscricao;
import com.segue.model.SegueMe;
import com.segue.model.StatusInscricao;
import com.segue.model.Usuario;
import com.segue.repository.EventoRepository;
import com.segue.util.jpa.Transactional;

public class CadastroEventoInscritoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Transactional
	public void salvar(Integer numero, SegueMe segueMe, Usuario usuario) throws NegocioException {
		if (numero <= 0) {
			throw new NegocioException("Informe um valor maior que 0 (zero)!");
		}
		for (int i = 1; i <= numero; i++) {
			Evento evento = new Evento();
			evento.setInscricao(new Inscricao());
			evento.setSegueMe(segueMe);
			evento.setUsuario(usuario);
			evento.getInscricao().setStatusInscricao(StatusInscricao.PENDENTE);
			repository.guardar(evento);
		}
	}

	@Transactional
	public void remover(Evento equipe) throws NegocioException {
		repository.remover(equipe);
	}

}
