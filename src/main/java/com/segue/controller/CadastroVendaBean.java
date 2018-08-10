package com.segue.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Evento;
import com.segue.model.FormaDePagamento;
import com.segue.model.ParaQuemVenda;
import com.segue.model.SegueMe;
import com.segue.model.StatusVenda;
import com.segue.model.Usuario;
import com.segue.model.Venda;
import com.segue.repository.EventoRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroVendaService;
import com.segue.service.NegocioException;
import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroVendaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroVendaService service;

	@Inject
	private EventoRepository eventoRepository;

	private Venda venda;
	private SegueMe segueMe;
	private Seguranca seguranca;
	private Usuario usarioLogado;

	private ParaQuemVenda paraQuem;

	public CadastroVendaBean() {
		limpar();
	}

	public void inicializar() {
		if (this.venda == null) {
			limpar();
		}
		carregarUsuarioLogado();
	}

	public void carregarUsuarioLogado() {
		this.seguranca = new Seguranca();
		this.usarioLogado = this.seguranca.usuarioLogado();
		this.segueMe = usarioLogado.getSegueMe();
	}

	private void limpar() {
		venda = new Venda();
		venda.setDataVenda(new Date());
		segueMe = new SegueMe();
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

	public void salvar() {
		try {
			if (venda.getValorTotal().compareTo(BigDecimal.ZERO) == 0) {
				FacesUtil.addErrorMessage("Informe um valor maior que ZERO!");
			} else {
				venda.setUsuario(usarioLogado);
				venda.setSegueMe(segueMe);
				this.venda = service.salvar(venda);
				limpar();
				carregarUsuarioLogado();
				FacesUtil.addInfoMessage("Venda salva com sucesso!");
			}
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.venda);
			FacesUtil.addInfoMessage("Venda " + this.venda.getId() + " excluída com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void onCarregarNome() {
		if (venda.getParaQuemVenda() == ParaQuemVenda.SEGUIDOR) {
			venda.setEvento(null);
			venda.setStatusVenda(StatusVenda.ABERTO);
		} else if (venda.getParaQuemVenda() == ParaQuemVenda.SEGUIMISTA) {
			venda.setEvento(null);
			venda.setStatusVenda(StatusVenda.ABERTO);
		} else if (venda.getParaQuemVenda() == ParaQuemVenda.CASAL) {
			venda.setEvento(null);
			venda.setStatusVenda(StatusVenda.ABERTO);
		} else if (venda.getParaQuemVenda() == ParaQuemVenda.PADRE) {
			venda.setEvento(null);
			venda.setStatusVenda(StatusVenda.ABERTO);
		} else if (venda.getParaQuemVenda() == ParaQuemVenda.DESCONHECIDO) {
			venda.setEvento(null);
			venda.setStatusVenda(StatusVenda.PAGO);
		} else {
			System.out.println("passou");
			venda.setEvento(null);
			venda.setStatusVenda(StatusVenda.PAGO);
		}
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

	public boolean isEditando() {
		return this.venda.getId() != null;
	}

	public Venda getVenda() {
		return venda;
	}

	public void setVenda(Venda venda) {
		this.venda = venda;
	}

	public ParaQuemVenda getParaQuem() {
		return paraQuem;
	}

	public void setParaQuem(ParaQuemVenda paraQuem) {
		this.paraQuem = paraQuem;
	}

}
