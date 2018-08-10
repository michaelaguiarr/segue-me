package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Funcao;
import com.segue.repository.FuncaoRepository;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaFuncaoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FuncaoRepository service;

	private Funcao funcaoSelecionado;

	private List<Funcao> listaFuncao;

	private String searchValue;

	public PesquisaFuncaoBean() {
		funcaoSelecionado = new Funcao();
	}

	public void inicializar() {
		listaFuncao = service.listaALL();
	}

	/**
	 * Pesquisa equipe pelo titulo
	 */
	public void pesquisar() {
		listaFuncao = service.findByNome(searchValue);

		if (listaFuncao.isEmpty()) {
			FacesUtil.addErrorMessage("Função '" + getSearchValue() + "' não encontrado");
		}
	}

	/**
	 * Seleciona segue-me na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/funcao/cadastro-funcao.xhtml?funcao=" + funcaoSelecionado.getId());
	}

	public Funcao getFuncaoSelecionado() {
		return funcaoSelecionado;
	}

	public void setFuncaoSelecionado(Funcao funcaoSelecionado) {
		this.funcaoSelecionado = funcaoSelecionado;
	}

	public List<Funcao> getListaFuncao() {
		return listaFuncao;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

}
