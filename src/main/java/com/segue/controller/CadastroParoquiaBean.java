package com.segue.controller;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Paroquia;
import com.segue.model.Enum.Estados;
import com.segue.service.CadastroParoquiaService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroParoquiaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroParoquiaService cadastroParoquiaService;

	private Paroquia paroquia;

	public CadastroParoquiaBean() {
		limpar();
	}

	public void inicializar() {
		if (this.paroquia == null) {
			limpar();
		}
	}

	private void limpar() {
		paroquia = new Paroquia();
	}

	public void salvar() {
		try {
			this.paroquia = cadastroParoquiaService.salvar(this.paroquia);
			limpar();
			FacesUtil.addInfoMessage("Paróquia salva com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			cadastroParoquiaService.remover(this.paroquia);
			FacesUtil.addInfoMessage("Paróquia " + this.paroquia.getDescricao() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public Estados[] getEstados() {
		return Estados.values();
	}

	public boolean isEditando() {
		return this.paroquia.getId() != null;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

}
