package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.filter.PadreFilter;
import com.segue.model.Padre;
import com.segue.model.Paroquia;
import com.segue.repository.PadreRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaPadreBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PadreRepository repository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	private Padre padre;

	private PadreFilter filter;

	private List<Padre> listaPadre;
	private List<Paroquia> listaParoquia;

	public PesquisaPadreBean() {
		padre = new Padre();
		filter = new PadreFilter();
		this.listaParoquia = new ArrayList<>();
		this.listaPadre = new ArrayList<>();

	}

	public void inicializar() {
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.listaPadre = repository.filtrados(filter);
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		listaPadre = repository.filtrados(this.filter);
		if (listaPadre.isEmpty()) {
			FacesUtil.addErrorMessage("Nenhum Resultado Encontrado!");
		}
	}

	/**
	 * Carrega o padre completo (com a foto) sob demanda ao abrir o diálogo. A
	 * listagem é projetada sem o blob por performance, então recarregamos pelo id.
	 */
	public void carregarFoto(Integer id) {
		this.padre = repository.findById(id);
	}

	/**
	 * Seleciona bairro na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/padre/detalhe-padre.xhtml?padre=" + padre.getId());
	}

	public List<Paroquia> getListaParoquia() {
		return listaParoquia;
	}

	public void setListaParoquia(List<Paroquia> listaParoquia) {
		this.listaParoquia = listaParoquia;
	}

	public Padre getPadre() {
		return padre;
	}

	public void setPadre(Padre padre) {
		this.padre = padre;
	}

	public PadreFilter getFilter() {
		return filter;
	}

	public void setFilter(PadreFilter filter) {
		this.filter = filter;
	}

	public List<Padre> getListaPadre() {
		return listaPadre;
	}

	public void setListaPadre(List<Padre> listaPadre) {
		this.listaPadre = listaPadre;
	}

}
