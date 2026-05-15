package com.segue.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.segue.model.Seguidor;
import com.segue.repository.SeguidorRepository;
import com.segue.util.jpa.Transactional;

public class CadastroSeguidorService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SeguidorRepository repository;

	@Transactional
	public Seguidor salvar(Seguidor seguidor) throws NegocioException {
		Seguidor seguidorExiste = repository.findByNomeDtNascimento(seguidor.getNome(),
				seguidor.getDataNascimento());
		if (seguidorExiste != null && !seguidorExiste.equals(seguidor)) {
			throw new NegocioException("Seguidor já cadastrado no sistema!Por favor faça uma pesquisa");
		}
//		if (seguidor.getImagem() == null) {
//			throw new NegocioException("Informe a foto!");
//		}
		seguidor.setNomeSemAcento(seguidor.getNome());
		return repository.guardar(seguidor);
	}
	
	@Transactional
	public Seguidor atualizarNome(Seguidor seguidor) throws NegocioException {
		seguidor.setNomeSemAcento(seguidor.getNome());
		return repository.guardar(seguidor);
	}

	@Transactional
	public void remover(Seguidor seguidor) throws NegocioException {
		repository.remover(seguidor);
	}

	
	/**
	 * Busca seguidor por nome
	 * 
	 * @return
	 */
	public List<Seguidor> findByNome(String nome) {
		return repository.findByNome(nome);
	}
	
	/**
	 * Busca seguidor por nome para fichas
	 * 
	 * @return
	 */
	public List<Seguidor> findByNomeFicha(String nome) {
		return repository.findByNomeFicha(nome);
	}
}
