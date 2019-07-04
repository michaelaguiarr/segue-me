package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.filter.CasalFilter;
import com.segue.model.Casal;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.model.Usuario;
import com.segue.repository.CasalRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroCasalService;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaCasalBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CasalRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;
	
	@Inject
	private CadastroCasalService service;

	private Seguranca seguranca;
	private Usuario usuarioLogado;
	private Casal casal;

	private CasalFilter filter;

	private List<Casal> lista;
	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;

	public PesquisaCasalBean() {
		casal = new Casal();
		filter = new CasalFilter();
		this.listaSegueMe = new ArrayList<>();
		this.lista = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();

	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		if (this.seguranca.usuarioLogado() != null) {
			this.usuarioLogado = this.seguranca.usuarioLogado();
			this.listaParoquia = paroquiaRepository.listaParoquias();
			filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
			this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
			filter.setSegueMe(this.usuarioLogado.getSegueMe());
			this.lista = repository.filtrados(filter);
		}
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		lista = repository.filtrados(this.filter);
		if (lista.isEmpty()) {
			FacesUtil.addErrorMessage("Nenhum Resultado Encontrado!");
		}
	}

	public void pesquisarPublic() {
		lista = repository.filtradosPublic(this.filter);
		if (lista.isEmpty()) {
			FacesUtil.addErrorMessage("Nenhum Resultado Encontrado!");
		}
	}
	
	public void atulizarNome() {
		try {
			for (Casal casal : lista) {
				service.salvar(casal);
			}
			FacesUtil.addInfoMessage("Deu Certo.Atulizado com sucesso!");
		} catch (Exception e) {
			e.printStackTrace();
			FacesUtil.addErrorMessage("Deu merda!");
		}
	}

	/**
	 * Carregar Segue-me;
	 */
	public void onSegueMeChange() {
		this.filter.setSegueMe(null);
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
				.redirect(Constants.CONTEXT + "/casal/detalhe-casal.xhtml?casal=" + casal.getId());
	}

	public void selecionarPublic() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/casal-public/detalhe-casal.xhtml?casal=" + casal.getId());
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

	public Casal getCasal() {
		return casal;
	}

	public void setCasal(Casal casal) {
		this.casal = casal;
	}

	public CasalFilter getFilter() {
		return filter;
	}

	public void setFilter(CasalFilter filter) {
		this.filter = filter;
	}

	public List<Casal> getLista() {
		return lista;
	}

	public void setLista(List<Casal> lista) {
		this.lista = lista;
	}

}
