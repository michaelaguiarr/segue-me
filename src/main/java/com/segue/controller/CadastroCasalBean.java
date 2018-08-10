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

import com.segue.model.Casal;
import com.segue.model.ECC;
import com.segue.model.Endereco;
import com.segue.model.NumeroRomano;
import com.segue.model.Paroquia;
import com.segue.model.Sexo;
import com.segue.model.Enum.Estados;
import com.segue.repository.ECCRepository;
import com.segue.repository.NumeroRomanoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SexoRepository;
import com.segue.service.CadastroCasalService;
import com.segue.service.CadastroEccService;
import com.segue.service.FotoService;
import com.segue.service.NegocioException;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroCasalBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroCasalService service;

	@Inject
	private CadastroEccService cadastroEccService;

	@Inject
	private ECCRepository eccRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private SexoRepository sexoRepository;

	@Inject
	private NumeroRomanoRepository numeroRomanoRepoditory;

	@Inject
	private FotoService fotoService;

	private Paroquia paroquiaEcc;
	private Paroquia paroquia;
	private Casal casal;
	private ECC ecc;
	private String imagemEle;
	private String imagemEla;

	private List<Paroquia> listaParoquia = new ArrayList<>();
	private List<Paroquia> listaParoquiaEcc = new ArrayList<>();
	private List<Sexo> listaSexo = new ArrayList<>();
	private List<ECC> listaEcc = new ArrayList<>();
	private List<NumeroRomano> numeroRomanosFiltro = new ArrayList<>();

	public CadastroCasalBean() {
		limpar();
	}

	public void inicializar() {
		this.listaParoquia = paroquiaRepository.listaParoquias();
		if (this.casal == null) {
			limpar();
		} else {
			if (this.casal.getEcc() != null) {
				this.paroquia = this.casal.getEcc().getParoquia();
				this.listaEcc = eccRepository.findByParoquia(this.casal.getEcc().getParoquia());
			}
			imagemEle = fotoService.recuperarViaByte(this.casal.getImagemELE(), "casalEle", this.casal.getId());
			imagemEla = fotoService.recuperarViaByte(this.casal.getImagemELA(), "casalEla", this.casal.getId());
		}
		this.listaSexo = sexoRepository.findAll();
	}

	private void limpar() {
		paroquiaEcc = new Paroquia();
		paroquia = new Paroquia();
		casal = new Casal();
		casal.setEndereco(new Endereco());
		this.imagemEla = null;
		this.imagemEle = null;
		ecc = new ECC();

	}

	public void limparEcc() {
		this.ecc = new ECC();
		this.paroquiaEcc = new Paroquia();
		numeroRomanosFiltro = numeroRomanoRepoditory.listaNumerosRomanos();
		this.listaParoquiaEcc = paroquiaRepository.listaParoquias();
	}

	public void salvar() throws IOException {
		try {
			this.casal = service.salvar(casal);
			limpar();
			FacesUtil.addInfoMessage("Casal salva com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void salvarEcc() throws IOException {
		try {
			this.ecc = cadastroEccService.salvar(ecc);
			limparEcc();
			FacesUtil.addInfoMessage("ECC salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}
	
	public void excluir() {
		try {
			service.remover(this.casal);
			FacesUtil.addInfoMessage("Casal " + this.casal.getApelidoEle()+ " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void uploadEle(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			casal.setImagemELE(fotoFile);
			casal.setExibirImagemEle(true);
			imagemEle = fotoService.recuperarViaByte(this.casal.getImagemELE(), "casalEle", this.casal.getId());
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void uploadEla(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			casal.setImagemELA(fotoFile);
			casal.setExibirImagemEla(true);
			imagemEla = fotoService.recuperarViaByte(this.casal.getImagemELA(), "casalEla", this.casal.getId());
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFotoEla() {
		try {
			fotoService.deletarTemp(this.casal.getId(), "casalEla");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		casal.setImagemELA(null);
		casal.setExibirImagemEla(false);
		imagemEla = null;
	}

	public void removerFotoEle() {
		try {
			fotoService.deletarTemp(this.casal.getId(), "casalEle");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		casal.setImagemELE(null);
		casal.setExibirImagemEle(false);
		imagemEle = null;
	}

	/**
	 * Carregar Segue-me Seguidor;
	 */
	public void onEccChange() {
		if (this.paroquia != null) {
			listaEcc = eccRepository.findByParoquia(this.paroquia);
			this.casal.setEcc(null);
		}
	}
	
	/**
	 * Seleciona bairro na lista
	 * 
	 * @throws IOException
	 */
	public void limparTela() throws IOException {
		limpar();
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/casal-public/cadastro-casal.xhtml");
	}

	public boolean isEditando() {
		return this.casal.getId() != null;
	}

	public Estados[] getEstados() {
		return Estados.values();
	}

	public List<Sexo> getListaSexo() {
		return listaSexo;
	}

	public void setListaSexo(List<Sexo> listaSexo) {
		this.listaSexo = listaSexo;
	}

	public Paroquia getParoquiaEcc() {
		return paroquiaEcc;
	}

	public void setParoquiaEcc(Paroquia paroquiaEcc) {
		this.paroquiaEcc = paroquiaEcc;
	}

	public Casal getCasal() {
		return casal;
	}

	public void setCasal(Casal casal) {
		this.casal = casal;
	}

	public ECC getEcc() {
		return ecc;
	}

	public void setEcc(ECC ecc) {
		this.ecc = ecc;
	}

	public String getImagemEle() {
		return imagemEle;
	}

	public void setImagemEle(String imagemEle) {
		this.imagemEle = imagemEle;
	}

	public String getImagemEla() {
		return imagemEla;
	}

	public void setImagemEla(String imagemEla) {
		this.imagemEla = imagemEla;
	}

	public List<Paroquia> getListaParoquia() {
		return listaParoquia;
	}

	public void setListaParoquia(List<Paroquia> listaParoquia) {
		this.listaParoquia = listaParoquia;
	}

	public List<Paroquia> getListaParoquiaEcc() {
		return listaParoquiaEcc;
	}

	public void setListaParoquiaEcc(List<Paroquia> listaParoquiaEcc) {
		this.listaParoquiaEcc = listaParoquiaEcc;
	}

	public List<ECC> getListaEcc() {
		return listaEcc;
	}

	public void setListaEcc(List<ECC> listaEcc) {
		this.listaEcc = listaEcc;
	}

	public List<NumeroRomano> getNumeroRomanosFiltro() {
		return numeroRomanosFiltro;
	}

	public void setNumeroRomanosFiltro(List<NumeroRomano> numeroRomanosFiltro) {
		this.numeroRomanosFiltro = numeroRomanosFiltro;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

}
