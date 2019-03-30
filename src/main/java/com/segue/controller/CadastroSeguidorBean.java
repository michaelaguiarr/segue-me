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

import com.segue.model.Circulo;
import com.segue.model.CorCirculo;
import com.segue.model.Endereco;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.model.Sexo;
import com.segue.model.SituacaoSeguidor;
import com.segue.model.Enum.Estados;
import com.segue.repository.CirculoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.repository.SexoRepository;
import com.segue.service.CadastroCirculoService;
import com.segue.service.CadastroSeguidorService;
import com.segue.service.FotoService;
import com.segue.service.NegocioException;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroSeguidorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroSeguidorService service;

	@Inject
	private CadastroCirculoService cadastroCirculoService;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private SexoRepository sexoRepository;

	@Inject
	private CirculoRepository circuloRepository;

	@Inject
	private FotoService fotoService;

	private Paroquia paroquia;
	private Paroquia paroquiaCirculo;
	private Seguidor seguidor;
	private String imagemDoUsuario;
	private Circulo circulo;

	private List<Paroquia> listaParoquiaSeguidor = new ArrayList<>();
	private List<SegueMe> listaSegueMeSeguidor = new ArrayList<>();
	private List<Sexo> listaSexo = new ArrayList<>();
	private List<Circulo> listaCirculos = new ArrayList<>();
	private List<Paroquia> listaParoquiaCirculo = new ArrayList<>();
	private List<SegueMe> listaSegueMeCirculo = new ArrayList<>();

	public CadastroSeguidorBean() {
		limpar();
	}

	public void inicializar() {
		if (this.seguidor == null) {
			limpar();
		} else {
			this.paroquia = this.seguidor.getSegueMe().getParoquia();
			this.listaCirculos = circuloRepository.findByParoquia(this.paroquia);
			this.listaSegueMeSeguidor = segueMeRepository.findByParoquia(this.paroquia);
			imagemDoUsuario = fotoService.recuperarViaByte(this.seguidor.getImagem(), "seguidor",
					this.seguidor.getId());
		}
		this.listaSexo = sexoRepository.findAll();
		this.listaParoquiaSeguidor = paroquiaRepository.listaParoquias();
	}

	private void limpar() {
		paroquia = new Paroquia();
		seguidor = new Seguidor();
		seguidor.setEndereco(new Endereco());
		this.listaSegueMeSeguidor = new ArrayList<>();
		this.listaParoquiaSeguidor = new ArrayList<>();
		this.listaCirculos = new ArrayList<>();
		this.imagemDoUsuario = null;
		this.circulo = new Circulo();
		this.paroquiaCirculo = new Paroquia();
		this.listaSegueMeCirculo = new ArrayList<>();
		this.listaParoquiaCirculo = new ArrayList<>();
	}

	public void limparCirculo() {
		this.circulo = new Circulo();
		this.paroquiaCirculo = new Paroquia();
		this.listaSegueMeCirculo = new ArrayList<>();
		this.listaParoquiaCirculo = paroquiaRepository.listaParoquias();
	}

	public void salvar() throws IOException {
		try {
			seguidor.setSituacaoSeguidor(SituacaoSeguidor.ATIVO);
			this.seguidor = service.salvar(seguidor);
			limpar();
			FacesUtil.addInfoMessage("Seguidor salva com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void salvarCirculo() throws IOException {
		try {
			this.circulo = cadastroCirculoService.salvar(circulo);
			if (this.seguidor.getSegueMe() != null) {
				listaCirculos = circuloRepository.findBySegueMe(this.seguidor.getSegueMe());
			} else {
				listaCirculos = new ArrayList<>();
			}
			limparCirculo();
			FacesUtil.addInfoMessage("Círculo salva com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.seguidor);
			FacesUtil.addInfoMessage("Seguidor " + this.seguidor.getNome() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void upload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			seguidor.setImagem(fotoFile);
			seguidor.setExibirImagem(true);
			imagemDoUsuario = fotoService.recuperarViaByte(this.seguidor.getImagem(), "seguidor",
					this.seguidor.getId());
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFoto() {
		try {
			fotoService.deletarTemp(this.seguidor.getId(), "seguidor");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		seguidor.setImagem(null);
		seguidor.setExibirImagem(false);
		imagemDoUsuario = null;
	}

	/**
	 * Carregar Segue-me Seguidor;
	 */
	public void onSegueMeSeguidorChange() {
		if (this.paroquia != null) {
			listaSegueMeSeguidor = segueMeRepository.findByParoquia(this.paroquia);
			this.seguidor.setSegueMe(null);
			this.seguidor.setCirculo(null);
		}
	}

	/**
	 * Carregar Segue-me Circulo;
	 */
	public void onSegueMeCirculoChange() {
		if (this.paroquiaCirculo != null) {
			listaSegueMeCirculo = segueMeRepository.findByParoquia(this.paroquiaCirculo);
			this.circulo.setSegueMe(null);
		}
	}

	/**
	 * Carregar Circulos;
	 */
	public void onCirculoChange() {
		if (this.paroquia != null) {
			listaCirculos = circuloRepository.findByParoquia(paroquia);
			this.seguidor.setCirculo(null);
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void limparTela() throws IOException {
		limpar();
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/seguidor-public/pesquisa-seguidor.xhtml");
	}

	public boolean isEditando() {
		return this.seguidor.getId() != null;
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

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public List<Sexo> getListaSexo() {
		return listaSexo;
	}

	public void setListaSexo(List<Sexo> listaSexo) {
		this.listaSexo = listaSexo;
	}

	public Seguidor getSeguidor() {
		return seguidor;
	}

	public void setSeguidor(Seguidor seguidor) {
		this.seguidor = seguidor;
	}

	public List<Paroquia> getListaParoquiaSeguidor() {
		return listaParoquiaSeguidor;
	}

	public void setListaParoquiaSeguidor(List<Paroquia> listaParoquiaSeguidor) {
		this.listaParoquiaSeguidor = listaParoquiaSeguidor;
	}

	public List<SegueMe> getListaSegueMeSeguidor() {
		return listaSegueMeSeguidor;
	}

	public void setListaSegueMeSeguidor(List<SegueMe> listaSegueMeSeguidor) {
		this.listaSegueMeSeguidor = listaSegueMeSeguidor;
	}

	public String getImagemDoUsuario() {
		return imagemDoUsuario;
	}

	public void setImagemDoUsuario(String imagemDoUsuario) {
		this.imagemDoUsuario = imagemDoUsuario;
	}

	public List<Circulo> getListaCirculos() {
		return listaCirculos;
	}

	public void setListaCirculos(List<Circulo> listaCirculos) {
		this.listaCirculos = listaCirculos;
	}

	public Circulo getCirculo() {
		return circulo;
	}

	public void setCirculo(Circulo circulo) {
		this.circulo = circulo;
	}

	public List<Paroquia> getListaParoquiaCirculo() {
		return listaParoquiaCirculo;
	}

	public void setListaParoquiaCirculo(List<Paroquia> listaParoquiaCirculo) {
		this.listaParoquiaCirculo = listaParoquiaCirculo;
	}

	public List<SegueMe> getListaSegueMeCirculo() {
		return listaSegueMeCirculo;
	}

	public void setListaSegueMeCirculo(List<SegueMe> listaSegueMeCirculo) {
		this.listaSegueMeCirculo = listaSegueMeCirculo;
	}

	public Paroquia getParoquiaCirculo() {
		return paroquiaCirculo;
	}

	public void setParoquiaCirculo(Paroquia paroquiaCirculo) {
		this.paroquiaCirculo = paroquiaCirculo;
	}

}
