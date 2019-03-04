package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.segue.model.Endereco;
import com.segue.model.PalestranteConvidado;
import com.segue.model.Sexo;
import com.segue.repository.SexoRepository;
import com.segue.service.CadastroPalestranteConvidadoService;
import com.segue.service.FotoService;
import com.segue.service.NegocioException;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroConvidadoPalestraBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroPalestranteConvidadoService service;

	@Inject
	private SexoRepository sexoRepository;

	@Inject
	private FotoService fotoService;

	private PalestranteConvidado palestranteConvidado;
	private String imagemDoUsuario;

	private List<Sexo> listaSexo = new ArrayList<>();

	public CadastroConvidadoPalestraBean() {
		limpar();
	}

	public void inicializar() {
		if (this.palestranteConvidado == null) {
			limpar();
		} else {
			imagemDoUsuario = fotoService.recuperarViaByte(this.palestranteConvidado.getImagem(),
					"palestranteConvidado", this.palestranteConvidado.getId());
		}
		this.listaSexo = sexoRepository.findAll();
	}

	private void limpar() {
		palestranteConvidado = new PalestranteConvidado();
		palestranteConvidado.setEndereco(new Endereco());
		this.imagemDoUsuario = null;
	}

	public void salvar() throws IOException {
		try {
			this.palestranteConvidado = service.salvar(palestranteConvidado);
			limpar();
			FacesUtil.addInfoMessage("Palestrante Convidado salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.palestranteConvidado);
			FacesUtil.addInfoMessage(
					"Palestrante Convidado " + this.palestranteConvidado.getNome() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void upload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			palestranteConvidado.setImagem(fotoFile);
			palestranteConvidado.setExibirImagem(true);
			imagemDoUsuario = fotoService.recuperarViaByte(this.palestranteConvidado.getImagem(),
					"palestranteConvidado", this.palestranteConvidado.getId());
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFoto() {
		try {
			fotoService.deletarTemp(this.palestranteConvidado.getId(), "palestranteConvidado");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		palestranteConvidado.setImagem(null);
		palestranteConvidado.setExibirImagem(false);
		imagemDoUsuario = null;
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void limparTela() throws IOException {
		limpar();
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/palestrante-public/cadastro-convidado.xhtml");
	}

	public boolean isEditando() {
		return this.palestranteConvidado.getId() != null;
	}

	public List<Sexo> getListaSexo() {
		return listaSexo;
	}

	public void setListaSexo(List<Sexo> listaSexo) {
		this.listaSexo = listaSexo;
	}

	public String getImagemDoUsuario() {
		return imagemDoUsuario;
	}

	public void setImagemDoUsuario(String imagemDoUsuario) {
		this.imagemDoUsuario = imagemDoUsuario;
	}

	public PalestranteConvidado getPalestranteConvidado() {
		return palestranteConvidado;
	}

	public void setPalestranteConvidado(PalestranteConvidado palestranteConvidado) {
		this.palestranteConvidado = palestranteConvidado;
	}

}
