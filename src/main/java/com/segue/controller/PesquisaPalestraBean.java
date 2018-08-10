package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Palestra;
import com.segue.repository.PalestraRepository;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaPalestraBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PalestraRepository repository;

	private Palestra palestraSelecionado;

	private List<Palestra> listaPalestra;

	private String searchValue;

	public PesquisaPalestraBean() {
		palestraSelecionado = new Palestra();
	}

	public void inicializar() {
		listaPalestra = repository.listaAll();
	}

	/**
	 * Pesquisa palestra pelo titulo
	 */
	public void pesquisar() {
		listaPalestra = repository.findByNome(searchValue);

		if (listaPalestra.isEmpty()) {
			FacesUtil.addErrorMessage("Palestra '" + getSearchValue() + "' não encontrado");
		}
	}

	/**
	 * Seleciona palestra na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext().redirect(
				Constants.CONTEXT + "/palestra/cadastro-palestra.xhtml?palestra=" + palestraSelecionado.getId());
	}

	public Palestra getPalestraSelecionado() {
		return palestraSelecionado;
	}

	public void setPalestraSelecionado(Palestra palestraSelecionado) {
		this.palestraSelecionado = palestraSelecionado;
	}

	public List<Palestra> getListaPalestra() {
		return listaPalestra;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

}
