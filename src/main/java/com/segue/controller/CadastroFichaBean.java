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
import com.segue.model.Endereco;
import com.segue.model.Evento;
import com.segue.model.Inscricao;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.model.Sexo;
import com.segue.model.StatusInscricao;
import com.segue.model.Usuario;
import com.segue.model.Enum.Estados;
import com.segue.repository.CirculoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.repository.SexoRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroEventoService;
import com.segue.service.FotoService;
import com.segue.service.NegocioException;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroFichaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroEventoService service;

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

	@Inject
	private transient CadastroEventoSeguidorBean cadastroEventoSeguidorBean;

	private String imagem;

	private Evento evento;
	private Seguranca seguranca;
	private Usuario usarioLogado;
	private Paroquia paroquia;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Sexo> listaSexo = new ArrayList<>();
	private List<Circulo> listaCirculo = new ArrayList<>();

	private boolean novoServidor = false;

	public CadastroFichaBean() {
		limpar();
	}

	public void inicializar() {
		if (this.evento == null) {
			limpar();
			carregarUsuarioLogado();
		} else {
			carregarUsuarioLogado();
			if (this.evento.getSeguidor() == null) {
				this.evento.setSeguidor(new Seguidor());
				this.evento.getSeguidor().setEndereco(new Endereco());
			}else {
				this.novoServidor = true;
			}
			imagem = fotoService.recuperarViaByte(this.evento.getSeguidor().getImagem(), "seguidor",
					this.evento.getSeguidor().getId());
			this.paroquia = this.evento.getSegueMe().getParoquia();
			this.listaSegueMe = segueMeRepository.findByParoquia(this.paroquia);
			this.listaCirculo = circuloRepository.findBySegueMe(this.getEvento().getSegueMe());
		}
		listaParoquia = paroquiaRepository.listaParoquias();
		listaSexo = sexoRepository.findAll();
	}

	public void carregarUsuarioLogado() {
		this.seguranca = new Seguranca();
		this.usarioLogado = this.seguranca.usuarioLogado();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.paroquia = this.usarioLogado.getSegueMe().getParoquia();
		this.listaSegueMe = segueMeRepository.findByParoquia(paroquia);
		this.evento.setSegueMe(usarioLogado.getSegueMe());
	}

	private void limpar() {
		paroquia = new Paroquia();
		evento = new Evento();
		evento.setInscricao(new Inscricao());
		evento.setSeguidor(new Seguidor());
		evento.getInscricao().setStatusInscricao(StatusInscricao.PENDENTE);
		usarioLogado = new Usuario();
		seguranca = new Seguranca();
		this.imagem = null;
		this.listaSegueMe = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
		this.listaCirculo = new ArrayList<>();
	}

	public void salvar() throws IOException {
		try {
			if (this.evento.getCirculo() != null) {
				this.evento.getSeguidor().setCirculo(this.evento.getCirculo());
				this.evento.getSeguidor().setCorCirculo(this.evento.getCirculo().getCorCirculo());
			}
			this.evento.setUsuario(usarioLogado);
			this.evento = service.salvar(evento);
			limpar();
			listaSexo = sexoRepository.findAll();
			carregarUsuarioLogado();
			FacesUtil.addInfoMessage("Ficha salva com sucesso!");
			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(Constants.CONTEXT + "/inscricao/pesquisa-inscricao.xhtml");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.evento);
			FacesUtil.addInfoMessage("Ficha " + this.evento.getInscricao().getNumero() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	/**
	 * Carregar Segue-me;
	 */
	public void onSegueMeChange() {
		if (this.paroquia != null) {
			listaSegueMe = segueMeRepository.findByParoquia(paroquia);
			this.evento.setSegueMe(null);
		}
	}

	/**
	 * Carregar funcao;
	 */
	public void onCirculoChange() {
		if (this.evento.getSeguidor().getSegueMe() != null) {
			listaCirculo = circuloRepository.findBySegueMe(this.evento.getSeguidor().getSegueMe());
			this.evento.getSeguidor().setCirculo(null);
		}
	}

	public void upload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			evento.getSeguidor().setImagem(fotoFile);
			evento.getSeguidor().setExibirImagem(true);
			imagem = fotoService.recuperarViaByte(this.evento.getSeguidor().getImagem(), "seguidor",
					this.evento.getSeguidor().getId());
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFoto() {
		try {
			fotoService.deletarTemp(this.evento.getSeguidor().getId(), "seguidor");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		evento.getSeguidor().setImagem(null);
		evento.getSeguidor().setExibirImagem(false);
		imagem = null;
	}

	/**
	 * Carregar Ficha;
	 */
	public void carregarFicha() {
		if (this.evento.getInscricao().getNumero() != null) {
			try {
				this.evento = service.carregarFicha(evento.getInscricao().getNumero(),
						evento.getSegueMe().getParoquia());
				if (this.evento.getSeguidor() == null) {
					this.evento.setSeguidor(new Seguidor());
					this.evento.getInscricao().setStatusInscricao(StatusInscricao.INSCRITO);
					this.evento.setSeguidor(new Seguidor());
					this.evento.getSeguidor().setEndereco(new Endereco());
				}
			} catch (NegocioException ne) {
				inicializar();
				FacesUtil.addErrorMessage(ne.getMessage());
			}
		}
	}

	public void novoServidorAcesso() {
		if (novoServidor) {
			novoServidor = false;
			this.evento.setSeguidor(new Seguidor());
		} else {
			novoServidor = true;
		}
	}

	public boolean isEditando() {
		return this.evento.getId() != null;
	}

	public boolean isExcluir() {
		return this.evento.getId() != null && this.evento.getSeguidor().getId() == null;
	}

	public boolean isInsrito() {
		return !this.evento.getInscricao().getStatusInscricao().equals(StatusInscricao.PENDENTE);
	}

	public boolean isSeguidorValido() {
		return this.evento.getSeguidor().getId() != null || novoServidor;
	}

	public StatusInscricao[] getStatusInscricao() {
		return StatusInscricao.values();
	}

	public Estados[] getEstados() {
		return Estados.values();
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public List<SegueMe> getListaSegueMe() {
		return listaSegueMe;
	}

	public void setListaSegueMe(List<SegueMe> listaSegueMe) {
		this.listaSegueMe = listaSegueMe;
	}

	public List<Paroquia> getListaParoquia() {
		return listaParoquia;
	}

	public List<Sexo> getListaSexo() {
		return listaSexo;
	}

	public List<Circulo> getListaCirculo() {
		return listaCirculo;
	}

	public void setListaCirculo(List<Circulo> listaCirculo) {
		this.listaCirculo = listaCirculo;
	}

	public String getImagem() {
		return imagem;
	}

	public void setImagem(String imagem) {
		this.imagem = imagem;
	}

	public CadastroEventoSeguidorBean getCadastroEventoSeguidorBean() {
		return cadastroEventoSeguidorBean;
	}

	public boolean isNovoServidor() {
		return novoServidor;
	}

	public void setNovoServidor(boolean novoServidor) {
		this.novoServidor = novoServidor;
	}

}
