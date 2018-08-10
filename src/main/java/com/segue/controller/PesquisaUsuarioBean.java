package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.filter.UsuarioFilter;
import com.segue.model.Equipe;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.EquipeRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.repository.UsuarioRepository;
import com.segue.security.Seguranca;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaUsuarioBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UsuarioRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private EquipeRepository equipeRepository;

	private Usuario usuario;

	private Seguranca seguranca;
	private Usuario usuarioLogado;

	private UsuarioFilter filter;

	private List<Usuario> listaUsuario;
	private List<Paroquia> listaParaquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipe;

	public PesquisaUsuarioBean() {
		usuario = new Usuario();
		filter = new UsuarioFilter();
		this.filter = new UsuarioFilter();
		this.listaSegueMe = new ArrayList<>();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaEquipe = equipeRepository.listaALL();
		this.listaParaquia = paroquiaRepository.listaParoquias();
		filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
		this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
		filter.setSegueMe(this.usuarioLogado.getSegueMe());
		if (!seguranca.isAdministrador()) {
			filter.setEquipe(this.usuarioLogado.getEquipe());
		}
		this.listaUsuario = repository.filtrados(this.filter);
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		listaUsuario = repository.filtrados(this.filter);
		if (listaUsuario.isEmpty()) {
			FacesUtil.addErrorMessage("Nenhum Resultado Encontrado!");
		}
	}

	/**
	 * Carregar Segue-me;
	 */
	public void onSegueMeChange() {
		if (this.filter.getParoquia() != null) {
			listaSegueMe = segueMeRepository.findByParoquia(this.filter.getParoquia());
			this.filter.setSegueMe(null);
			this.filter.setEquipe(null);
		}
	}

	/**
	 * Seleciona bairro na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/usuario/usuario-detalhe.xhtml?usuario=" + usuario.getId());
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public UsuarioFilter getFilter() {
		return filter;
	}

	public void setFilter(UsuarioFilter filter) {
		this.filter = filter;
	}

	public List<Paroquia> getListaParaquia() {
		return listaParaquia;
	}

	public void setListaParaquia(List<Paroquia> listaParaquia) {
		this.listaParaquia = listaParaquia;
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

	public List<Usuario> getListaUsuario() {
		return listaUsuario;
	}

}
