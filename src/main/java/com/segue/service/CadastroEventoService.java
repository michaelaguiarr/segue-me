package com.segue.service;

import java.io.Serializable;
import java.text.ParseException;

import javax.inject.Inject;

import com.segue.model.Evento;
import com.segue.model.Paroquia;
import com.segue.model.SituacaoSeguidor;
import com.segue.model.StatusInscricao;
import com.segue.repository.EventoRepository;
import com.segue.util.CalcularIdade;
import com.segue.util.jpa.Transactional;

public class CadastroEventoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Transactional
	public Evento salvar(Evento inscricao) throws NegocioException {
		if (!inscricao.getInscricao().getStatusInscricao().equals(StatusInscricao.PENDENTE)) {

			inscricao.getSeguidor().setCurso(inscricao.getInscricao().getCurso());
			inscricao.getSeguidor().setMovimentos(inscricao.getInscricao().getMovimentos());
			inscricao.getSeguidor().setEscolaridade(inscricao.getInscricao().getEscolaridade());
			inscricao.getSeguidor().setReligiao(inscricao.getInscricao().getReligiao());
			inscricao.getSeguidor().setSagramentoBatismo(inscricao.getInscricao().getSagramentoBatismo());
			inscricao.getSeguidor().setSagramentoCrisma(inscricao.getInscricao().getSagramentoCrisma());
			inscricao.getSeguidor().setSagramentoEucaristia(inscricao.getInscricao().getSagramentoEucaristia());

			inscricao.getInscricao().setInstituicao(inscricao.getSeguidor().getInstituicao());
			inscricao.getInscricao().setSegueMe(inscricao.getSegueMe());
			inscricao.getInscricao().setParoquia(inscricao.getSegueMe().getParoquia());
			inscricao.getInscricao().setEndereco(inscricao.getSeguidor().getEndereco());
			inscricao.getInscricao().setSituacaoEscolaridade(inscricao.getSeguidor().getSituacaoEscolaridade());
			inscricao.getInscricao().setInstituicao(inscricao.getSeguidor().getInstituicao());
			if (inscricao.getInscricao().getStatusInscricao().equals(StatusInscricao.APROVADO)) {
				inscricao.getSeguidor().setSituacaoSeguidor(SituacaoSeguidor.ATIVO);
				inscricao.getSeguidor().setParoquia(inscricao.getInscricao().getParoquia());
				inscricao.getSeguidor().setSegueMe(inscricao.getInscricao().getSegueMe());
			}
			try {
				inscricao.setIdade(CalcularIdade.calculaIdadeSegueMe(inscricao.getSeguidor().getDataNascimento(),
						inscricao.getSegueMe().getDtFim()));
			} catch (ParseException e) {
				throw new NegocioException("Idade / Data de nascimento invalida.");
			}
		}
		return repository.guardar(inscricao);
	}

	@Transactional
	public void remover(Evento inscricao) throws NegocioException {
		repository.remover(inscricao);
	}

	public Evento carregarFicha(Integer id, Paroquia paroquia) throws NegocioException {
		return repository.findByIdInscrito(id, paroquia);
	}

}
