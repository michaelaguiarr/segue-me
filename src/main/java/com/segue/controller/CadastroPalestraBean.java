package com.segue.controller;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Palestra;
import com.segue.service.CadastroPalestraService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroPalestraBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroPalestraService service;

	private Palestra palestra;

	public CadastroPalestraBean() {
		limpar();
	}

	public void inicializar() {
		if (this.palestra == null) {
			limpar();
		}
	}

	private void limpar() {
		palestra = new Palestra();
	}

	public void salvar() {
		try {
			this.palestra = service.salvar(palestra);
			limpar();
			FacesUtil.addInfoMessage("Palestra salva com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.palestra);
			FacesUtil.addInfoMessage("Palestra " + this.palestra.getNome() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public boolean isEditando() {
		return this.palestra.getId() != null;
	}

	public Palestra getPalestra() {
		return palestra;
	}

	public void setPalestra(Palestra palestra) {
		this.palestra = palestra;
	}

}
