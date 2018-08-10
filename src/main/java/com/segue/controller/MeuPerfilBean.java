package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.segue.model.Equipe;
import com.segue.model.Funcao;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Sexo;
import com.segue.model.Usuario;
import com.segue.repository.EquipeRepository;
import com.segue.repository.FuncaoRepository;
import com.segue.repository.ParoquiaRepository;
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
public class MeuPerfilBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroUsuarioService usuarioService;

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
	private FotoService fotoService;

	private Usuario usuario;
	private Paroquia paroquia;
	private Seguranca seguranca;
	private String imagemDoUsuario;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipe;
	private List<Funcao> listaFuncao;
	private List<Sexo> listaSexo;

	private String senhaAtual;

	private String novaSenha;

	private Boolean autorizadoParaTrocarSenha;

	private Boolean trocarAssinatura;

	private static final String MSG_SAVE = "Salvo com sucesso!";

	private static final String CONTEXT = FacesContext.getCurrentInstance().getExternalContext()
			.getRequestContextPath();

	@PostConstruct
	public void init() {
		limpar();
	}

	public void inicializar() {
		this.usuario = seguranca.getUsuarioLogado().getUsuario();
		this.paroquia = this.usuario.getSegueMe().getParoquia();
		this.listaSegueMe = segueMeRepository.findByParoquia(this.paroquia);
		this.listaFuncao = funcaoRepository.findByEquipe(this.usuario.getEquipe());
		listaParoquia = paroquiaRepository.listaParoquias();
		listaEquipe = equipeRepository.listaALL();
		listaSexo = sexoRepository.findAll();
		imagemDoUsuario = fotoService.recuperarViaByte(this.usuario.getImagem(), "usuario", this.usuario.getId());
	}

	private void limpar() {
		this.usuario = new Usuario();
		this.seguranca = new Seguranca();
		this.listaSegueMe = new ArrayList<>();
		this.listaFuncao = new ArrayList<>();
		this.setAutorizadoParaTrocarSenha(false);
		this.setTrocarAssinatura(false);
		this.imagemDoUsuario = null;
	}

	/**
	 * Atualiza dados do usuario
	 */
	public void updateUsuario() {
		try {
			usuario.setUltimaAlteracao(Calendar.getInstance());
			usuarioService.salvar(usuario);
			FacesUtil.addInfoMessage(MSG_SAVE);
		} catch (NegocioException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	/**
	 * Recarrega assinatura para exibir nova imagem
	 * 
	 * @throws IOException
	 */
	public void recarregaUsuario() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(CONTEXT + "/meu-perfil/meu-perfil.xhtml?usuario=" + usuario.getId());
	}

	/**
	 * Mudar senha
	 */
	public String mudarSenha() {
		try {
			usuarioService.businessSenha(senhaAtual, usuario);
			usuario.setUltimaAlteracao(Calendar.getInstance());
			usuario.setSenha(ConverteParaMD5.toMD5(novaSenha));
			usuarioService.salvar(usuario);
			FacesUtil.addInfoMessage("Senha alterada com sucesso!");
		} catch (NegocioException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		return null;
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

	/**
	 * Busca cod do usuario logado - (somente o usuario logado)
	 * 
	 */
	public void buscarUsuarioLogado() {
		usuario = usuarioService.loadUsuarioLogado(usuario);
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getSenhaAtual() {
		return senhaAtual;
	}

	public void setSenhaAtual(String senhaAtual) {
		this.senhaAtual = senhaAtual;
	}

	public String getNovaSenha() {
		return novaSenha;
	}

	public void setNovaSenha(String novaSenha) {
		this.novaSenha = novaSenha;
	}

	public Boolean getTrocarAssinatura() {
		return trocarAssinatura;
	}

	public void setTrocarAssinatura(Boolean trocarAssinatura) {
		this.trocarAssinatura = trocarAssinatura;
	}

	public Boolean getAutorizadoParaTrocarSenha() {
		return autorizadoParaTrocarSenha;
	}

	public void setAutorizadoParaTrocarSenha(Boolean autorizadoParaTrocarSenha) {
		this.autorizadoParaTrocarSenha = autorizadoParaTrocarSenha;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public List<Paroquia> getListaParoquia() {
		return listaParoquia;
	}

	public void setListaParoquia(List<Paroquia> listaParoquia) {
		this.listaParoquia = listaParoquia;
	}

	public List<SegueMe> getListaSegueMe() {
		return listaSegueMe;
	}

	public void setListaSegueMe(List<SegueMe> listaSegueMe) {
		this.listaSegueMe = listaSegueMe;
	}

	public List<Equipe> getListaEquipe() {
		return listaEquipe;
	}

	public void setListaEquipe(List<Equipe> listaEquipe) {
		this.listaEquipe = listaEquipe;
	}

	public List<Funcao> getListaFuncao() {
		return listaFuncao;
	}

	public void setListaFuncao(List<Funcao> listaFuncao) {
		this.listaFuncao = listaFuncao;
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

}
