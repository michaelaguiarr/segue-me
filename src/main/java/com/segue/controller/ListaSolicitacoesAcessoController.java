package com.segue.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.SelectEvent;

import com.segue.filter.UsuarioFilter;
import com.segue.model.Equipe;
import com.segue.model.Paroquia;
import com.segue.model.Perfil;
import com.segue.model.SegueMe;
import com.segue.model.Status;
import com.segue.model.Usuario;
import com.segue.repository.EquipeRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.PerfilRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.repository.UsuarioRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroUsuarioService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ListaSolicitacoesAcessoController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroUsuarioService usuarioService;

	@Inject
	private PerfilRepository perfilRepository;

	@Inject
	private UsuarioRepository usuarioRepository;

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
	private Perfil perfil;

	private List<Usuario> solicitacoesDeAcesso;

	private List<Perfil> perfis;
	private List<Paroquia> listaParaquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipe;

	@PostConstruct
	public void init() {
		limpar();
	}

	public void inicializar() {
		if (this.usuario == null) {
			limpar();
		}
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaEquipe = equipeRepository.listaALL();
		this.listaParaquia = paroquiaRepository.listaParoquias();
		filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
		this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
		filter.setSegueMe(this.usuarioLogado.getSegueMe());
		if (!seguranca.isAdministrador()) {
			filter.setEquipe(this.usuarioLogado.getEquipe());
			this.perfil = this.usuarioLogado.getPerfil();
		}
		perfis = perfilRepository.listaALL();
		solicitacoesDeAcesso = usuarioRepository.filtradosSolicitacao(filter);
	}

	private void limpar() {
		usuario = new Usuario();
		filter = new UsuarioFilter();
		perfil = new Perfil();
		this.listaSegueMe = new ArrayList<>();
	}

	/**
	 * Autoriza solicitacao do usuario
	 * 
	 * @throws NegocioException
	 */
	public void autorizarAcesso() throws NegocioException {
		try {
			usuario.setStatus(Status.AUTORIZADO);
			usuario.setPerfil(perfil);
			this.usuario = usuarioService.salvar(usuario);
			FacesUtil.addInfoMessage(this.usuario.getNome() + " autorizado!");
			inicializar();
		} catch (NegocioException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	/**
	 * Atualiza lista de solicitacoes e limpa usuario
	 */
	public void limparTela() {
		usuario = new Usuario();
		solicitacoesDeAcesso = usuarioRepository.findByUsuarioStatus(Status.PENDENTE);
	}

	/**
	 * Remove solicitacao
	 */
	public void removerSolicitacao() {
		try {
			usuarioService.remover(this.usuario);
			FacesUtil.addInfoMessage("Solicitação de " + usuario.getNome() + " removida!");
			limparTela();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}
	
	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		solicitacoesDeAcesso = usuarioRepository.filtradosSolicitacao(this.filter);
		if (solicitacoesDeAcesso.isEmpty()) {
			FacesUtil.addErrorMessage("Nenhum Resultado Encontrado!");
		}
	}
	
	/**
	 * Carregar Segue-me Circulo;
	 */
	public void onSegueMeChange() {
		if (this.filter.getParoquia() != null) {
			listaSegueMe = segueMeRepository.findByParoquia(this.filter.getParoquia());
			this.filter.setSegueMe(null);
		}
	}


	/**
	 * Lista as solicitacoes pendentes
	 * 
	 * @return
	 */
	public List<Usuario> getSolicitacoesDeAcesso() {
		return solicitacoesDeAcesso;
	}

	public void selecionaUsuario(SelectEvent event) {
		System.out.println(">> Usuario " + usuario.getNome());
	}

	/**
	 * Lista os perfis
	 * 
	 * @return
	 */
	public List<Perfil> getPerfis() {
		return perfis;
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

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}
	
	

}
