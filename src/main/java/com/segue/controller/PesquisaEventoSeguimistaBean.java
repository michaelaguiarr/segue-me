package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

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
import com.segue.service.CadastroEventoSeguidorService;
import com.segue.service.FotoService;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;
import com.segue.util.report.ExecutorRelatorioDownload;

import net.sf.jasperreports.engine.JRParameter;

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

	@Inject
	private CadastroEventoSeguidorService cadastroService;

	@Inject
	private EntityManager manager;

	@Inject
	private HttpServletResponse response;

	@Inject
	private FotoService fotoService;

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
	 * Coloca os selecionados de volta na fila de impressão (cracha = true), para
	 * reimprimir apenas alguns crachás.
	 */
	public void marcarSelecionadosParaImprimir() {
		atualizarCrachaSelecionados(true);
	}

	/**
	 * Marca os selecionados como já impressos (cracha = false), tirando-os da fila.
	 */
	public void marcarSelecionadosComoImpressos() {
		atualizarCrachaSelecionados(false);
	}

	private void atualizarCrachaSelecionados(boolean pendente) {
		List<Long> ids = new ArrayList<>();
		for (Evento evento : listaEventoSeguidor) {
			if (evento.isSelecionado()) {
				ids.add(evento.getId());
			}
		}
		if (ids.isEmpty()) {
			FacesUtil.addInfoMessage("Selecione ao menos um registro.");
			return;
		}
		cadastroService.atualizarCracha(ids, pendente);
		FacesUtil.addInfoMessage(ids.size()
				+ (pendente ? " crachá(s) marcado(s) para imprimir." : " crachá(s) marcado(s) como impresso(s)."));
		pesquisar();
	}

	/**
	 * Gera o PDF de crachá apenas dos seguimistas selecionados (por id), sem
	 * alterar a flag de crachá.
	 */
	public void imprimirSelecionados() {
		List<Long> ids = new ArrayList<>();
		for (Evento evento : listaEventoSeguidor) {
			if (evento.isSelecionado()) {
				ids.add(evento.getId());
			}
		}
		if (ids.isEmpty()) {
			FacesUtil.addInfoMessage("Selecione ao menos um registro.");
			return;
		}
		SegueMe segueMe = filter.getSegueMe();
		if (segueMe == null || segueMe.getId() == null) {
			FacesUtil.addErrorMessage("Selecione um Segue-Me na pesquisa antes de imprimir.");
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			fotoService.materializarImagensCirculo(segueMe);
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", segueMe.getId());
			parametros.put("whereCracha", montarFiltroIds(ids));
			String nomeArquivo = segueMe.getNomeArquivoCracha().isEmpty() ? "cracha" : segueMe.getNomeArquivoCracha();
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload(
					"/jasper/" + nomeArquivo + "Seguimista.jasper", response, parametros, "CrachaSelecionados.pdf");
			Session session = manager.unwrap(Session.class);
			session.doWork(executor);
			if (executor.isRelatorioGerado()) {
				FacesContext.getCurrentInstance().responseComplete();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível gerar o PDF dos selecionados.");
		}
	}

	private String montarFiltroIds(List<Long> ids) {
		StringBuilder sb = new StringBuilder("public.evento_segue_me.id IN (");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(ids.get(i));
		}
		return sb.append(")").toString();
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
