package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.segue.filter.EventoFilter;
import com.segue.model.Evento;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.EventoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaInscricaoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	private Evento inscricao;

	private Seguranca seguranca;
	private Usuario usuarioLogado;

	private EventoFilter filter;

	private List<Evento> listaInscritos;
	private LazyDataModel<Evento> modelo;
	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;

	public PesquisaInscricaoBean() {
		inscricao = new Evento();
		filter = new EventoFilter();
		this.listaSegueMe = new ArrayList<>();
		this.listaInscritos = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
		this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
		filter.setSegueMe(this.usuarioLogado.getSegueMe());
		this.modelo = criarModelo();
	}

	/**
	 * Cria o modelo de paginação server-side (só a página visível vem à memória).
	 */
	private LazyDataModel<Evento> criarModelo() {
		return new LazyDataModel<Evento>() {
			private static final long serialVersionUID = 1L;

			@Override
			public List<Evento> load(int first, int pageSize, String sortField, SortOrder sortOrder,
					Map<String, Object> filters) {
				setRowCount(repository.contarInscricao(filter).intValue());
				boolean asc = sortOrder != SortOrder.DESCENDING;
				return repository.filtradosInscricaoPaginado(filter, first, pageSize, sortField, asc);
			}

			/**
			 * Converte o rowKey (Evento.id) de volta no objeto ao selecionar a linha.
			 * Obrigatório em LazyDataModel: o PrimeFaces não usa o algoritmo básico de
			 * rowKey para modelos lazy. Buscamos na página já carregada para reaproveitar
			 * a projeção e não recarregar a imagem (blob EAGER) da inscrição.
			 */
			@Override
			@SuppressWarnings("unchecked")
			public Evento getRowData(String rowKey) {
				List<Evento> pagina = (List<Evento>) getWrappedData();
				if (pagina != null) {
					for (Evento evento : pagina) {
						if (evento.getId() != null && evento.getId().toString().equals(rowKey)) {
							return evento;
						}
					}
				}
				return null;
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
				.redirect(Constants.CONTEXT + "/inscricao/cadastro-inscricao.xhtml?inscricao=" + inscricao.getId());
	}

	public Evento getInscricao() {
		return inscricao;
	}

	public void setInscricao(Evento inscricao) {
		this.inscricao = inscricao;
	}

	public EventoFilter getFilter() {
		return filter;
	}

	public void setFilter(EventoFilter filter) {
		this.filter = filter;
	}

	public List<Evento> getListaInscritos() {
		return listaInscritos;
	}

	public void setListaInscritos(List<Evento> listaInscritos) {
		this.listaInscritos = listaInscritos;
	}

	public LazyDataModel<Evento> getModelo() {
		return modelo;
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

}
