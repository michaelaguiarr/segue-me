package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.ECC;
import com.segue.repository.ECCRepository;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaEccBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ECCRepository repository;

	private ECC eccSelecionado;

	private List<ECC> listaEcc;

	private String searchValue;

	public PesquisaEccBean() {
		eccSelecionado = new ECC();
	}

	public void inicializar() {
		listaEcc = repository.listaAll();
	}

	/**
	 * Pesquisa unidade pelo nome
	 */
	public void pesquisar() {
		listaEcc = repository.findByParoquiaAndTitulo(searchValue);

		if (listaEcc.isEmpty()) {
			FacesUtil.addErrorMessage("ECC '" + getSearchValue() + "' não encontrado");
		}
	}

	/**
	 * Seleciona ecc na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext().redirect(
				Constants.CONTEXT + "/ecc/cadastro-ecc.xhtml?ecc=" + eccSelecionado.getId());
	}

	public ECC getEccSelecionado() {
		return eccSelecionado;
	}

	public void setEccSelecionado(ECC eccSelecionado) {
		this.eccSelecionado = eccSelecionado;
	}

	public List<ECC> getListaEcc() {
		return listaEcc;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

}
