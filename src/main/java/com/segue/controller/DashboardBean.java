package com.segue.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.SegueMe;
import com.segue.model.StatusInscricao;
import com.segue.model.Usuario;
import com.segue.repository.EventoRepository;
import com.segue.security.Seguranca;
import com.segue.service.FotoService;
import com.segue.util.jsf.FacesUtil;

/**
 * Dashboard focado nas duas entregas da Equipe Gráfica em cada encontro: gerar o
 * quadrante e imprimir os crachás. Todos os números são do Segue-Me do usuário
 * logado, agregados no {@link EventoRepository} (flag {@code cracha} = pendente e
 * {@link StatusInscricao}).
 */
@Named
@ViewScoped
public class DashboardBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Id da Equipe Gráfica (mesma constante usada em {@code Seguranca.isGrafica}). */
	private static final int EQUIPE_GRAFICA = 15;

	@Inject
	private Seguranca seguranca;

	@Inject
	private EventoRepository eventoRepository;

	@Inject
	private FotoService fotoService;

	private Usuario usuarioLogado;
	private SegueMe segueMe;
	private boolean grafica;
	private boolean semEvento;

	private List<Cracha> crachas = new ArrayList<>();
	private long totalCrachas;
	private long totalPendentes;
	private long totalImpressos;
	private int pctCrachas;

	private List<EquipeLinha> equipes = new ArrayList<>();
	private long totalPrevistos;
	private long totalAlocados;
	private long vagas;
	private int pctAlocacao;

	private List<StatusLinha> situacoes = new ArrayList<>();
	private long totalInscritos;

	public void inicializar() {
		this.usuarioLogado = seguranca.usuarioLogado();
		if (usuarioLogado == null) {
			this.semEvento = true;
			return;
		}
		this.grafica = usuarioLogado.getEquipe() != null && usuarioLogado.getEquipe().getId() != null
				&& usuarioLogado.getEquipe().getId() == EQUIPE_GRAFICA;
		this.segueMe = usuarioLogado.getSegueMe();
		if (segueMe == null) {
			this.semEvento = true;
			return;
		}
		carregarCrachas();
		carregarEquipes();
		carregarSituacoes();
	}

	private void carregarCrachas() {
		adicionarCracha("Seguidores", "fa fa-users", eventoRepository.resumoCrachaSeguidores(segueMe),
				"/evento-seguidor/relatorio-cracha", "/evento-seguidor/relatorio-cracha-equipe");
		adicionarCracha("Seguimistas", "fa fa-user", eventoRepository.resumoCrachaSeguimistas(segueMe),
				"/evento-seguidor/relatorio-cracha-seguimista", null);
		adicionarCracha("Casais", "fa fa-heart", eventoRepository.resumoCrachaCasais(segueMe),
				"/evento-casal/relatorio-cracha", "/evento-casal/relatorio-cracha-equipe");
		adicionarCracha("Padres / Bispos", "fa fa-institution", eventoRepository.resumoCrachaPadres(segueMe),
				"/evento-padre/relatorio-cracha", null);
		adicionarCracha("Palestrantes", "fa fa-microphone", eventoRepository.resumoCrachaConvidados(segueMe),
				"/evento-convidado/relatorio-cracha", null);
		this.pctCrachas = totalCrachas > 0 ? (int) Math.round(totalImpressos * 100.0 / totalCrachas) : 0;
	}

	private void adicionarCracha(String rotulo, String icone, long[] r, String outcome, String outcomeEquipe) {
		Cracha c = new Cracha(rotulo, icone, r[0], r[1], outcome, outcomeEquipe);
		crachas.add(c);
		totalCrachas += c.getTotal();
		totalPendentes += c.getPendentes();
		totalImpressos += c.getImpressos();
	}

	private void carregarEquipes() {
		Integer minhaEquipe = usuarioLogado.getEquipe() != null ? usuarioLogado.getEquipe().getId() : null;
		for (Object[] r : eventoRepository.resumoSeguidoresPorEquipe(segueMe)) {
			Integer equipeId = (Integer) r[0];
			String titulo = (String) r[1];
			long previstos = r[2] != null ? ((Number) r[2]).longValue() : 0;
			long alocados = ((Number) r[3]).longValue();
			long pendentes = ((Number) r[4]).longValue();
			boolean sua = minhaEquipe != null && minhaEquipe.equals(equipeId);
			equipes.add(new EquipeLinha(titulo, previstos, alocados, pendentes, sua));
			totalPrevistos += previstos;
			totalAlocados += alocados;
		}
		this.vagas = Math.max(0, totalPrevistos - totalAlocados);
		this.pctAlocacao = totalPrevistos > 0 ? (int) Math.round(totalAlocados * 100.0 / totalPrevistos) : 0;
	}

	private void carregarSituacoes() {
		long[] contagem = new long[StatusInscricao.values().length];
		for (Object[] r : eventoRepository.resumoInscritosPorStatus(segueMe)) {
			StatusInscricao st = (StatusInscricao) r[0];
			if (st != null) {
				long total = ((Number) r[1]).longValue();
				contagem[st.ordinal()] = total;
				totalInscritos += total;
			}
		}
		StatusInscricao[] ordem = { StatusInscricao.APROVADO, StatusInscricao.PENDENTE, StatusInscricao.INSCRITO,
				StatusInscricao.RESERVA, StatusInscricao.RECUSADO };
		for (StatusInscricao st : ordem) {
			long total = contagem[st.ordinal()];
			int pct = totalInscritos > 0 ? (int) Math.round(total * 100.0 / totalInscritos) : 0;
			situacoes.add(new StatusLinha(st, total, pct));
		}
	}

	/**
	 * Regrava em disco as imagens do retiro, círculos e equipes — perdidas a cada
	 * atualização do sistema — para que os crachás/quadrante não saiam em branco.
	 */
	public void prepararImagens() {
		if (segueMe == null) {
			return;
		}
		fotoService.materializarImagensSegueMe(segueMe);
		fotoService.materializarImagensCirculo(segueMe);
		fotoService.materializarImagensEquipe();
		FacesUtil.addInfoMessage("Imagens preparadas para impressão.");
	}

	public boolean isMontagemCompleta() {
		return totalPrevistos > 0 && vagas == 0;
	}

	public Usuario getUsuarioLogado() {
		return usuarioLogado;
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public boolean isGrafica() {
		return grafica;
	}

	public boolean isSemEvento() {
		return semEvento;
	}

	public List<Cracha> getCrachas() {
		return crachas;
	}

	public long getTotalCrachas() {
		return totalCrachas;
	}

	public long getTotalPendentes() {
		return totalPendentes;
	}

	public long getTotalImpressos() {
		return totalImpressos;
	}

	public int getPctCrachas() {
		return pctCrachas;
	}

	public List<EquipeLinha> getEquipes() {
		return equipes;
	}

	public long getTotalPrevistos() {
		return totalPrevistos;
	}

	public long getTotalAlocados() {
		return totalAlocados;
	}

	public long getVagas() {
		return vagas;
	}

	public int getPctAlocacao() {
		return pctAlocacao;
	}

	public List<StatusLinha> getSituacoes() {
		return situacoes;
	}

	public long getTotalInscritos() {
		return totalInscritos;
	}

	// ===== DTOs de exibição =====

	public static class Cracha implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String rotulo;
		private final String icone;
		private final String outcome;
		private final String outcomeEquipe;
		private final long total;
		private final long pendentes;

		Cracha(String rotulo, String icone, long total, long pendentes, String outcome, String outcomeEquipe) {
			this.rotulo = rotulo;
			this.icone = icone;
			this.total = total;
			this.pendentes = pendentes;
			this.outcome = outcome;
			this.outcomeEquipe = outcomeEquipe;
		}

		public String getRotulo() {
			return rotulo;
		}

		public String getIcone() {
			return icone;
		}

		public long getTotal() {
			return total;
		}

		public long getPendentes() {
			return pendentes;
		}

		public long getImpressos() {
			return total - pendentes;
		}

		public int getPctImpresso() {
			return total > 0 ? (int) Math.round(getImpressos() * 100.0 / total) : 0;
		}

		public boolean isCompleto() {
			return total > 0 && pendentes == 0;
		}

		public boolean isVazio() {
			return total == 0;
		}

		public String getOutcome() {
			return outcome;
		}

		public String getOutcomeEquipe() {
			return outcomeEquipe;
		}

		public boolean isTemEquipe() {
			return outcomeEquipe != null;
		}
	}

	public static class EquipeLinha implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String titulo;
		private final long previstos;
		private final long alocados;
		private final long pendentes;
		private final boolean sua;

		EquipeLinha(String titulo, long previstos, long alocados, long pendentes, boolean sua) {
			this.titulo = titulo;
			this.previstos = previstos;
			this.alocados = alocados;
			this.pendentes = pendentes;
			this.sua = sua;
		}

		public String getTitulo() {
			return titulo;
		}

		public long getPrevistos() {
			return previstos;
		}

		public long getAlocados() {
			return alocados;
		}

		public long getPendentes() {
			return pendentes;
		}

		public long getImpressos() {
			return alocados - pendentes;
		}

		public long getFaltam() {
			return Math.max(0, previstos - alocados);
		}

		public boolean isCompletaEquipe() {
			return previstos > 0 && alocados >= previstos;
		}

		public boolean isCrachaCompleto() {
			return pendentes == 0;
		}

		public int getPctImpresso() {
			return alocados > 0 ? (int) Math.round(getImpressos() * 100.0 / alocados) : 0;
		}

		public boolean isSua() {
			return sua;
		}
	}

	public static class StatusLinha implements Serializable {
		private static final long serialVersionUID = 1L;
		private final StatusInscricao status;
		private final long total;
		private final int pct;

		StatusLinha(StatusInscricao status, long total, int pct) {
			this.status = status;
			this.total = total;
			this.pct = pct;
		}

		public StatusInscricao getStatus() {
			return status;
		}

		public long getTotal() {
			return total;
		}

		public int getPct() {
			return pct;
		}
	}
}
