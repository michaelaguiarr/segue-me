package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.filter.EventoPadreFilter;
import com.segue.model.Equipe;
import com.segue.model.Evento;
import com.segue.model.Funcao;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.EquipeRepository;
import com.segue.repository.EventoRepository;
import com.segue.repository.FuncaoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaEventoPadreBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private EquipeRepository equipeRepository;

	@Inject
	private FuncaoRepository funcaoRepository;

	private Evento evento;

	private Seguranca seguranca;
	private Usuario usuarioLogado;

	private EventoPadreFilter filter;

	private List<Evento> listaEventoSeguidor;
	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipes;
	private List<Funcao> listaFuncaos;

	public PesquisaEventoPadreBean() {
		evento = new Evento();
		filter = new EventoPadreFilter();
		this.listaSegueMe = new ArrayList<>();
		this.listaEventoSeguidor = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaEquipes = equipeRepository.listaALL();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
		this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
		filter.setSegueMe(this.usuarioLogado.getSegueMe());
		if (!seguranca.isGraficaAndComandante()) {
			filter.setEquipe(usuarioLogado.getEquipe());
			listaFuncaos = funcaoRepository.findByEquipe(filter.getEquipe());
		}
	    this.listaEventoSeguidor = repository.filtradosPadre(this.filter);
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		listaEventoSeguidor = repository.filtradosPadre(this.filter);
		if (listaEventoSeguidor.isEmpty()) {
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
		}
	}

	/**
	 * Carregar funcao;
	 */
	public void onEquipeChange() {
		if (this.filter.getEquipe() != null) {
			listaFuncaos = funcaoRepository.findByEquipe(this.filter.getEquipe());
			this.filter.setFuncao(null);
		}
	}

	/**
	 * Seleciona bairro na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext().redirect(
				Constants.CONTEXT + "/evento-padre/cadastro-evento-padre.xhtml?padre=" + evento.getId());
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

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public EventoPadreFilter getFilter() {
		return filter;
	}

	public void setFilter(EventoPadreFilter filter) {
		this.filter = filter;
	}

	public List<Evento> getListaEventoSeguidor() {
		return listaEventoSeguidor;
	}

	public void setListaEventoSeguidor(List<Evento> listaEventoSeguidor) {
		this.listaEventoSeguidor = listaEventoSeguidor;
	}

	public List<Equipe> getListaEquipes() {
		return listaEquipes;
	}

	public void setListaEquipes(List<Equipe> listaEquipes) {
		this.listaEquipes = listaEquipes;
	}

	public List<Funcao> getListaFuncaos() {
		return listaFuncaos;
	}

	public void setListaFuncaos(List<Funcao> listaFuncaos) {
		this.listaFuncaos = listaFuncaos;
	}

}
