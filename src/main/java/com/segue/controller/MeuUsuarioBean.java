package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

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
import com.segue.service.CadastroUsuarioService;
import com.segue.service.NegocioException;
import com.segue.util.ConverteParaMD5;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class MeuUsuarioBean implements Serializable {

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

	private Usuario usuario;
	private Paroquia paroquia;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipe;
	private List<Funcao> listaFuncao;
	private List<Sexo> listaSexo;
	private List<Perfil> listaPerfil;

	private String senhaAtual;

	private String novaSenha;

	private Boolean autorizadoParaTrocarSenha;

	private static final String MSG_SAVE = "Usuário salvo com sucesso!";

	public MeuUsuarioBean() {
		limpar();
	}

	public void inicializar() {
		if (this.usuario == null) {
			limpar();
		} else {
			this.paroquia = this.usuario.getSegueMe().getParoquia();
			this.listaSegueMe = segueMeRepository.findByParoquia(this.paroquia);
			this.listaFuncao = funcaoRepository.findByEquipe(this.usuario.getEquipe());
		}
		listaParoquia = paroquiaRepository.listaParoquias();
		listaEquipe = equipeRepository.listaALL();
		listaSexo = sexoRepository.findAll();
		listaPerfil = perfilRepository.listaALL();
	}

	private void limpar() {
		usuario = new Usuario();
		paroquia = new Paroquia();
		this.listaSegueMe = new ArrayList<>();
		this.listaFuncao = new ArrayList<>();
	}

	public void salvar() {
		try {
			usuario.setStatus(Status.AUTORIZADO);
			this.usuario = service.salvar(usuario);
			limpar();
			FacesUtil.addInfoMessage("Usuário salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	/**
	 * Atualiza dados do usuario
	 * 
	 * @throws IOException
	 */
	public void updateUsuario() throws IOException {
		try {
			usuario.setUltimaAlteracao(Calendar.getInstance());
			this.usuario = service.salvar(usuario);
			FacesUtil.addInfoMessage(MSG_SAVE);

		} catch (NegocioException e) {
			FacesUtil.addErrorMessage(e.getMessage());
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
	 * Mudar senha
	 */
	public String mudarSenha() {
		try {
			usuario.setUltimaAlteracao(Calendar.getInstance());
			usuario.setSenha(ConverteParaMD5.toMD5(novaSenha));
			this.usuario = service.salvar(usuario);
			FacesUtil.addInfoMessage("Senha alterada com sucesso!");
		} catch (NegocioException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		return null;
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

	public Boolean getAutorizadoParaTrocarSenha() {
		return autorizadoParaTrocarSenha;
	}

	public void setAutorizadoParaTrocarSenha(Boolean autorizadoParaTrocarSenha) {
		this.autorizadoParaTrocarSenha = autorizadoParaTrocarSenha;
	}

}
