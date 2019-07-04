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

import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.StatusInscricao;
import com.segue.model.Usuario;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.util.jsf.FacesUtil;
import com.segue.util.report.ExecutorRelatorio;

import net.sf.jasperreports.engine.JRParameter;

@Named
@ViewScoped
public class RelatorioInscritosPorSituacao implements Serializable {

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

	private StatusInscricao status = null;
	private SegueMe segueMe;

	private Seguranca seguranca;
	private Usuario usarioLogado;
	private Paroquia paroquia;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;

	public RelatorioInscritosPorSituacao() {
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
			if (!this.status.equals(StatusInscricao.PENDENTE)) {
				Map<String, Object> parametros = new HashMap<>();
				parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
				parametros.put("status", this.status);
				parametros.put("segueMe", this.segueMe.getId());
				ExecutorRelatorio executor = new ExecutorRelatorio("/jasper/relacaoInscritos.jasper", this.response,
						parametros, "RelacaoInscrtios" + status + ".pdf");
				Session session = manager.unwrap(Session.class);
				session.doWork(executor);

				if (executor.isRelatorioGerado()) {
					facesContext.responseComplete();
					inicializar();
				}
			} else {
				emitirFicha();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	public void emitirFicha() {
		try {
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("status", this.status);
			parametros.put("segueMe", this.segueMe.getId());
			System.out.println(this.status);
			ExecutorRelatorio executor = new ExecutorRelatorio("/jasper/inscricao.jasper", this.response, parametros,
					"RelacaoInscrtios" + status + ".pdf");
			Session session = manager.unwrap(Session.class);
			session.doWork(executor);

			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
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

	public StatusInscricao[] getStatusInscricao() {
		return StatusInscricao.values();
	}

	public StatusInscricao getStatus() {
		return status;
	}

	public void setStatus(StatusInscricao status) {
		this.status = status;
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

}
