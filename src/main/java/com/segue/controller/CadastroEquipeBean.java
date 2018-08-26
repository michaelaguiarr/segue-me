package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.segue.model.Equipe;
import com.segue.service.CadastroEquipeService;
import com.segue.service.FotoService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEquipeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroEquipeService service;

	@Inject
	private FotoService fotoService;

	private Equipe equipe;
	private String imagem;

	public CadastroEquipeBean() {
		limpar();
	}

	public void inicializar() {
		if (this.equipe == null) {
			limpar();
		} else {
			imagem = fotoService.recuperarViaByte(this.equipe.getImagem(), "equipe", this.equipe.getId());
		}
	}

	private void limpar() {
		equipe = new Equipe();
		this.imagem = null;
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

	public void upload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			equipe.setImagem(fotoFile);
			imagem = fotoService.recuperarViaByte(this.equipe.getImagem(), "equipe", this.equipe.getId());
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFoto() {
		try {
			fotoService.deletarTemp(this.equipe.getId(), "equipe");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		equipe.setImagem(null);
		imagem = null;
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

	public String getImagem() {
		return imagem;
	}

	public void setImagem(String imagem) {
		this.imagem = imagem;
	}

}
