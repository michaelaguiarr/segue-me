package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.segue.filter.PalestranteConvidadoFilter;
import com.segue.model.PalestranteConvidado;
import com.segue.model.Usuario;
import com.segue.repository.PalestranteConvidadoRepository;
import com.segue.security.Seguranca;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaPalestranteConvidadoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PalestranteConvidadoRepository repository;

	private Seguranca seguranca;
	private Usuario usuarioLogado;
	private PalestranteConvidado palestranteConvidado;

	private PalestranteConvidadoFilter filter;

	private List<PalestranteConvidado> listaPalestranteConvidado;
	private LazyDataModel<PalestranteConvidado> modelo;

	public PesquisaPalestranteConvidadoBean() {
		palestranteConvidado = new PalestranteConvidado();
		filter = new PalestranteConvidadoFilter();

	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.modelo = criarModelo();
	}

	/**
	 * Cria o modelo de paginação server-side (só a página visível vem à memória).
	 */
	private LazyDataModel<PalestranteConvidado> criarModelo() {
		return new LazyDataModel<PalestranteConvidado>() {
			private static final long serialVersionUID = 1L;

			@Override
			public List<PalestranteConvidado> load(int first, int pageSize, String sortField, SortOrder sortOrder,
					Map<String, Object> filters) {
				setRowCount(repository.contar(filter).intValue());
				boolean asc = sortOrder != SortOrder.DESCENDING;
				return repository.filtradosPaginado(filter, first, pageSize, sortField, asc);
			}
		};
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		this.modelo = criarModelo();
	}

	/**
	 * Carrega o convidado completo (com a foto) sob demanda ao abrir o diálogo. A
	 * listagem é projetada sem o blob por performance, então recarregamos pelo id.
	 */
	public void carregarFoto(Integer id) {
		this.palestranteConvidado = repository.findById(id);
	}

	/**
	 * Seleciona bairro na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext().redirect(Constants.CONTEXT
				+ "/palestranteConvidado/detalhe-convidado.xhtml?convidado=" + palestranteConvidado.getId());
	}

	public PalestranteConvidado getPalestranteConvidado() {
		return palestranteConvidado;
	}

	public void setPalestranteConvidado(PalestranteConvidado palestranteConvidado) {
		this.palestranteConvidado = palestranteConvidado;
	}

	public PalestranteConvidadoFilter getFilter() {
		return filter;
	}

	public void setFilter(PalestranteConvidadoFilter filter) {
		this.filter = filter;
	}

	public List<PalestranteConvidado> getListaPalestranteConvidado() {
		return listaPalestranteConvidado;
	}

	public void setListaPalestranteConvidado(List<PalestranteConvidado> listaPalestranteConvidado) {
		this.listaPalestranteConvidado = listaPalestranteConvidado;
	}

	public LazyDataModel<PalestranteConvidado> getModelo() {
		return modelo;
	}

	public Usuario getUsuarioLogado() {
		return usuarioLogado;
	}

	public void setUsuarioLogado(Usuario usuarioLogado) {
		this.usuarioLogado = usuarioLogado;
	}

}
