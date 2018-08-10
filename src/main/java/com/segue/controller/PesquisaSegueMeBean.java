package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.SegueMe;
import com.segue.repository.SegueMeRepository;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaSegueMeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SegueMeRepository service;

	private SegueMe segueMeSelecionado;

	private List<SegueMe> listaSegueMe;

	private String searchValue;

	public PesquisaSegueMeBean() {
		segueMeSelecionado = new SegueMe();
	}

	public void inicializar() {
		listaSegueMe = service.listaEjcs();
	}

	/**
	 * Pesquisa unidade pelo nome
	 */
	public void pesquisar() {
		listaSegueMe = service.findByParoquiaAndTitulo(searchValue);

		if (listaSegueMe.isEmpty()) {
			FacesUtil.addErrorMessage("Segue-me '" + getSearchValue() + "' não encontrado");
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
				Constants.CONTEXT + "/segueMe/cadastro-segue-me.xhtml?segue-me=" + segueMeSelecionado.getId());
	}

	public SegueMe getSegueMeSelecionado() {
		return segueMeSelecionado;
	}

	public void setSegueMeSelecionado(SegueMe segueMeSelecionado) {
		this.segueMeSelecionado = segueMeSelecionado;
	}

	public List<SegueMe> getListaSegueMe() {
		return listaSegueMe;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

}
