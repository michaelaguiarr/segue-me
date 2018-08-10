package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.segue.model.CorCirculo;
import com.segue.model.Endereco;
import com.segue.model.Padre;
import com.segue.model.Paroquia;
import com.segue.model.SituacaoSeguidor;
import com.segue.model.Enum.Estados;
import com.segue.repository.ParoquiaRepository;
import com.segue.service.CadastroPadreService;
import com.segue.service.FotoService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroPadreBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroPadreService service;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private FotoService fotoService;

	private Padre padre;
	private String imagemDoUsuario;

	private List<Paroquia> listaParoquiaSeguidor = new ArrayList<>();

	public CadastroPadreBean() {
		limpar();
	}

	public void inicializar() {
		if (this.padre == null) {
			limpar();
		} else {
			imagemDoUsuario = fotoService.recuperarViaByte(this.padre.getImagem(), "padre", this.padre.getId());
		}
		this.listaParoquiaSeguidor = paroquiaRepository.listaParoquias();
	}

	private void limpar() {
		padre = new Padre();
		padre.setEndereco(new Endereco());
		this.listaParoquiaSeguidor = new ArrayList<>();
		this.imagemDoUsuario = null;
	}

	public void salvar() throws IOException {
		try {
			this.padre = service.salvar(padre);
			limpar();
			FacesUtil.addInfoMessage("Padre/Bispo salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.padre);
			FacesUtil.addInfoMessage("Padre/Bispo " + this.padre.getNome() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void upload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			padre.setImagem(fotoFile);
			padre.setExibirImagem(true);
			imagemDoUsuario = fotoService.recuperarViaByte(this.padre.getImagem(), "padre", this.padre.getId());
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFoto() {
		try {
			fotoService.deletarTemp(this.padre.getId(), "padre");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		padre.setImagem(null);
		padre.setExibirImagem(false);
		imagemDoUsuario = null;
	}

	public boolean isEditando() {
		return this.padre.getId() != null;
	}

	public SituacaoSeguidor[] getSituacaoSeguidor() {
		return SituacaoSeguidor.values();
	}

	public Estados[] getEstados() {
		return Estados.values();
	}

	public CorCirculo[] getCorCirculo() {
		return CorCirculo.values();
	}

	public List<Paroquia> getListaParoquiaSeguidor() {
		return listaParoquiaSeguidor;
	}

	public void setListaParoquiaSeguidor(List<Paroquia> listaParoquiaSeguidor) {
		this.listaParoquiaSeguidor = listaParoquiaSeguidor;
	}

	public String getImagemDoUsuario() {
		return imagemDoUsuario;
	}

	public void setImagemDoUsuario(String imagemDoUsuario) {
		this.imagemDoUsuario = imagemDoUsuario;
	}

	public Padre getPadre() {
		return padre;
	}

	public void setPadre(Padre padre) {
		this.padre = padre;
	}

}
