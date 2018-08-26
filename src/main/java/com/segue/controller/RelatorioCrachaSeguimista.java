package com.segue.controller;

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
import com.segue.model.Evento;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.EventoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroEventoSeguidorService;
import com.segue.util.jsf.FacesUtil;
import com.segue.util.report.ExecutorRelatorioDownload;

import net.sf.jasperreports.engine.JRParameter;

@Named
@ViewScoped
public class RelatorioCrachaSeguimista implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	@Inject
	private FacesContext facesContext;

	@Inject
	private HttpServletResponse response;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;
	
	@Inject
	private EventoRepository eventoRepository;
	
	@Inject
	private CadastroEventoSeguidorService service;

	private SegueMe segueMe;
	private Seguranca seguranca;
	private Usuario usarioLogado;
	private Paroquia paroquia;
	private boolean atualizar;
	private String nomeArquivo = "cracha";

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;

	public RelatorioCrachaSeguimista() {
		limpar();
	}

	public void inicializar() {
		limpar();
		carregarUsuarioLogado();
	}

	public void limpar() {
		paroquia = new Paroquia();
		usarioLogado = new Usuario();
		seguranca = new Seguranca();
		segueMe = new SegueMe();
		atualizar = false;
		this.listaSegueMe = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
	}

	public void carregarUsuarioLogado() {
		this.seguranca = new Seguranca();
		this.usarioLogado = this.seguranca.usuarioLogado();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.paroquia = this.usarioLogado.getSegueMe().getParoquia();
		this.listaSegueMe = segueMeRepository.findByParoquia(paroquia);
		this.segueMe = usarioLogado.getSegueMe();
	}

	/**
	 * gerar pdf
	 */
	public void emitir() {
		try {
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", this.segueMe.getId());
			if(this.segueMe.getNomeArquivoCracha().isEmpty()) {
				this.nomeArquivo = "cracha";
			}else {
				this.nomeArquivo = this.segueMe.getNomeArquivoCracha();
			}
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload("/jasper/"+nomeArquivo+"Seguimista.jasper",
					this.response, parametros, "Cracha" + this.segueMe.getNumeroRomano().getNumeroRomano() + ".pdf");
			Session session = manager.unwrap(Session.class);
			session.doWork(executor);

			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
				if(atualizar) {
					List<Evento> eventos = new ArrayList<>();
					EventoSeguimistaFilter filter = new EventoSeguimistaFilter();
					filter.setCracha("SIM");
					filter.setSegueMe(segueMe);;
					eventos = eventoRepository.filtradosSeguimista(filter);
					for (Evento evento : eventos) {
						evento.setCracha(false);
						evento = service.salvar(evento);
						evento = new Evento();
					}
				}
				inicializar();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	/**
	 * Carregar Segue-me;
	 */
	public void onSegueMeChange() {
		if (this.paroquia != null) {
			listaSegueMe = segueMeRepository.findByParoquia(paroquia);
			this.segueMe = null;
		}
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
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

	public boolean isAtualizar() {
		return atualizar;
	}

	public void setAtualizar(boolean atualizar) {
		this.atualizar = atualizar;
	}

}
