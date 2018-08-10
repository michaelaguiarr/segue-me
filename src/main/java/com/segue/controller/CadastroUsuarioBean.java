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

import com.segue.model.Equipe;
import com.segue.model.Funcao;
import com.segue.model.Paroquia;
import com.segue.model.Perfil;
import com.segue.model.SegueMe;
import com.segue.model.Sexo;
import com.segue.model.Status;
import com.segue.model.Usuario;
import com.segue.repository.EquipeRepository;
import com.segue.repository.FuncaoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.PerfilRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.repository.SexoRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroUsuarioService;
import com.segue.service.FotoService;
import com.segue.service.NegocioException;
import com.segue.util.ConverteParaMD5;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroUsuarioBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroUsuarioService service;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private EquipeRepository equipeRepository;

	@Inject
	private FuncaoRepository funcaoRepository;

	@Inject
	private SexoRepository sexoRepository;

	@Inject
	private PerfilRepository perfilRepository;

	@Inject
	private FotoService fotoService;

	private Usuario usuario;
	private Seguranca seguranca;
	private Usuario usuarioLogado;
	private Paroquia paroquia;
	private String imagemDoUsuario;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipe;
	private List<Funcao> listaFuncao;
	private List<Sexo> listaSexo;
	private List<Perfil> listaPerfil;

	public CadastroUsuarioBean() {
		limpar();
	}

	public void inicializar() throws IOException {
		if (this.usuario == null) {
			limpar();
			carregarUsuarioLogado();
		} else {
			carregarUsuarioLogado();
			imagemDoUsuario = fotoService.recuperarViaByte(this.usuario.getImagem(), "usuario", this.usuario.getId());
			this.paroquia = this.usuario.getSegueMe().getParoquia();
			this.listaSegueMe = segueMeRepository.findByParoquia(this.paroquia);
			this.listaFuncao = funcaoRepository.findByEquipe(this.usuario.getEquipe());
		}
		listaParoquia = paroquiaRepository.listaParoquias();
		listaEquipe = equipeRepository.listaALL();
		listaSexo = sexoRepository.findAll();
		listaPerfil = perfilRepository.listaALL();
	}

	public void carregarUsuarioLogado(){
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.paroquia = this.usuarioLogado.getSegueMe().getParoquia();
		this.listaSegueMe = segueMeRepository.findByParoquia(paroquia);
		this.usuario.setSegueMe(this.usuarioLogado.getSegueMe());
		this.usuario.setEquipe(this.usuarioLogado.getEquipe());
		this.listaFuncao = funcaoRepository.findByEquipe(this.usuario.getEquipe());
		if (!seguranca.isGraficaAndComandante()) {
			this.usuario.setEquipe(usuarioLogado.getEquipe());
			listaFuncao = funcaoRepository.findByEquipe(usuarioLogado.getEquipe());
		}
	}
	private void limpar() {
		usuario = new Usuario();
		paroquia = new Paroquia();
		usuarioLogado = new Usuario();
		seguranca = new Seguranca();
		this.listaSegueMe = new ArrayList<>();
		this.listaFuncao = new ArrayList<>();
		this.imagemDoUsuario = null;
	}
	

	public void upload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();

		try {
			byte[] fotoFile = fotoService.salvarFotoTempByte(uploadedFile.getFileName(), event.getFile().getContents());
			usuario.setImagem(fotoFile);
			imagemDoUsuario = fotoService.recuperarViaByte(this.usuario.getImagem(), "usuario", this.usuario.getId());
		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	public void removerFoto() {
		try {
			fotoService.deletarTemp(this.usuario.getId(), "usuario");
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		usuario.setImagem(null);
		imagemDoUsuario = null;
	}

	public void salvar() {
		try {
			usuario.setStatus(Status.AUTORIZADO);
			usuario.setSenha(ConverteParaMD5.toMD5((usuario.getSenha())));
			this.usuario = service.salvar(usuario);
			limpar();
			FacesUtil.addInfoMessage("Usuário salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.usuario);
			FacesUtil.addInfoMessage("Usuário " + this.usuario.getNome() + " excluído com sucesso.");
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
			this.usuario.setSegueMe(null);
		}
	}

	/**
	 * Carregar Funcao;
	 */
	public void onFuncaoChange() {
		if (this.usuario.getEquipe() != null) {
			listaFuncao = funcaoRepository.findByEquipe(this.usuario.getEquipe());
			this.usuario.setEquipe(null);
		}
	}

	public boolean isEditando() {
		return this.usuario.getId() != null;
	}

	public Status[] getStatuss() {
		return Status.values();
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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

	public List<Funcao> getListaFuncao() {
		return listaFuncao;
	}

	public void setListaFuncao(List<Funcao> listaFuncao) {
		this.listaFuncao = listaFuncao;
	}

	public List<Equipe> getListaEquipe() {
		return listaEquipe;
	}

	public List<Sexo> getListaSexo() {
		return listaSexo;
	}

	public List<Paroquia> getListaParoquia() {
		return listaParoquia;
	}

	public List<Perfil> getListaPerfil() {
		return listaPerfil;
	}

	public String getImagemDoUsuario() {
		return imagemDoUsuario;
	}

	public void setImagemDoUsuario(String imagemDoUsuario) {
		this.imagemDoUsuario = imagemDoUsuario;
	}

}
