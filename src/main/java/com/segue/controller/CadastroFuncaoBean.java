package com.segue.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Equipe;
import com.segue.model.Funcao;
import com.segue.repository.EquipeRepository;
import com.segue.service.CadastroFuncaoService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroFuncaoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroFuncaoService service;

	@Inject
	private EquipeRepository equipeRepository;

	private Funcao funcao;

	private List<Equipe> equipes;

	public CadastroFuncaoBean() {
		limpar();
	}

	public void inicializar() {
		if (this.funcao == null) {
			limpar();
		}
		this.equipes = equipeRepository.listaALL();
	}

	private void limpar() {
		funcao = new Funcao();
	}

	public void salvar() {
		try {
			this.funcao = service.salvar(funcao);
			limpar();
			FacesUtil.addInfoMessage("Função salva com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.funcao);
			FacesUtil.addInfoMessage("Funcao " + this.funcao.getDescricao() + " excluída com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public boolean isEditando() {
		return this.funcao.getId() != null;
	}

	public Funcao getFuncao() {
		return funcao;
	}

	public void setFuncao(Funcao funcao) {
		this.funcao = funcao;
	}

	public List<Equipe> getEquipes() {
		return equipes;
	}

}
