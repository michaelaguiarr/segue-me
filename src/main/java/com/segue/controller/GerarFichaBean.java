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

import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.security.Seguranca;
import com.segue.service.CadastroEventoInscritoService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;
import com.segue.util.report.ExecutorRelatorio;

import net.sf.jasperreports.engine.JRParameter;

@Named
@ViewScoped
public class GerarFichaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroEventoInscritoService service;

	@Inject
	private FacesContext facesContext;

	@Inject
	private HttpServletResponse response;

	@Inject
	private EntityManager manager;

	private SegueMe segueMe;
	private Seguranca seguranca;
	private Usuario usarioLogado;
	private Integer numero;

	public GerarFichaBean() {
		limpar();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		this.usarioLogado = this.seguranca.usuarioLogado();
		this.segueMe = this.usarioLogado.getSegueMe();
		this.numero = null;
	}

	private void limpar() {
		this.segueMe = new SegueMe();
		this.usarioLogado = new Usuario();
		this.seguranca = new Seguranca();
	}

	public void salvar() {
		try {
			service.salvar(numero, segueMe, usarioLogado);
			limpar();
			FacesUtil.addInfoMessage("Fichas Geradas com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	/**
	 * gerar pdf
	 */
	public void emitir() {
		try {
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", this.segueMe.getId());
			ExecutorRelatorio executor = new ExecutorRelatorio("/relatorios/examesPorSecao.jasper", this.response,
					parametros, "Fichas " + segueMe.getTitulo() + ".pdf");

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

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

}
