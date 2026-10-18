package com.segue.controller;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.segue.model.SegueMe;
import com.segue.service.FotoService;
import com.segue.util.report.ExecutorLivroEncontro;

/**
 * Serve, SEM login, o mesmo PDF do "Livro do Encontro" (capa + história +
 * quadrante de seguimistas + seguidores + palestrantes) que
 * {@code DashboardBean.gerarLivroEncontro()} gera na área logada — acessado
 * via link/QR Code compartilhado com seguimistas e coordenadores, identificado
 * só pelo id do {@link SegueMe} na URL (mesmo padrão aberto já usado em
 * {@code seguidor-public}/{@code casal-public}, sem token).
 */
@Named
@ViewScoped
public class QuadrantePublicoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	@Inject
	private FacesContext facesContext;

	@Inject
	private HttpServletResponse response;

	@Inject
	private FotoService fotoService;

	/** Resolvido automaticamente pelo {@code SegueMeConverter} a partir do o:viewParam. */
	private SegueMe segueMe;

	/** true quando o link é inválido ou o PDF não tem o que gerar — a view mostra uma mensagem. */
	private boolean indisponivel;

	/** Acionado pelo f:viewAction ao carregar a página — escreve o PDF direto na resposta. */
	public void gerar() {
		if (segueMe == null) {
			indisponivel = true;
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			fotoService.materializarImagensCirculo(segueMe);
			ExecutorLivroEncontro executor = new ExecutorLivroEncontro(segueMe.getId(), response,
					"Encontro" + segueMe.getNumeroRomano().getNumeroRomano() + ".pdf", true);
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			} else {
				indisponivel = true;
			}
		} catch (Exception e) {
			indisponivel = true;
		}
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	public boolean isIndisponivel() {
		return indisponivel;
	}
}
