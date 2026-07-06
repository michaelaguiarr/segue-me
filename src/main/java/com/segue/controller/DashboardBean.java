package com.segue.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

import com.segue.model.CorCirculo;
import com.segue.model.SegueMe;
import com.segue.model.StatusInscricao;
import com.segue.model.Usuario;
import com.segue.repository.EventoRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroEjcService;
import com.segue.service.CadastroEventoCasalService;
import com.segue.service.CadastroEventoPadreService;
import com.segue.service.CadastroEventoSeguidorService;
import com.segue.service.CadastroPalestraConvidadoService;
import com.segue.service.FotoService;
import com.segue.util.jsf.FacesUtil;
import org.primefaces.event.FileUploadEvent;

import com.segue.util.report.ExecutorCrachaEquipe;
import com.segue.util.report.ExecutorLivroEncontro;
import com.segue.util.report.ExecutorRelatorioDownload;

import net.sf.jasperreports.engine.JRParameter;

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
	private SegueMeRepository segueMeRepository;

	@Inject
	private CadastroEjcService cadastroEjcService;

	@Inject
	private FotoService fotoService;

	@Inject
	private CadastroEventoSeguidorService seguidorService;

	@Inject
	private CadastroEventoCasalService casalService;

	@Inject
	private CadastroEventoPadreService padreService;

	@Inject
	private CadastroPalestraConvidadoService convidadoService;

	@Inject
	private EntityManager manager;

	@Inject
	private FacesContext facesContext;

	@Inject
	private HttpServletResponse response;

	private Usuario usuarioLogado;
	private SegueMe segueMe;
	private boolean grafica;
	private boolean semEvento;
	private boolean temCapa;
	private boolean temHistoria;
	private boolean temImagem;
	/** Caminho web da imagem do padroeiro já materializada em disco (para exibir no cabeçalho). */
	private String imagemPadroeiroPath;
	/** Cópia FRESCA do Segue-Me (por id) usada só nos diálogos de edição — não o snapshot do login. */
	private SegueMe editSegueMe;

	private List<Cracha> crachas = new ArrayList<>();
	private long totalCrachas;
	private long totalPendentes;
	private long totalImpressos;
	private int pctCrachas;

	private List<EquipeLinha> equipes = new ArrayList<>();
	private long totalPrevistos;
	private long totalAlocados;
	private long totalAlocadosPessoas;
	private long totalPendentesEquipes;
	private long vagas;
	private int pctAlocacao;

	private long pessoasServindo;
	private long seguidoresServindo;
	private long casaisServindo;
	private long alocadosQuadrante;
	private long excedenteQuadrante;

	private List<StatusLinha> situacoes = new ArrayList<>();
	private long totalInscritos;

	private List<CirculoLinha> circulos = new ArrayList<>();
	private long totalSeguimistas;
	private long totalSeguimistasPendentes;
	private List<String> seguimistasSemCirculo = new ArrayList<>();

	private List<Aniversariante> aniversariantesSeguimistas = new ArrayList<>();
	private List<Aniversariante> aniversariantesServico = new ArrayList<>();
	private List<Aniversariante> aniversariantesHoje = new ArrayList<>();
	private List<Aniversariante> aniversariantesCasaisNascimento = new ArrayList<>();
	private List<Aniversariante> aniversariantesCasamento = new ArrayList<>();
	private String mesAtualNome;

	private static final String[] MESES = { "janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho",
			"agosto", "setembro", "outubro", "novembro", "dezembro" };

	public void inicializar() {
		this.usuarioLogado = seguranca.usuarioLogado();
		if (usuarioLogado == null) {
			this.semEvento = true;
			return;
		}
		this.grafica = usuarioLogado.getEquipe() != null && usuarioLogado.getEquipe().getId() != null
				&& usuarioLogado.getEquipe().getId() == EQUIPE_GRAFICA;
		SegueMe snap = usuarioLogado.getSegueMe();
		if (snap == null || snap.getId() == null) {
			this.semEvento = true;
			return;
		}
		// Recarrega FRESCO por id para o cabeçalho refletir as edições (o snapshot do login é estático)
		this.segueMe = segueMeRepository.porId(snap.getId());
		this.temCapa = segueMe.getArquivoCapa() != null && segueMe.getArquivoCapa().length > 0;
		this.temHistoria = segueMe.getArquivoHistoria() != null && segueMe.getArquivoHistoria().length > 0;
		this.temImagem = segueMe.getImagem() != null && segueMe.getImagem().length > 0;
		if (temImagem) {
			// Grava a imagem fresca em disco (sobrescreve) e monta o caminho relativo à
			// raiz do contexto (o dashboard não fica em subpasta). O ?v= com o tamanho do
			// arquivo quebra o cache do navegador quando a imagem é trocada.
			fotoService.materializarImagensSegueMe(segueMe);
			this.imagemPadroeiroPath = "resources/temp/segueMe/" + segueMe.getId() + ".jpg?v=" + segueMe.getImagem().length;
		}
		carregarCrachas();
		carregarEquipes();
		carregarSituacoes();
		carregarCirculos();
		carregarAniversariantes();
		carregarAniversariantesCasais();
		this.seguidoresServindo = totalAlocados;
		this.pessoasServindo = seguidoresServindo + casaisServindo * 2;
		// Quadrante = escala de TODAS as pessoas servindo (seguidores + casais*2)
		// vs o total planejado das equipes (Equipe.pessoas, que inclui os casais).
		this.alocadosQuadrante = pessoasServindo;
		this.excedenteQuadrante = Math.max(0, alocadosQuadrante - totalPrevistos);
		this.vagas = Math.max(0, totalPrevistos - alocadosQuadrante);
		this.pctAlocacao = totalPrevistos > 0
				? (int) Math.min(100, Math.round(alocadosQuadrante * 100.0 / totalPrevistos))
				: 0;
	}

	private void carregarCrachas() {
		adicionarCracha("Seguidores", "fa fa-users", "Seguidor", eventoRepository.resumoCrachaSeguidores(segueMe),
				"/evento-seguidor/relatorio-cracha-equipe");
		adicionarCracha("Seguimistas", "fa fa-user", "Seguimista", eventoRepository.resumoCrachaSeguimistas(segueMe),
				null);
		long[] casais = eventoRepository.resumoCrachaCasais(segueMe);
		this.casaisServindo = casais[0];
		adicionarCracha("Casais", "fa fa-heart", "Casal", casais, "/evento-casal/relatorio-cracha-equipe");
		adicionarCracha("Padres / Bispos", "fa fa-institution", "Padre", eventoRepository.resumoCrachaPadres(segueMe),
				null);
		adicionarCracha("Palestrantes", "fa fa-microphone", "Convidado", eventoRepository.resumoCrachaConvidados(segueMe),
				null);
		this.pctCrachas = totalCrachas > 0 ? (int) Math.round(totalImpressos * 100.0 / totalCrachas) : 0;
	}

	private void adicionarCracha(String rotulo, String icone, String jasperTipo, long[] r, String outcomeEquipe) {
		Cracha c = new Cracha(rotulo, icone, jasperTipo, r[0], r[1], outcomeEquipe);
		crachas.add(c);
		totalCrachas += c.getTotal();
		totalPendentes += c.getPendentes();
		totalImpressos += c.getImpressos();
	}

	private void carregarEquipes() {
		Integer minhaEquipe = usuarioLogado.getEquipe() != null ? usuarioLogado.getEquipe().getId() : null;
		// Une seguidores + casais por equipe. Preserva a ordem dos seguidores (por
		// crachás a imprimir); equipes só de casais entram depois. Cada valor é
		// [previstos, seguidores, casais, pendentesSeguidor, pendentesCasal].
		Map<Integer, long[]> dados = new LinkedHashMap<>();
		Map<Integer, String> titulos = new HashMap<>();
		for (Object[] r : eventoRepository.resumoSeguidoresPorEquipe(segueMe)) {
			Integer equipeId = (Integer) r[0];
			titulos.put(equipeId, (String) r[1]);
			long previstos = r[2] != null ? ((Number) r[2]).longValue() : 0;
			long seguidores = ((Number) r[3]).longValue();
			long pendentes = ((Number) r[4]).longValue();
			dados.put(equipeId, new long[] { previstos, seguidores, 0, pendentes, 0 });
		}
		for (Object[] r : eventoRepository.resumoCasaisPorEquipe(segueMe)) {
			Integer equipeId = (Integer) r[0];
			long casais = ((Number) r[3]).longValue();
			long casaisPendentes = ((Number) r[4]).longValue();
			long[] d = dados.get(equipeId);
			if (d != null) {
				d[2] = casais;
				d[4] = casaisPendentes;
			} else {
				titulos.put(equipeId, (String) r[1]);
				long previstos = r[2] != null ? ((Number) r[2]).longValue() : 0;
				dados.put(equipeId, new long[] { previstos, 0, casais, 0, casaisPendentes });
			}
		}
		for (Map.Entry<Integer, long[]> entry : dados.entrySet()) {
			Integer equipeId = entry.getKey();
			long[] d = entry.getValue();
			boolean sua = minhaEquipe != null && minhaEquipe.equals(equipeId);
			equipes.add(new EquipeLinha(equipeId, titulos.get(equipeId), d[0], d[1], d[2], d[3], d[4], sua));
			totalPrevistos += d[0];
			totalAlocados += d[1];
			totalAlocadosPessoas += d[1] + d[2] * 2;
			totalPendentesEquipes += d[3] + d[4];
		}
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

	private void carregarCirculos() {
		for (Object[] r : eventoRepository.resumoSeguimistasPorCirculo(segueMe)) {
			Integer circuloId = (Integer) r[0];
			String nome = (String) r[1];
			CorCirculo cor = (CorCirculo) r[2];
			long total = ((Number) r[3]).longValue();
			long pendentes = ((Number) r[4]).longValue();
			circulos.add(new CirculoLinha(circuloId, nome, cor, corHex(cor), total, pendentes));
			totalSeguimistas += total;
			totalSeguimistasPendentes += pendentes;
		}
		for (Object[] r : eventoRepository.seguimistasSemCirculo(segueMe)) {
			String nome = (String) r[0];
			String apelido = (String) r[1];
			seguimistasSemCirculo.add(apelido != null && !apelido.trim().isEmpty() ? nome + " (" + apelido + ")" : nome);
		}
	}

	private void carregarAniversariantes() {
		Calendar hoje = Calendar.getInstance();
		int mes = hoje.get(Calendar.MONTH) + 1; // 1..12
		int dia = hoje.get(Calendar.DAY_OF_MONTH);
		this.mesAtualNome = MESES[mes - 1];

		for (Object[] r : eventoRepository.aniversariantesSeguimistas(segueMe)) {
			Aniversariante a = montarAniversariante((String) r[0], (String) r[1], (Date) r[2], (CorCirculo) r[3],
					(String) r[4], mes, dia);
			if (a != null) {
				aniversariantesSeguimistas.add(a);
			}
		}
		for (Object[] r : eventoRepository.aniversariantesSeguidoresServico(segueMe)) {
			Aniversariante a = montarAniversariante((String) r[0], (String) r[1], (Date) r[2], null, (String) r[3], mes,
					dia);
			if (a != null) {
				aniversariantesServico.add(a);
			}
		}
		ordenarPorDia(aniversariantesSeguimistas);
		ordenarPorDia(aniversariantesServico);
		for (Aniversariante a : aniversariantesSeguimistas) {
			if (a.isHoje()) {
				aniversariantesHoje.add(a);
			}
		}
		for (Aniversariante a : aniversariantesServico) {
			if (a.isHoje()) {
				aniversariantesHoje.add(a);
			}
		}
	}

	/**
	 * Aniversariantes de casais de serviço: nascimento (dele e dela) e aniversário
	 * de casamento no mês corrente. Cada casal pode gerar até 3 entradas; o
	 * casamento traz "N anos" (ano atual − ano do casamento).
	 */
	private void carregarAniversariantesCasais() {
		Calendar hoje = Calendar.getInstance();
		int mes = hoje.get(Calendar.MONTH) + 1;
		int dia = hoje.get(Calendar.DAY_OF_MONTH);
		int anoAtual = hoje.get(Calendar.YEAR);

		for (Object[] r : eventoRepository.aniversariantesCasais(segueMe)) {
			String nomeEle = (String) r[0];
			String nomeEla = (String) r[1];
			Date nascEle = (Date) r[2];
			Date nascEla = (Date) r[3];
			Date casamento = (Date) r[4];
			String equipe = (String) r[5];

			Aniversariante ele = montarAniversariante(nomeEle, null, nascEle, null, equipe, mes, dia);
			if (ele != null) {
				aniversariantesCasaisNascimento.add(ele);
			}
			Aniversariante ela = montarAniversariante(nomeEla, null, nascEla, null, equipe, mes, dia);
			if (ela != null) {
				aniversariantesCasaisNascimento.add(ela);
			}
			if (casamento != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(casamento);
				if (c.get(Calendar.MONTH) + 1 == mes) {
					int diaCas = c.get(Calendar.DAY_OF_MONTH);
					int anos = anoAtual - c.get(Calendar.YEAR);
					boolean ehHoje = diaCas == dia;
					String casal = nomeEle + " & " + nomeEla;
					String detalhe = anos > 0 ? anos + (anos == 1 ? " ano" : " anos") + " · " + equipe : equipe;
					aniversariantesCasamento.add(new Aniversariante(casal, null, diaCas, detalhe, null, null, ehHoje));
				}
			}
		}
		ordenarPorDia(aniversariantesCasaisNascimento);
		ordenarPorDia(aniversariantesCasamento);
	}

	/** Cria o aniversariante só se nascer no mês corrente; marca {@code hoje}. */
	private Aniversariante montarAniversariante(String nome, String apelido, Date nascimento, CorCirculo cor,
			String grupo, int mes, int dia) {
		if (nascimento == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(nascimento);
		int mesNasc = c.get(Calendar.MONTH) + 1;
		if (mesNasc != mes) {
			return null;
		}
		int diaNasc = c.get(Calendar.DAY_OF_MONTH);
		boolean ehHoje = diaNasc == dia;
		return new Aniversariante(nome, apelido, diaNasc, grupo, cor, corHex(cor), ehHoje);
	}

	private void ordenarPorDia(List<Aniversariante> lista) {
		lista.sort((a, b) -> Integer.compare(a.getDia(), b.getDia()));
	}

	/** Cor de exibição (chip/ponto) por {@link CorCirculo}; {@code null} → neutro. */
	private static String corHex(CorCirculo cor) {
		if (cor == null) {
			return "#b6b0a6";
		}
		switch (cor) {
		case AMARELO:
			return "#f1c40f";
		case AZUL:
			return "#2e78d6";
		case LARANJA:
			return "#e67e22";
		case ROSA:
			return "#e84393";
		case VERDE:
			return "#1e9e5a";
		case VERMELHO:
			return "#e74c3c";
		case ROXO:
			return "#7b4fd4";
		case CINZA:
			return "#9fb4c7";
		default:
			return "#b6b0a6";
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

	/**
	 * Gera e baixa o PDF do quadrante de seguidores direto do dashboard (mesma
	 * lógica de {@code RelatorioQuadrante.emitir}). Deve ser acionado por um botão
	 * NÃO-AJAX, pois escreve o PDF na resposta HTTP.
	 */
	public void gerarQuadrante() {
		if (segueMe == null) {
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("pagina", 0);
			parametros.put("segueMe", segueMe.getId());
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload("/jasper/quadranteSeguidorLayout.jasper",
					response, parametros, "Quadrante" + segueMe.getNumeroRomano().getNumeroRomano() + ".pdf");
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	/**
	 * Gera e baixa o "Livro do Encontro": um único PDF unindo capa + história
	 * (PDFs externos enviados no cadastro do Segue-Me) + relação de seguimistas +
	 * quadrante de seguidores + quadrante de palestrantes. Botão NÃO-AJAX.
	 */
	public void gerarLivroEncontro() {
		if (segueMe == null) {
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			fotoService.materializarImagensCirculo(segueMe);
			ExecutorLivroEncontro executor = new ExecutorLivroEncontro(segueMe.getId(), response,
					"Encontro" + segueMe.getNumeroRomano().getNumeroRomano() + ".pdf");
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível gerar o livro do encontro.");
		}
	}

	/** Baixa o PDF da capa enviada no cadastro do Segue-Me (fresco do banco). */
	public void baixarCapa() {
		if (segueMe == null) {
			return;
		}
		baixarArquivo(eventoRepository.arquivoCapa(segueMe.getId()), "application/pdf",
				"Capa" + segueMe.getNumeroRomano().getNumeroRomano() + ".pdf");
	}

	/** Baixa o PDF da história enviada no cadastro do Segue-Me (fresco do banco). */
	public void baixarHistoria() {
		if (segueMe == null) {
			return;
		}
		baixarArquivo(eventoRepository.arquivoHistoria(segueMe.getId()), "application/pdf",
				"Historia" + segueMe.getNumeroRomano().getNumeroRomano() + ".pdf");
	}

	/** Baixa a imagem (JPEG) do padroeiro do encontro. */
	public void baixarImagem() {
		if (segueMe == null) {
			return;
		}
		baixarArquivo(segueMe.getImagem(), "image/jpeg",
				"Padroeiro" + segueMe.getNumeroRomano().getNumeroRomano() + ".jpg");
	}

	private void baixarArquivo(byte[] bytes, String contentType, String nomeArquivo) {
		if (bytes == null || bytes.length == 0) {
			FacesUtil.addErrorMessage("Arquivo não encontrado.");
			return;
		}
		try {
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + nomeArquivo + "\"");
			response.getOutputStream().write(bytes);
			response.getOutputStream().flush();
			facesContext.responseComplete();
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível baixar o arquivo.");
		}
	}

	// ===== Editar / anexar o encontro pelo dashboard =====

	/** Carrega uma cópia FRESCA do Segue-Me (por id) para o modal — não o snapshot do login. */
	public void abrirEdicao() {
		if (segueMe != null) {
			this.editSegueMe = segueMeRepository.porId(segueMe.getId());
		}
	}

	/** Anexa a imagem do padroeiro direto (sem modal): normaliza, grava e recarrega. */
	public void anexarImagem(FileUploadEvent event) {
		try {
			byte[] jpg = fotoService.normalizarImagemJpeg(event.getFile().getContents());
			aplicarAnexo(s -> s.setImagem(jpg), "Imagem do padroeiro atualizada.");
		} catch (Exception e) {
			flashErro("Imagem inválida — envie um JPG.");
		}
	}

	/** Anexa a capa (PDF) direto: valida, grava e recarrega. */
	public void anexarCapa(FileUploadEvent event) {
		byte[] bytes = event.getFile().getContents();
		if (!ehPdf(bytes)) {
			flashErro("A capa precisa ser um PDF.");
			return;
		}
		aplicarAnexo(s -> s.setArquivoCapa(bytes), "Capa anexada.");
	}

	/** Anexa a história (PDF) direto: valida, grava e recarrega. */
	public void anexarHistoria(FileUploadEvent event) {
		byte[] bytes = event.getFile().getContents();
		if (!ehPdf(bytes)) {
			flashErro("A história precisa ser um PDF.");
			return;
		}
		aplicarAnexo(s -> s.setArquivoHistoria(bytes), "História anexada.");
	}

	/**
	 * Carrega o Segue-Me FRESCO por id (não o snapshot do login), aplica só o anexo
	 * alterado e persiste. A mensagem sobrevive ao reload da tela (flash) que o
	 * upload dispara em {@code oncomplete}, para o cabeçalho/chips refletirem.
	 */
	private void aplicarAnexo(java.util.function.Consumer<SegueMe> mutacao, String msg) {
		if (segueMe == null) {
			return;
		}
		try {
			SegueMe fresco = segueMeRepository.porId(segueMe.getId());
			mutacao.accept(fresco);
			cadastroEjcService.salvar(fresco);
			mensagemFlash(msg);
		} catch (Exception e) {
			flashErro("Não foi possível anexar: " + e.getMessage());
		}
	}

	private void flashErro(String msg) {
		facesContext.getExternalContext().getFlash().setKeepMessages(true);
		FacesUtil.addErrorMessage(msg);
	}

	/** Remove a imagem do padroeiro do encontro (confirmar antes). */
	public String removerImagem() {
		return removerAnexo(s -> s.setImagem(null), "Imagem removida.");
	}

	/** Remove a capa (PDF) do encontro (confirmar antes). */
	public String removerCapa() {
		return removerAnexo(s -> s.setArquivoCapa(null), "Capa removida.");
	}

	/** Remove a história (PDF) do encontro (confirmar antes). */
	public String removerHistoria() {
		return removerAnexo(s -> s.setArquivoHistoria(null), "História removida.");
	}

	/** Zera um anexo no Segue-Me FRESCO por id, persiste e recarrega o dashboard. */
	private String removerAnexo(java.util.function.Consumer<SegueMe> mutacao, String msg) {
		if (segueMe == null) {
			return null;
		}
		try {
			SegueMe fresco = segueMeRepository.porId(segueMe.getId());
			mutacao.accept(fresco);
			cadastroEjcService.salvar(fresco);
			mensagemFlash(msg);
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível remover: " + e.getMessage());
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	/** Persiste as edições/anexos e recarrega o dashboard (redirect → viewAction). */
	public String salvarEdicao() {
		if (editSegueMe == null) {
			return null;
		}
		try {
			cadastroEjcService.salvar(editSegueMe);
			mensagemFlash("Encontro atualizado.");
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível salvar: " + e.getMessage());
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	private boolean ehPdf(byte[] bytes) {
		return bytes != null && bytes.length > 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D'
				&& bytes[3] == 'F';
	}

	public SegueMe getEditSegueMe() {
		return editSegueMe;
	}

	/**
	 * Gera e baixa o PDF dos crachás de um papel direto do dashboard, usando o
	 * MESMO modelo configurado no retiro que a tela de relatório
	 * ({@code segueMe.nomeArquivoCracha}, ou "cracha" quando vazio) — cada retiro
	 * tem seu próprio modelo. {@code tipo} ∈ {Seguidor, Seguimista, Casal, Padre,
	 * Convidado}. Deve ser acionado por um botão NÃO-AJAX, pois escreve o PDF na
	 * resposta HTTP.
	 */
	public void gerarCracha(String tipo) {
		if (segueMe == null || tipo == null) {
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			if ("Seguimista".equals(tipo)) {
				fotoService.materializarImagensCirculo(segueMe); // crachá de seguimista usa a imagem do círculo
			} else {
				fotoService.materializarImagensEquipe(); // demais crachás usam a imagem das equipes
			}
			String nomeArquivo = segueMe.getNomeArquivoCracha() == null || segueMe.getNomeArquivoCracha().isEmpty()
					? "cracha"
					: segueMe.getNomeArquivoCracha();
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", segueMe.getId());
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload(
					"/jasper/" + nomeArquivo + tipo + ".jasper", response, parametros,
					"Cracha" + tipo + segueMe.getNumeroRomano().getNumeroRomano() + ".pdf");
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	/**
	 * Gera e baixa TODOS os crachás do encontro num único PDF, unindo os relatórios
	 * de cada papel: seguidor, seguimista, casal, padre e palestrante (convidado).
	 * Cada papel que voltar vazio é pulado. Botão NÃO-AJAX.
	 */
	public void gerarCrachaTodos() {
		if (segueMe == null) {
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			fotoService.materializarImagensCirculo(segueMe); // seguimista usa a imagem do círculo
			fotoService.materializarImagensEquipe(); // demais usam a imagem das equipes
			String nomeArquivo = segueMe.getNomeArquivoCracha() == null || segueMe.getNomeArquivoCracha().isEmpty()
					? "cracha"
					: segueMe.getNomeArquivoCracha();
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", segueMe.getId());
			List<String> relatorios = java.util.Arrays.asList(
					"/jasper/" + nomeArquivo + "Seguidor.jasper",
					"/jasper/" + nomeArquivo + "Seguimista.jasper",
					"/jasper/" + nomeArquivo + "Casal.jasper",
					"/jasper/" + nomeArquivo + "Padre.jasper",
					"/jasper/" + nomeArquivo + "Convidado.jasper");
			ExecutorCrachaEquipe executor = new ExecutorCrachaEquipe(relatorios, parametros, response,
					"Crachas" + segueMe.getNumeroRomano().getNumeroRomano() + ".pdf");
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			} else {
				FacesUtil.addErrorMessage("Nenhum crachá para imprimir.");
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	/**
	 * Gera e baixa o PDF dos crachás de UMA equipe — TODOS os participantes: une num
	 * único PDF o crachá de seguidor ({@code <modelo>SeguidorEquipe}) e o de casal
	 * ({@code <modelo>CasalEquipe}). Cada relatório que voltar vazio é pulado (ex.:
	 * seguidor numa equipe só de casais, como a Visitação). Acionado a partir da
	 * linha da equipe na tabela "Pessoas por equipe". Botão NÃO-AJAX.
	 */
	public void gerarCrachaEquipe(Integer equipeId, String titulo) {
		if (segueMe == null || equipeId == null) {
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			fotoService.materializarImagensEquipe();
			String nomeArquivo = segueMe.getNomeArquivoCracha() == null || segueMe.getNomeArquivoCracha().isEmpty()
					? "cracha"
					: segueMe.getNomeArquivoCracha();
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", segueMe.getId());
			parametros.put("equipe", equipeId);
			List<String> relatorios = java.util.Arrays.asList(
					"/jasper/" + nomeArquivo + "SeguidorEquipe.jasper",
					"/jasper/" + nomeArquivo + "CasalEquipe.jasper");
			ExecutorCrachaEquipe executor = new ExecutorCrachaEquipe(relatorios, parametros, response,
					"Cracha" + titulo + ".pdf");
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			} else {
				FacesUtil.addErrorMessage("Nenhum crachá para imprimir nesta equipe.");
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	/**
	 * Gera e baixa o PDF do quadrante de UMA equipe (mesma lógica de
	 * {@code RelatorioQuadranteEquipe.emitir}). Acionado a partir da linha da
	 * equipe na tabela "Seguidores por equipe". Botão NÃO-AJAX (escreve o PDF na
	 * resposta HTTP).
	 */
	public void gerarQuadranteEquipe(Integer equipeId, String titulo) {
		if (segueMe == null || equipeId == null) {
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("pagina", 0);
			parametros.put("segueMe", segueMe.getId());
			parametros.put("equipe", equipeId);
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload(
					"/jasper/quadranteSeguidorEquipe.jasper", response, parametros,
					"Quadrante" + segueMe.getNumeroRomano().getNumeroRomano() + "-" + titulo + ".pdf");
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	/**
	 * Gera e baixa o PDF da LISTA (relação) de seguimistas de UM círculo (mesma
	 * lógica de {@code RelatorioSeguimista.emitir} com círculo) —
	 * {@code relacaoSeguimistaCirculo.jasper}, params segueMe + circulo. Botão
	 * NÃO-AJAX.
	 */
	public void gerarListaCirculo(Integer circuloId, String corLabel) {
		if (segueMe == null || circuloId == null) {
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("segueMe", segueMe.getId());
			parametros.put("circulo", circuloId);
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload(
					"/jasper/relacaoSeguimistaCirculo.jasper", response, parametros,
					"ListaSeguimista" + segueMe.getNumeroRomano().getNumeroRomano() + "-" + corLabel + ".pdf");
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	/**
	 * Gera e baixa o PDF do QUADRANTE (modelo com layout) de UM círculo de
	 * seguimistas (mesma lógica de {@code RelatorioQuadranteSeguimista.emitir}) —
	 * {@code quadranteSeguimistaLayout.jasper}, params pagina + segueMe + circulo.
	 * Botão NÃO-AJAX.
	 */
	public void gerarQuadranteCirculo(Integer circuloId, String corLabel) {
		if (segueMe == null || circuloId == null) {
			return;
		}
		try {
			fotoService.materializarImagensSegueMe(segueMe);
			fotoService.materializarImagensCirculo(segueMe);
			Map<String, Object> parametros = new HashMap<>();
			parametros.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
			parametros.put("pagina", 0);
			parametros.put("segueMe", segueMe.getId());
			parametros.put("circulo", circuloId);
			ExecutorRelatorioDownload executor = new ExecutorRelatorioDownload(
					"/jasper/quadranteSeguimistaLayout.jasper", response, parametros,
					"QuadranteSeguimista" + segueMe.getNumeroRomano().getNumeroRomano() + "-" + corLabel + ".pdf");
			manager.unwrap(Session.class).doWork(executor);
			if (executor.isRelatorioGerado()) {
				facesContext.responseComplete();
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("A execução do relatório não retornou dados.");
		}
	}

	/**
	 * Marca como IMPRESSOS (cracha = false) os crachás pendentes de um papel do
	 * retiro inteiro. Deve ser acionado só depois de confirmar que a impressão
	 * física saiu certa. Redireciona para o dashboard (o {@code viewAction}
	 * recarrega os contadores). {@code tipo} ∈ {Seguidor, Seguimista, Casal,
	 * Padre, Convidado}.
	 */
	public String marcarImpresso(String tipo) {
		if (segueMe == null || tipo == null) {
			return null;
		}
		try {
			List<Long> ids;
			int n;
			switch (tipo) {
			case "Seguidor":
				ids = eventoRepository.idsCrachaPendenteSeguidores(segueMe);
				n = seguidorService.atualizarCracha(ids, false);
				break;
			case "Seguimista":
				ids = eventoRepository.idsCrachaPendenteSeguimistas(segueMe);
				n = seguidorService.atualizarCracha(ids, false);
				break;
			case "Casal":
				ids = eventoRepository.idsCrachaPendenteCasais(segueMe);
				n = casalService.atualizarCracha(ids, false);
				break;
			case "Padre":
				ids = eventoRepository.idsCrachaPendentePadres(segueMe);
				n = padreService.atualizarCracha(ids, false);
				break;
			case "Convidado":
				ids = eventoRepository.idsCrachaPendenteConvidados(segueMe);
				n = convidadoService.atualizarCracha(ids, false);
				break;
			default:
				return null;
			}
			mensagemFlash(n + " crachá(s) marcado(s) como impresso(s).");
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível marcar os crachás como impressos.");
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	/** Marca como impressos TODOS os crachás pendentes (todos os papéis) do encontro. */
	public String marcarImpressoTodos() {
		if (segueMe == null) {
			return null;
		}
		try {
			int n = seguidorService.atualizarCracha(eventoRepository.idsCrachaPendenteSeguidores(segueMe), false);
			n += seguidorService.atualizarCracha(eventoRepository.idsCrachaPendenteSeguimistas(segueMe), false);
			n += casalService.atualizarCracha(eventoRepository.idsCrachaPendenteCasais(segueMe), false);
			n += padreService.atualizarCracha(eventoRepository.idsCrachaPendentePadres(segueMe), false);
			n += convidadoService.atualizarCracha(eventoRepository.idsCrachaPendenteConvidados(segueMe), false);
			mensagemFlash(n + " crachá(s) marcado(s) como impresso(s).");
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível marcar os crachás como impressos.");
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	/** Desfaz a baixa: volta a PENDENTE TODOS os crachás já impressos (todos os papéis). */
	public String marcarNaoImpressoTodos() {
		if (segueMe == null) {
			return null;
		}
		try {
			int n = seguidorService.atualizarCracha(eventoRepository.idsCrachaImpressoSeguidores(segueMe), true);
			n += seguidorService.atualizarCracha(eventoRepository.idsCrachaImpressoSeguimistas(segueMe), true);
			n += casalService.atualizarCracha(eventoRepository.idsCrachaImpressoCasais(segueMe), true);
			n += padreService.atualizarCracha(eventoRepository.idsCrachaImpressoPadres(segueMe), true);
			n += convidadoService.atualizarCracha(eventoRepository.idsCrachaImpressoConvidados(segueMe), true);
			mensagemFlash(n + " crachá(s) voltaram para a lista de impressão.");
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível desmarcar os crachás.");
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	/** Marca como impressos os crachás (seguidor + casal) pendentes de UMA equipe. */
	public String marcarImpressoEquipe(Integer equipeId, String titulo) {
		if (segueMe == null || equipeId == null) {
			return null;
		}
		try {
			int n = seguidorService.atualizarCracha(eventoRepository.idsCrachaPendenteSeguidoresEquipe(segueMe, equipeId),
					false);
			n += casalService.atualizarCracha(eventoRepository.idsCrachaCasaisEquipe(segueMe, equipeId, true), false);
			mensagemFlash(n + " crachá(s) da " + titulo + " marcado(s) como impresso(s).");
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível marcar os crachás como impressos.");
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	/** Desfaz a baixa: volta a PENDENTE (a imprimir) os crachás (seguidor + casal) de UMA equipe. */
	public String marcarNaoImpressoEquipe(Integer equipeId, String titulo) {
		if (segueMe == null || equipeId == null) {
			return null;
		}
		try {
			int n = seguidorService.atualizarCracha(eventoRepository.idsCrachaImpressoSeguidoresEquipe(segueMe, equipeId),
					true);
			n += casalService.atualizarCracha(eventoRepository.idsCrachaCasaisEquipe(segueMe, equipeId, false), true);
			mensagemFlash(n + " crachá(s) da " + titulo + " voltaram para a lista de impressão.");
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível desmarcar os crachás.");
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	/** Marca como impressos os crachás de seguimista pendentes de UM círculo. */
	public String marcarImpressoCirculo(Integer circuloId, String corLabel) {
		if (segueMe == null || circuloId == null) {
			return null;
		}
		try {
			List<Long> ids = eventoRepository.idsCrachaSeguimistasCirculo(segueMe, circuloId, true);
			int n = seguidorService.atualizarCracha(ids, false);
			mensagemFlash(n + " crachá(s) do círculo " + corLabel + " marcado(s) como impresso(s).");
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível marcar os crachás como impressos.");
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	/** Desfaz a baixa: volta a PENDENTE os crachás de seguimista de UM círculo. */
	public String marcarNaoImpressoCirculo(Integer circuloId, String corLabel) {
		if (segueMe == null || circuloId == null) {
			return null;
		}
		try {
			List<Long> ids = eventoRepository.idsCrachaSeguimistasCirculo(segueMe, circuloId, false);
			int n = seguidorService.atualizarCracha(ids, true);
			mensagemFlash(n + " crachá(s) do círculo " + corLabel + " voltaram para a lista de impressão.");
		} catch (Exception e) {
			FacesUtil.addErrorMessage("Não foi possível desmarcar os crachás.");
			return null;
		}
		return "/dashboard.xhtml?faces-redirect=true";
	}

	/** Mantém a mensagem viva através do redirect (flash scope). */
	private void mensagemFlash(String msg) {
		facesContext.getExternalContext().getFlash().setKeepMessages(true);
		FacesUtil.addInfoMessage(msg);
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

	public boolean isTemCapa() {
		return temCapa;
	}

	public boolean isTemHistoria() {
		return temHistoria;
	}

	public boolean isTemImagem() {
		return temImagem;
	}

	public String getImagemPadroeiroPath() {
		return imagemPadroeiroPath;
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

	public long getTotalAlocadosPessoas() {
		return totalAlocadosPessoas;
	}

	public long getTotalPendentesEquipes() {
		return totalPendentesEquipes;
	}

	public long getVagas() {
		return vagas;
	}

	public int getPctAlocacao() {
		return pctAlocacao;
	}

	public long getAlocadosQuadrante() {
		return alocadosQuadrante;
	}

	public long getExcedenteQuadrante() {
		return excedenteQuadrante;
	}

	public boolean isTemExcedente() {
		return excedenteQuadrante > 0;
	}

	public List<StatusLinha> getSituacoes() {
		return situacoes;
	}

	public long getTotalInscritos() {
		return totalInscritos;
	}

	public List<CirculoLinha> getCirculos() {
		return circulos;
	}

	public long getTotalSeguimistas() {
		return totalSeguimistas;
	}

	public List<String> getSeguimistasSemCirculo() {
		return seguimistasSemCirculo;
	}

	public int getTotalSeguimistasSemCirculo() {
		return seguimistasSemCirculo.size();
	}

	public boolean isTemSeguimistaSemCirculo() {
		return !seguimistasSemCirculo.isEmpty();
	}

	public long getTotalSeguimistasPendentes() {
		return totalSeguimistasPendentes;
	}

	public List<Aniversariante> getAniversariantesSeguimistas() {
		return aniversariantesSeguimistas;
	}

	public List<Aniversariante> getAniversariantesServico() {
		return aniversariantesServico;
	}

	public List<Aniversariante> getAniversariantesHoje() {
		return aniversariantesHoje;
	}

	public boolean isTemAniversarianteHoje() {
		return !aniversariantesHoje.isEmpty();
	}

	public String getMesAtualNome() {
		return mesAtualNome;
	}

	public long getPessoasServindo() {
		return pessoasServindo;
	}

	public long getSeguidoresServindo() {
		return seguidoresServindo;
	}

	public long getCasaisServindo() {
		return casaisServindo;
	}

	public List<Aniversariante> getAniversariantesCasaisNascimento() {
		return aniversariantesCasaisNascimento;
	}

	public List<Aniversariante> getAniversariantesCasamento() {
		return aniversariantesCasamento;
	}

	// ===== DTOs de exibição =====

	public static class Cracha implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String rotulo;
		private final String icone;
		private final String jasperTipo;
		private final String outcomeEquipe;
		private final long total;
		private final long pendentes;

		Cracha(String rotulo, String icone, String jasperTipo, long total, long pendentes, String outcomeEquipe) {
			this.rotulo = rotulo;
			this.icone = icone;
			this.jasperTipo = jasperTipo;
			this.total = total;
			this.pendentes = pendentes;
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

		public String getJasperTipo() {
			return jasperTipo;
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
		private final Integer equipeId;
		private final String titulo;
		private final long previstos;
		private final long alocados;
		private final long casais;
		private final long pendentesSeguidor;
		private final long pendentesCasal;
		private final boolean sua;

		EquipeLinha(Integer equipeId, String titulo, long previstos, long alocados, long casais, long pendentesSeguidor,
				long pendentesCasal, boolean sua) {
			this.equipeId = equipeId;
			this.titulo = titulo;
			this.previstos = previstos;
			this.alocados = alocados;
			this.casais = casais;
			this.pendentesSeguidor = pendentesSeguidor;
			this.pendentesCasal = pendentesCasal;
			this.sua = sua;
		}

		public Integer getEquipeId() {
			return equipeId;
		}

		public String getTitulo() {
			return titulo;
		}

		public long getPrevistos() {
			return previstos;
		}

		/** Seguidores alocados na equipe. */
		public long getAlocados() {
			return alocados;
		}

		/** Casais alocados na equipe. */
		public long getCasais() {
			return casais;
		}

		public boolean isTemCasais() {
			return casais > 0;
		}

		/** Pessoas alocadas = seguidores + casais*2 (mesma base do "previstos"). */
		public long getAlocadosPessoas() {
			return alocados + casais * 2;
		}

		/** Total de crachás da equipe = seguidores + casais (1 crachá por casal). */
		public long getCrachaTotal() {
			return alocados + casais;
		}

		/** Crachás a imprimir = pendentes de seguidor + de casal. */
		public long getPendentes() {
			return pendentesSeguidor + pendentesCasal;
		}

		public long getImpressos() {
			return getCrachaTotal() - getPendentes();
		}

		/** Vagas em aberto na equipe, já descontando os casais (2 por casal). */
		public long getFaltam() {
			return Math.max(0, previstos - getAlocadosPessoas());
		}

		public boolean isCompletaEquipe() {
			return previstos > 0 && getAlocadosPessoas() >= previstos;
		}

		public boolean isCrachaCompleto() {
			return getCrachaTotal() > 0 && getPendentes() == 0;
		}

		public int getPctImpresso() {
			return getCrachaTotal() > 0 ? (int) Math.round(getImpressos() * 100.0 / getCrachaTotal()) : 0;
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

	public static class CirculoLinha implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Integer circuloId;
		private final String nome;
		private final CorCirculo cor;
		private final String corHex;
		private final long total;
		private final long pendentes;

		CirculoLinha(Integer circuloId, String nome, CorCirculo cor, String corHex, long total, long pendentes) {
			this.circuloId = circuloId;
			this.nome = nome;
			this.cor = cor;
			this.corHex = corHex;
			this.total = total;
			this.pendentes = pendentes;
		}

		public Integer getCirculoId() {
			return circuloId;
		}

		public String getNome() {
			return nome;
		}

		public String getCorLabel() {
			return cor != null ? cor.getDescricao() : "—";
		}

		public String getCorHex() {
			return corHex;
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

		public boolean isCrachaCompleto() {
			return pendentes == 0;
		}

		public int getPctImpresso() {
			return total > 0 ? (int) Math.round(getImpressos() * 100.0 / total) : 0;
		}
	}

	public static class Aniversariante implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String nome;
		private final String apelido;
		private final int dia;
		private final String grupo;
		private final CorCirculo cor;
		private final String corHex;
		private final boolean hoje;

		Aniversariante(String nome, String apelido, int dia, String grupo, CorCirculo cor, String corHex,
				boolean hoje) {
			this.nome = nome;
			this.apelido = apelido;
			this.dia = dia;
			this.grupo = grupo;
			this.cor = cor;
			this.corHex = corHex;
			this.hoje = hoje;
		}

		public String getNome() {
			return nome;
		}

		public String getApelido() {
			return apelido;
		}

		public boolean isTemApelido() {
			return apelido != null && !apelido.trim().isEmpty();
		}

		public int getDia() {
			return dia;
		}

		public String getGrupo() {
			return grupo;
		}

		public boolean isTemCor() {
			return cor != null;
		}

		public String getCorHex() {
			return corHex;
		}

		public boolean isHoje() {
			return hoje;
		}
	}
}
