package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Paroquia;
import com.segue.repository.ParoquiaRepository;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaParoquiaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ParoquiaRepository service;

	private Paroquia paroquiaSelecionado;

	private List<Paroquia> listaParoquia;

	private String searchValue;

	public PesquisaParoquiaBean() {
		paroquiaSelecionado = new Paroquia();
	}

	public void inicializar() {
		listaParoquia = service.listaParoquias();
	}

	public void pesquisar() {
		listaParoquia = service.findByNome(searchValue);

		if (listaParoquia.isEmpty()) {
			FacesUtil.addErrorMessage("Paraoquia '" + getSearchValue() + "' não encontrado");
		}
	}

	/**
	 * Seleciona segue-me na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext().redirect(
				Constants.CONTEXT + "/paroquia/cadastro-paroquia.xhtml?paroquia=" + paroquiaSelecionado.getId());
	}

	public Paroquia getParoquiaSelecionado() {
		return paroquiaSelecionado;
	}

	public void setParoquiaSelecionado(Paroquia paroquiaSelecionado) {
		this.paroquiaSelecionado = paroquiaSelecionado;
	}

	public List<Paroquia> getListaParoquia() {
		return listaParoquia;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

}
