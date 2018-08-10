package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.Funcao;
import com.segue.repository.FuncaoRepository;
import com.segue.util.jpa.Transactional;

public class CadastroFuncaoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FuncaoRepository repository;

	@Transactional
	public Funcao salvar(Funcao funcao) throws NegocioException {
		Funcao funcaoExistente = repository.findNomeSigle(funcao.getDescricao(), funcao.getEquipe());
		if (funcaoExistente != null && !funcaoExistente.equals(funcao)) {
			throw new NegocioException("Já existe um função com essa DESCRICAO informado.");
		}
		Funcao funcaoOrdem = repository.findEquipeOrdem(funcao.getEquipe(), funcao.getOrdem());
		if (funcaoOrdem != null && !funcaoOrdem.equals(funcao)) {
			throw new NegocioException("Já existe um função nessa ordem informada.");
		}
		return repository.guardar(funcao);
	}

	@Transactional
	public void remover(Funcao funcao) throws NegocioException {
		repository.remover(funcao);
	}

}
