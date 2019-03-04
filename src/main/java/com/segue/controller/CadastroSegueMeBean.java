package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.segue.model.NumeroRomano;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Enum.Estados;
import com.segue.repository.NumeroRomanoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.service.CadastroEjcService;
import com.segue.service.FotoService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroSegueMeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroEjcService cadastroEjcService;

	@Inject
	private ParoquiaRepository paroquias;

	@Inject
	private NumeroRomanoRepository numeroRomanoRepoditory;

	@Inject
	private FotoService fotoService;

	private SegueMe segueMe;
	private String imagem;
	private String imagemFundo;
	private String imagemRoda;

	private List<Paroquia> paroquiasFiltro;

	private List<NumeroRomano> numeroRomanosFiltro;

	public CadastroSegueMeBean() {
		limpar();
	}

	public void inicializar() {
		if (this.segueMe == null) {
			limpar();
		} else {
			Integer id = Integer.valueOf(this.segueMe.getId().toString());
			imagem = fotoService.recuperarViaByte(this.segueMe.getImagem(), "segueMe", id);
			imagemFundo = fotoService.recuperarViaByte(this.segueMe.getImagemFundo(), "segueMeFundo", id);
			imagemRoda = fotoService.recuperarViaByte(this.segueMe.getImagemRoda(), "segueMeRoda", id);
		}
		paroquiasFiltro = paroquias.listaParoquias();
		numeroRomanosFiltro = numeroRomanoRepoditory.listaNumerosRomanos();

	}

	private void limpar() {
		segueMe = new SegueMe();
		imagem = null;
		imagemFundo = null;
		imagemRoda = null;
	}

	public void salvar() {
		try {
			this.segueMe = cadastroEjcService.salvar(this.segueMe);
			limpar();
			FacesUtil.addInfoMessage("Evento Segue-me salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			cadastroEjcService.remover(this.segueMe);
			FacesUtil.addInfoMessage("Evento " + this.segueMe.getTitulo() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void upload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			segueMe.setImagem(fotoFile);
			Integer id = Integer.valueOf(this.segueMe.getId().toString());
			imagem = fotoService.recuperarViaByte(this.segueMe.getImagem(), "segueMe", id);
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFoto() {
		try {
			Integer id = Integer.valueOf(this.segueMe.getId().toString());
			fotoService.deletarTemp(id, "segueMe");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		segueMe.setImagem(null);
		imagem = null;
	}

	public void uploadFundo(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			segueMe.setImagemFundo(fotoFile);
			Integer id = Integer.valueOf(this.segueMe.getId().toString());
			imagemFundo = fotoService.recuperarViaByte(this.segueMe.getImagemFundo(), "segueMeFundo", id);
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFotoFundo() {
		try {
			Integer id = Integer.valueOf(this.segueMe.getId().toString());
			fotoService.deletarTemp(id, "segueMeFundo");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		segueMe.setImagemFundo(null);
		imagemFundo = null;
	}

	public void uploadRoda(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			segueMe.setImagemRoda(fotoFile);
			Integer id = Integer.valueOf(this.segueMe.getId().toString());
			imagemRoda = fotoService.recuperarViaByte(this.segueMe.getImagemFundo(), "segueMeRoda", id);
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFotoRoda() {
		try {
			Integer id = Integer.valueOf(this.segueMe.getId().toString());
			fotoService.deletarTemp(id, "segueMeRoda");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		segueMe.setImagemRoda(null);
		imagemRoda = null;
	}

	public Estados[] getEstados() {
		return Estados.values();
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	public List<Paroquia> getParoquiasFiltro() {
		return paroquiasFiltro;
	}

	public List<NumeroRomano> getNumeroRomanosFiltro() {
		return numeroRomanosFiltro;
	}

	public String getImagem() {
		return imagem;
	}

	public void setImagem(String imagem) {
		this.imagem = imagem;
	}

	public String getImagemFundo() {
		return imagemFundo;
	}

	public void setImagemFundo(String imagemFundo) {
		this.imagemFundo = imagemFundo;
	}

	public boolean isEditando() {
		return this.segueMe.getId() != null;
	}

	public String getImagemRoda() {
		return imagemRoda;
	}

	public void setImagemRoda(String imagemRoda) {
		this.imagemRoda = imagemRoda;
	}

}
