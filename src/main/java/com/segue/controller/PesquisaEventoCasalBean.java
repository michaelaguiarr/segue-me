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

import com.segue.filter.EventoCasalFilter;
import com.segue.model.Equipe;
import com.segue.model.Evento;
import com.segue.model.Funcao;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.EquipeRepository;
import com.segue.repository.EventoRepository;
import com.segue.repository.FuncaoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroEventoCasalService;
import com.segue.service.FotoService;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;
import com.segue.util.report.ExecutorRelatorioDownload;

import net.sf.jasperreports.engine.JRParameter;

@Named
@ViewScoped
public class PesquisaEventoCasalBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private EquipeRepository equipeRepository;

	@Inject
	private FuncaoRepository funcaoRepository;

	@Inject
	private CadastroEventoCasalService cadastroService;

	@Inject
	private EntityManager manager;

	@Inject
	private HttpServletResponse response;

	@Inject
	private FotoService fotoService;

	private Evento evento;

	private Seguranca seguranca;
	private Usuario usuarioLogado;

	private EventoCasalFilter filter;

	private List<Evento> listaEventoCasal;
	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipes;
	private List<Funcao> listaFuncaos;

	public PesquisaEventoCasalBean() {
		evento = new Evento();
		filter = new EventoCasalFilter();
		this.listaSegueMe = new ArrayList<>();
		this.listaEventoCasal = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaEquipes = equipeRepository.listaALL();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
		this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
		filter.setSegueMe(this.usuarioLogado.getSegueMe());
		if (!seguranca.isGraficaAndComandante()) {
			filter.setEquipe(usuarioLogado.getEquipe());
			listaFuncaos = funcaoRepository.findByEquipe(filter.getEquipe());
		}
//		this.listaEventoCasal = repository.filtradosCasal(this.filter);
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		listaEventoCasal = repository.filtradosCasal(this.filter);
		if (listaEventoCasal.isEmpty()) {
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
		for (Evento evento : listaEventoCasal) {
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
	 * Gera o PDF de crachá apenas dos casais selecionados (por id), sem alterar a
	 * flag de crachá. Reusa o relatório Jasper passando o parâmetro whereCracha.
	 */
	public void imprimirSelecionados() {
		List<Long> ids = new ArrayList<>();
		for (Evento evento : listaEventoCasal) {
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
			fotoService.materializarImagensEquipe();
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", segueMe.getId());
			parametros.put("whereCracha", montarFiltroIds(ids));
			String nomeArquivo = segueMe.getNomeArquivoCracha().isEmpty() ? "cracha" : segueMe.getNomeArquivoCracha();
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload(
					"/jasper/" + nomeArquivo + "Casal.jasper", response, parametros, "CrachaSelecionados.pdf");
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
		if (this.filter.getParoquia() != null) {
			listaSegueMe = segueMeRepository.findByParoquia(this.filter.getParoquia());
			this.filter.setSegueMe(null);
		}
	}

	/**
	 * Carregar funcao;
	 */
	public void onEquipeChange() {
		if (this.filter.getEquipe() != null) {
			listaFuncaos = funcaoRepository.findByEquipe(this.filter.getEquipe());
			this.filter.setFuncao(null);
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
				.redirect(Constants.CONTEXT + "/evento-casal/cadastro-evento-casal.xhtml?casal=" + evento.getId());
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

	public EventoCasalFilter getFilter() {
		return filter;
	}

	public void setFilter(EventoCasalFilter filter) {
		this.filter = filter;
	}

	public List<Equipe> getListaEquipes() {
		return listaEquipes;
	}

	public void setListaEquipes(List<Equipe> listaEquipes) {
		this.listaEquipes = listaEquipes;
	}

	public List<Funcao> getListaFuncaos() {
		return listaFuncaos;
	}

	public void setListaFuncaos(List<Funcao> listaFuncaos) {
		this.listaFuncaos = listaFuncaos;
	}

	public List<Evento> getListaEventoCasal() {
		return listaEventoCasal;
	}

	public void setListaEventoCasal(List<Evento> listaEventoCasal) {
		this.listaEventoCasal = listaEventoCasal;
	}

}
