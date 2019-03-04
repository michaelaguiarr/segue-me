package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.filter.VendaFilter;
import com.segue.model.Evento;
import com.segue.model.FormaDePagamento;
import com.segue.model.ParaQuemVenda;
import com.segue.model.SegueMe;
import com.segue.model.StatusVenda;
import com.segue.model.Usuario;
import com.segue.model.Venda;
import com.segue.repository.EventoRepository;
import com.segue.repository.VendaRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroVendaService;
import com.segue.service.NegocioException;
import com.segue.util.Constants;
import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaVendaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private VendaRepository service;

	@Inject
	private EventoRepository eventoRepository;

	@Inject
	private CadastroVendaService serviceVenda;

	private Venda vendaSelecionado;

	private List<Venda> listaVenda;

	private VendaFilter filter;

	private boolean vendaAdicionadaParaPagar;
	private boolean exibirPagar;
	private List<Venda> listaVendaPagar = new ArrayList<>();

	private SegueMe segueMe;
	private Seguranca seguranca;
	private Usuario usarioLogado;
	private ParaQuemVenda paraQuem;

	private List<Evento> listaSeguidor = new ArrayList<>();
	private List<Evento> listaSeguimista = new ArrayList<>();
	private List<Evento> listaCasal = new ArrayList<>();
	private List<Evento> listaPadre = new ArrayList<>();

	public PesquisaVendaBean() {
		vendaSelecionado = new Venda();
		filter = new VendaFilter();
		listaSeguidor = new ArrayList<>();
		listaSeguimista = new ArrayList<>();
		listaCasal = new ArrayList<>();
		listaPadre = new ArrayList<>();
	}

	public void inicializar() {
		exibirPagar = true;
		carregarUsuarioLogado();
		filter.setSegueMe(segueMe);
	}

	public void carregarUsuarioLogado() {
		this.seguranca = new Seguranca();
		this.usarioLogado = this.seguranca.usuarioLogado();
		this.segueMe = usarioLogado.getSegueMe();
	}

	/**
	 * Pesquisa venda pelo titulo
	 */
	public void pesquisar() {
		listaVenda = service.filtrados(filter);
		if (listaVenda.isEmpty()) {
			FacesUtil.addErrorMessage("Venda não encontrado");
		}
	}

	public void onCarregarNome() {
		filter.setEvento(null);
	}

	public BigDecimal getTotalPesquisa() {
		if (listaVenda == null) {
			return BigDecimal.ZERO;
		} else {
			BigDecimal valorTotal = listaVenda.stream().map(Venda::getValorTotal).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			return valorTotal;
		}

	}

	/**
	 * AutoComplete Seguidor
	 */
	public List<Evento> buscaPessoas(String nome) {
		return this.getSeguidorPorNome(nome);
	}

	private List<Evento> getSeguidorPorNome(String nome) {
		List<Evento> seguidores = eventoRepository.findBySeguidorNome(NomeComInicialMaiscula.iniciaisMaiuscula(nome),
				segueMe);
		return seguidores;
	}

	/**
	 * AutoComplete Casal
	 */
	public List<Evento> buscaPessoasCasal(String nome) {
		return this.getCasalPorNome(nome);
	}

	private List<Evento> getCasalPorNome(String nome) {
		List<Evento> casal = eventoRepository.findByCasalNome(NomeComInicialMaiscula.iniciaisMaiuscula(nome), segueMe);
		return casal;
	}

	/**
	 * AutoComplete padre
	 */
	public List<Evento> buscaPessoasPadre(String nome) {
		return this.getPadrePorNome(nome);
	}

	private List<Evento> getPadrePorNome(String nome) {
		List<Evento> padres = eventoRepository.findByPadreNome(NomeComInicialMaiscula.iniciaisMaiuscula(nome), segueMe);
		return padres;
	}

	/**
	 * AutoComplete seguimista
	 */
	public List<Evento> buscaPessoasSeguimista(String nome) {
		return this.getSeguimistaPorNome(nome);
	}

	private List<Evento> getSeguimistaPorNome(String nome) {
		List<Evento> padres = eventoRepository.findBySeguimistaNome(NomeComInicialMaiscula.iniciaisMaiuscula(nome),
				segueMe);
		return padres;
	}

	/**
	 * Seleciona segue-me na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/venda/cadastro-venda.xhtml?venda=" + vendaSelecionado.getId());
	}

	/**
	 * Adiciona venda para pagar
	 */
	public void adicionaVenda(Venda a) {
		if (vendaAdicionadaParaPagar) {
			listaVendaPagar.add(a);
			a = new Venda();
		} else {
			listaVendaPagar.remove(a);
			a = new Venda();
		}
	}

	public void salvar() {
		try {
			for (Venda v : listaVendaPagar) {
				Seguranca s = new Seguranca();
				
				v.setUsuario(s.usuarioLogado());
				v.setSegueMe(segueMe);
				v.setStatusVenda(StatusVenda.PAGO);
				v = serviceVenda.salvar(v);
				carregarUsuarioLogado();
				v = new Venda();
			}
			FacesUtil.addInfoMessage("Venda salva com sucesso!");
			listaVendaPagar = new ArrayList<>();
			listaVenda = new ArrayList<>();
			vendaAdicionadaParaPagar = false;

		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public boolean liberaFuncaoParaLiberar() {
		return listaVendaPagar.isEmpty();
	}

	public ParaQuemVenda[] getParaQuemVenda() {
		return ParaQuemVenda.values();
	}

	public FormaDePagamento[] getFormaPagamento() {
		return FormaDePagamento.values();
	}

	public StatusVenda[] getStatusVendas() {
		return StatusVenda.values();
	}

	public Venda getVendaSelecionado() {
		return vendaSelecionado;
	}

	public void setVendaSelecionado(Venda vendaSelecionado) {
		this.vendaSelecionado = vendaSelecionado;
	}

	public ParaQuemVenda getParaQuem() {
		return paraQuem;
	}

	public void setParaQuem(ParaQuemVenda paraQuem) {
		this.paraQuem = paraQuem;
	}

	public List<Evento> getListaSeguidor() {
		return listaSeguidor;
	}

	public void setListaSeguidor(List<Evento> listaSeguidor) {
		this.listaSeguidor = listaSeguidor;
	}

	public List<Evento> getListaSeguimista() {
		return listaSeguimista;
	}

	public void setListaSeguimista(List<Evento> listaSeguimista) {
		this.listaSeguimista = listaSeguimista;
	}

	public List<Evento> getListaCasal() {
		return listaCasal;
	}

	public void setListaCasal(List<Evento> listaCasal) {
		this.listaCasal = listaCasal;
	}

	public List<Evento> getListaPadre() {
		return listaPadre;
	}

	public void setListaPadre(List<Evento> listaPadre) {
		this.listaPadre = listaPadre;
	}

	public List<Venda> getListaVenda() {
		return listaVenda;
	}

	public VendaFilter getFilter() {
		return filter;
	}

	public void setFilter(VendaFilter filter) {
		this.filter = filter;
	}

	public void setListaVenda(List<Venda> listaVenda) {
		this.listaVenda = listaVenda;
	}

	public boolean isVendaAdicionadaParaPagar() {
		return vendaAdicionadaParaPagar;
	}

	public void setVendaAdicionadaParaPagar(boolean vendaAdicionadaParaPagar) {
		this.vendaAdicionadaParaPagar = vendaAdicionadaParaPagar;
	}

	public List<Venda> getListaVendaPagar() {
		return listaVendaPagar;
	}

	public void setListaVendaPagar(List<Venda> listaVendaPagar) {
		this.listaVendaPagar = listaVendaPagar;
	}

	public boolean isExibirPagar() {
		return exibirPagar;
	}

	public void setExibirPagar(boolean exibirPagar) {
		this.exibirPagar = exibirPagar;
	}

}
