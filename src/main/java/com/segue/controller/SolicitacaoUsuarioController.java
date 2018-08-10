package com.segue.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Equipe;
import com.segue.model.Funcao;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Sexo;
import com.segue.model.Status;
import com.segue.model.Usuario;
import com.segue.repository.EquipeRepository;
import com.segue.repository.FuncaoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.repository.SexoRepository;
import com.segue.service.CadastroUsuarioService;
import com.segue.service.NegocioException;
import com.segue.util.ConverteParaMD5;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class SolicitacaoUsuarioController implements Serializable {

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

	private Usuario usuario;
	private Paroquia paroquia;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipe;
	private List<Funcao> listaFuncao;
	private List<Sexo> listaSexo;

	@PostConstruct
	public void init() {
		limpar();
	}

	public void inicializar() {
		listaParoquia = paroquiaRepository.listaParoquias();
		listaEquipe = equipeRepository.listaALL();
		listaSexo = sexoRepository.findAll();
	}

	private void limpar() {
		usuario = new Usuario();
		paroquia = new Paroquia();
		this.listaSegueMe = new ArrayList<>();
		this.listaFuncao = new ArrayList<>();
	}

	/**
	 * Apenas envia solicitacao de acesso
	 */
	public String enviarSolicitacao() {
		try {
			usuario.setSenha(ConverteParaMD5.toMD5((usuario.getSenha())));
			usuario.setStatus(Status.PENDENTE);
			this.usuario = usuarioService.salvar(usuario);
			usuario = new Usuario();

			FacesUtil.addInfoMessage("Solicitação enviada aguarde autorização");
			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

			return "/login?faces-redirect=true";
		} catch (NegocioException e) {
			FacesUtil.addErrorMessage(e.getMessage());
			return null;
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

	public Usuario getUsuario() {
		return usuario;
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

	public void setListaEquipe(List<Equipe> listaEquipe) {
		this.listaEquipe = listaEquipe;
	}

	public void setListaSexo(List<Sexo> listaSexo) {
		this.listaSexo = listaSexo;
	}

}
