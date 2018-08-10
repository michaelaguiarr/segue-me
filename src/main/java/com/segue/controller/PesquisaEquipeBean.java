package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Equipe;
import com.segue.repository.EquipeRepository;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaEquipeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EquipeRepository service;

	private Equipe equipeSelecionado;

	private List<Equipe> listaEquipe;

	private String searchValue;

	public PesquisaEquipeBean() {
		equipeSelecionado = new Equipe();
	}

	public void inicializar() {
		listaEquipe = service.listaALL();
	}

	/**
	 * Pesquisa equipe pelo titulo
	 */
	public void pesquisar() {
		listaEquipe = service.findByNome(searchValue);

		if (listaEquipe.isEmpty()) {
			FacesUtil.addErrorMessage("Equipe '" + getSearchValue() + "' não encontrado");
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
				.redirect(Constants.CONTEXT + "/equipe/cadastro-equipe.xhtml?equipe=" + equipeSelecionado.getId());
	}

	public Equipe getEquipeSelecionado() {
		return equipeSelecionado;
	}

	public void setEquipeSelecionado(Equipe equipeSelecionado) {
		this.equipeSelecionado = equipeSelecionado;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

	public List<Equipe> getListaEquipe() {
		return listaEquipe;
	}

}
