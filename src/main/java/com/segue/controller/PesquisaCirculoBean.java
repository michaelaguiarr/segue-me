package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.filter.CirculoFilter;
import com.segue.model.Circulo;
import com.segue.model.CorCirculo;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.CirculoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaCirculoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CirculoRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	private Circulo circulo;

	private CirculoFilter filter;

	private Seguranca seguranca;
	private Usuario usuarioLogado;

	private List<Circulo> listaCirculo;
	private List<Paroquia> listaParaquia;
	private List<SegueMe> listaSegueMe;

	public PesquisaCirculoBean() {
		this.circulo = new Circulo();
		this.filter = new CirculoFilter();
		this.seguranca = new Seguranca();
		this.usuarioLogado = new Usuario();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.filter = new CirculoFilter();
		this.listaParaquia = paroquiaRepository.listaParoquias();
		filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
		this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
		filter.setSegueMe(this.usuarioLogado.getSegueMe());
		this.listaCirculo = repository.filtrados(this.filter);
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		this.listaCirculo = repository.filtrados(this.filter);
		if (listaCirculo.isEmpty()) {
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
	 * Seleciona circulo na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/circulo/cadastro-circulo.xhtml?circulo=" + circulo.getId());
	}

	public CorCirculo[] getCorCirculo() {
		return CorCirculo.values();
	}

	public Circulo getCirculo() {
		return circulo;
	}

	public void setCirculo(Circulo circulo) {
		this.circulo = circulo;
	}

	public CirculoFilter getFilter() {
		return filter;
	}

	public void setFilter(CirculoFilter filter) {
		this.filter = filter;
	}

	public List<Circulo> getListaCirculo() {
		return listaCirculo;
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

}
