package com.segue.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.segue.model.Evento;
import com.segue.model.StatusInscricao;
import com.segue.util.jsf.FacesUtil;
import com.segue.util.report.ExecutorRelatorioDownload;

import net.sf.jasperreports.engine.JRParameter;

@Named
@ViewScoped
public class RelatorioInscritosFichasSingle implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	@Inject
	private FacesContext facesContext;

	@Inject
	private HttpServletResponse response;

	private Evento evento;

	public RelatorioInscritosFichasSingle() {
		limpar();
	}

	public void inicializar() {
		if (this.evento != null) {
			limpar();
		}
	}

	public void limpar() {
		this.evento = new Evento();
	}

	/**
	 * gerar pdf
	 */
	public void emitir() {
		try {
			if (!this.evento.getInscricao().getStatusInscricao().equals(StatusInscricao.PENDENTE)) {
				Map<String, Object> parametros = new HashMap<>();
				parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
				parametros.put("id", this.evento.getInscricao().getId());
				ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload(
						"/jasper/inscricaoVisitacaoSingle.jasper", this.response, parametros,
						"Fichas" + evento.getInscricao().getId() + ".pdf");
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
			parametros.put("id", this.evento.getInscricao().getId());
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload("/jasper/inscricaoSingle.jasper",
					this.response, parametros, "RelacaoInscrtios" + this.evento.getInscricao().getId() + ".pdf");
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

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

}
