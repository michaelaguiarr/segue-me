package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.filter.PalestraCasalFilter;
import com.segue.model.Evento;
import com.segue.model.Palestra;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.EventoRepository;
import com.segue.repository.PalestraRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaPalestraCasalBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private PalestraRepository palestraRepository;

	private Evento evento;

	private Seguranca seguranca;
	private Usuario usuarioLogado;

	private PalestraCasalFilter filter;

	private List<Evento> listaEventoCasal;
	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Palestra> listaPaletras;

	public PesquisaPalestraCasalBean() {
		evento = new Evento();
		filter = new PalestraCasalFilter();
		this.listaSegueMe = new ArrayList<>();
		this.listaEventoCasal = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaPaletras = palestraRepository.listaAll();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
		this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
		filter.setSegueMe(this.usuarioLogado.getSegueMe());
		this.listaEventoCasal = repository.filtradosPalestraCasal(this.filter);
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		listaEventoCasal = repository.filtradosPalestraCasal(filter);
		if (listaEventoCasal.isEmpty()) {
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
	 * Seleciona bairro na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/evento-casal/cadastro-evento-palestra.xhtml?casal=" + evento.getId());
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

	public List<Evento> getListaEventoCasal() {
		return listaEventoCasal;
	}

	public void setListaEventoCasal(List<Evento> listaEventoCasal) {
		this.listaEventoCasal = listaEventoCasal;
	}

	public PalestraCasalFilter getFilter() {
		return filter;
	}

	public void setFilter(PalestraCasalFilter filter) {
		this.filter = filter;
	}

	public List<Palestra> getListaPaletras() {
		return listaPaletras;
	}

	public void setListaPaletras(List<Palestra> listaPaletras) {
		this.listaPaletras = listaPaletras;
	}

}
