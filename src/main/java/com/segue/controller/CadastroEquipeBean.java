package com.segue.controller;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Equipe;
import com.segue.service.CadastroEquipeService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEquipeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroEquipeService service;

	private Equipe equipe;

	public CadastroEquipeBean() {
		limpar();
	}

	public void inicializar() {
		if (this.equipe == null) {
			limpar();
		}
	}

	private void limpar() {
		equipe = new Equipe();
	}

	public void salvar() {
		try {
			this.equipe = service.salvar(equipe);
			limpar();
			FacesUtil.addInfoMessage("Equipe salva com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.equipe);
			FacesUtil.addInfoMessage("Equipe " + this.equipe.getTitulo() + " excluída com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public boolean isEditando() {
		return this.equipe.getId() != null;
	}

	public Equipe getEquipe() {
		return equipe;
	}

	public void setEquipe(Equipe equipe) {
		this.equipe = equipe;
	}

}
