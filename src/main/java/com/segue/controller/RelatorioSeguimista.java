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

import com.segue.model.Circulo;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.CirculoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.util.jsf.FacesUtil;
import com.segue.util.report.ExecutorRelatorioDownload;

import net.sf.jasperreports.engine.JRParameter;

@Named
@ViewScoped
public class RelatorioSeguimista implements Serializable {

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
	private CirculoRepository circuloRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	private SegueMe segueMe;
	private Seguranca seguranca;
	private Usuario usarioLogado;
	private Paroquia paroquia;
	private Circulo circulo;
	private Integer pagina;
	private boolean layout;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Circulo> listaCirculo;

	public RelatorioSeguimista() {
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
		circulo = new Circulo();
		this.pagina = 0;
		this.layout = false;
		this.listaSegueMe = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
		this.listaCirculo = new ArrayList<>();
	}

	public void carregarUsuarioLogado() {
		this.seguranca = new Seguranca();
		this.usarioLogado = this.seguranca.usuarioLogado();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.paroquia = this.usarioLogado.getSegueMe().getParoquia();
		this.listaSegueMe = segueMeRepository.findByParoquia(paroquia);
		this.segueMe = usarioLogado.getSegueMe();
		listaCirculo = circuloRepository.findBySegueMe(this.segueMe);
	}

	/**
	 * gerar pdf
	 */
	public void emitir() {
		try {
			String nomeArquivo = "";
			Map<String, Object> parametros = new HashMap<>();
			if (circulo != null) {
				nomeArquivo = "relacaoSeguimistaCirculo";
				parametros.put("circulo", this.circulo.getId());
			} else {
				nomeArquivo = "relacaoSeguimista";
			}
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", this.segueMe.getId());
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload("/jasper/" + nomeArquivo + ".jasper",
					this.response, parametros, "Resumo" + this.segueMe.getNumeroRomano().getNumeroRomano() + ".pdf");
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

	/**
	 * Carregar funcao;
	 */
	public void onCirculoChange() {
		if (this.segueMe != null) {
			listaCirculo = circuloRepository.findBySegueMe(this.segueMe);
			this.circulo = null;
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

	public Integer getPagina() {
		return pagina;
	}

	public void setPagina(Integer pagina) {
		this.pagina = pagina;
	}

	public Circulo getCirculo() {
		return circulo;
	}

	public void setCirculo(Circulo circulo) {
		this.circulo = circulo;
	}

	public List<Circulo> getListaCirculo() {
		return listaCirculo;
	}

	public void setListaCirculo(List<Circulo> listaCirculo) {
		this.listaCirculo = listaCirculo;
	}

	public boolean isLayout() {
		return layout;
	}

	public void setLayout(boolean layout) {
		this.layout = layout;
	}

}
