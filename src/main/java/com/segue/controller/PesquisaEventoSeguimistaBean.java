package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.filter.EventoSeguimistaFilter;
import com.segue.model.Circulo;
import com.segue.model.Evento;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.CirculoRepository;
import com.segue.repository.EventoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaEventoSeguimistaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private CirculoRepository circuloRepository;

	private Evento evento;

	private Seguranca seguranca;
	private Usuario usuarioLogado;

	private EventoSeguimistaFilter filter;

	private List<Evento> listaEventoSeguidor;
	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Circulo> listaCirculo;

	public PesquisaEventoSeguimistaBean() {
		evento = new Evento();
		filter = new EventoSeguimistaFilter();
		this.listaSegueMe = new ArrayList<>();
		this.listaEventoSeguidor = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
		this.listaCirculo = new ArrayList<>();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
		this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
		filter.setSegueMe(this.usuarioLogado.getSegueMe());
		this.listaCirculo = circuloRepository.findBySegueMe(filter.getSegueMe());
		// this.listaEventoSeguidor = repository.filtradosSeguidor(this.filter);
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		listaEventoSeguidor = repository.filtradosSeguimista(filter);
		if (listaEventoSeguidor.isEmpty()) {
			FacesUtil.addErrorMessage("Nenhum Resultado Encontrado!");
		}
	}

	/**
	 * Carregar Segue-me;
	 */
	public void onSegueMeChange() {
		this.filter.setSegueMe(null);
		this.filter.setCirculo(null);
		this.listaCirculo = new ArrayList<>();
		if (this.filter.getParoquia() != null) {
			listaSegueMe = segueMeRepository.findByParoquia(this.filter.getParoquia());
			this.filter.setSegueMe(null);
			this.filter.setCirculo(null);
		}
	}

	/**
	 * Carregar funcao;
	 */
	public void onCirculoChange() {
		if (this.filter.getSegueMe() != null) {
			listaCirculo = circuloRepository.findBySegueMe(filter.getSegueMe());
			this.filter.setCirculo(null);
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
				Constants.CONTEXT + "/evento-seguidor/cadastro-seguimista.xhtml?seguimista=" + evento.getId());
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

	public List<Evento> getListaEventoSeguidor() {
		return listaEventoSeguidor;
	}

	public void setListaEventoSeguidor(List<Evento> listaEventoSeguidor) {
		this.listaEventoSeguidor = listaEventoSeguidor;
	}

	public EventoSeguimistaFilter getFilter() {
		return filter;
	}

	public void setFilter(EventoSeguimistaFilter filter) {
		this.filter = filter;
	}

	public List<Circulo> getListaCirculo() {
		return listaCirculo;
	}

	public void setListaCirculo(List<Circulo> listaCirculo) {
		this.listaCirculo = listaCirculo;
	}

}
