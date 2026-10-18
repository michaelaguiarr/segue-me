package com.segue.util.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;

import com.segue.model.SegueMe;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

/**
 * Gera, via Java2D (sem depender de modelo Jasper), a imagem do "santinho do
 * quadrante": um cartão de 15 × 14&nbsp;cm pra imprimir e colar o QR Code do
 * link público do quadrante — pensado pra seguimistas e coordenadores, PNG a
 * 300dpi pronto pra gráfica. Layout aprovado em prévia com o usuário:
 * <ol>
 * <li>faixa "QUADRANTE DO ENCONTRO" no topo;
 * <li>círculo com a imagem da padroeira, nome, lema (opcional) e dados do
 * retiro (número/paróquia/data);
 * <li>linha pontilhada de "talão";
 * <li>rodapé com dois "selos" do mesmo tamanho — logo do Segue-Me e QR Code
 * de verdade — com uma chamada centralizada entre eles.
 * </ol>
 */
public final class GeradorCartaoQuadrante {

	private static final float MM = 300f / 25.4f; // px por mm, a 300dpi
	private static final float PT = 300f / 72f; // px por pt, a 300dpi

	private static final int LARGURA_PX = Math.round(150 * MM); // 15cm
	private static final int ALTURA_PX = Math.round(140 * MM); // 14cm

	private static final Color COR_FUNDO = new Color(0xff, 0xfa, 0xf1);
	private static final Color COR_FAIXA_1 = new Color(0xc9, 0x69, 0x0a);
	private static final Color COR_FAIXA_2 = new Color(0xe0, 0x83, 0x0b);
	private static final Color COR_FAIXA_3 = new Color(0xf0, 0xa2, 0x3a);
	private static final Color COR_FAIXA_TEXTO = new Color(0xff, 0xf8, 0xec);
	private static final Color COR_ANEL_BORDA = new Color(0xf0, 0xa2, 0x3a);
	private static final Color COR_ANEL_1 = new Color(0xff, 0xfd, 0xf8);
	private static final Color COR_ANEL_2 = new Color(0xff, 0xf1, 0xdc);
	private static final Color COR_ANEL_3 = new Color(0xf7, 0xd9, 0xa3);
	private static final Color COR_TEXTO = new Color(0x2b, 0x20, 0x18);
	private static final Color COR_LEMA = new Color(0x8a, 0x3d, 0x0a);
	private static final Color COR_SUBTEXTO = new Color(0x7a, 0x6a, 0x58);
	private static final Color COR_ACCENT = new Color(0xc9, 0x69, 0x0a);
	private static final Color COR_PERF = new Color(0xe3, 0xc7, 0x9a);
	private static final Color COR_RODAPE_BG = new Color(0xfd, 0xf1, 0xdd);
	private static final Color COR_SELO_BORDA = new Color(0xe0, 0x83, 0x0b);

	private static final String[] MESES = { "JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO", "JULHO",
			"AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO" };

	private static final Font BASE_EXTRABOLD = carregarFonte("/fonts/Poppins-ExtraBold.ttf", Font.SANS_SERIF);
	private static final Font BASE_BOLD = carregarFonte("/fonts/Poppins-Bold.ttf", Font.SANS_SERIF);
	private static final Font BASE_SEMIBOLD = carregarFonte("/fonts/Poppins-SemiBold.ttf", Font.SANS_SERIF);
	private static final Font BASE_MEDIUM = carregarFonte("/fonts/Poppins-Medium.ttf", Font.SANS_SERIF);
	private static final Font BASE_LEMA = carregarFonte("/fonts/Spectral-MediumItalic.ttf", Font.SERIF);

	private GeradorCartaoQuadrante() {
	}

	public static byte[] gerar(SegueMe segueMe, String linkPublico, BufferedImage padroeiraImg,
			BufferedImage logoImg) throws IOException {
		BufferedImage img = new BufferedImage(LARGURA_PX, ALTURA_PX, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2.setColor(COR_FUNDO);
		g2.fillRect(0, 0, LARGURA_PX, ALTURA_PX);

		float faixaH = mm(11f);
		desenharFaixaTopo(g2, faixaH);

		float rodapeH = mm(30f);
		float perfY = ALTURA_PX - rodapeH;
		desenharZonaTopo(g2, segueMe, padroeiraImg, faixaH, perfY);
		desenharPerforacao(g2, perfY);
		desenharRodape(g2, logoImg, linkPublico, perfY, rodapeH);

		g2.dispose();

		ByteArrayOutputStream saida = new ByteArrayOutputStream();
		ImageIO.write(img, "png", saida);
		return saida.toByteArray();
	}

	// ===== Faixa do topo =====

	private static void desenharFaixaTopo(Graphics2D g2, float altura) {
		LinearGradientPaint gradiente = new LinearGradientPaint(new Point2D.Float(0, 0),
				new Point2D.Float(LARGURA_PX, altura * 0.4f), new float[] { 0f, 0.55f, 1f },
				new Color[] { COR_FAIXA_1, COR_FAIXA_2, COR_FAIXA_3 });
		g2.setPaint(gradiente);
		g2.fillRect(0, 0, LARGURA_PX, Math.round(altura));

		Font fonte = BASE_EXTRABOLD.deriveFont(pt(10.5f));
		g2.setColor(COR_FAIXA_TEXTO);
		desenharTextoEspacado(g2, "QUADRANTE DO ENCONTRO", fonte, pt(0.9f), LARGURA_PX / 2f, altura / 2f + pt(3.6f));
	}

	// ===== Zona de identidade (padroeira, nome, lema, dados) =====

	private static void desenharZonaTopo(Graphics2D g2, SegueMe segueMe, BufferedImage padroeiraImg, float topoY,
			float baseY) {
		float centroX = LARGURA_PX / 2f;
		float larguraDisponivel = baseY - topoY;

		// monta os blocos de cima pra baixo, depois centraliza o conjunto verticalmente
		float diametroAnel = mm(37f);
		float gap = mm(2.3f);

		Font fNome = BASE_BOLD.deriveFont(pt(11f));
		FontMetrics fmNome = g2.getFontMetrics(fNome);

		Font fLema = BASE_LEMA.deriveFont(pt(15f));
		FontMetrics fmLema = g2.getFontMetrics(fLema);
		List<String> linhasLema = quebrarLinhas(segueMe.getLema(), fmLema, mm(96f));

		Font fRetiro = BASE_SEMIBOLD.deriveFont(pt(9f));
		FontMetrics fmRetiro = g2.getFontMetrics(fRetiro);

		Font fData = BASE_SEMIBOLD.deriveFont(pt(7.4f));

		float alturaBloco = diametroAnel + gap + fmNome.getHeight()
				+ (linhasLema.isEmpty() ? 0 : gap + linhasLema.size() * fmLema.getHeight() * 1.22f) + gap
				+ fmRetiro.getHeight() + gap + pt(7.4f) * 1.3f;

		float y = topoY + Math.max(mm(5.4f), (larguraDisponivel - alturaBloco) / 2f);

		// crest-ring
		float cy = y + diametroAnel / 2f;
		RadialGradientPaint anelGrad = new RadialGradientPaint(new Point2D.Float(centroX, cy), diametroAnel / 2f,
				new float[] { 0f, 0.6f, 1f }, new Color[] { COR_ANEL_1, COR_ANEL_2, COR_ANEL_3 });
		Ellipse2D anel = new Ellipse2D.Float(centroX - diametroAnel / 2f, y, diametroAnel, diametroAnel);
		g2.setPaint(anelGrad);
		g2.fill(anel);
		g2.setStroke(new BasicStroke(pt(1.6f)));
		g2.setColor(COR_ANEL_BORDA);
		g2.draw(anel);
		Shape clipAnterior = g2.getClip();
		g2.setClip(anel);
		desenharImagemContida(g2, padroeiraImg, centroX, cy, diametroAnel * 0.9f);
		g2.setClip(clipAnterior);
		y += diametroAnel + gap;

		// nome do padroeiro
		g2.setColor(COR_TEXTO);
		String nome = segueMe.getPadroeiro() != null ? segueMe.getPadroeiro().toUpperCase() : "";
		desenharTextoEspacado(g2, nome, fNome, pt(0.7f), centroX, y + fmNome.getAscent());
		y += fmNome.getHeight();

		// lema (opcional)
		if (!linhasLema.isEmpty()) {
			y += gap;
			g2.setColor(COR_LEMA);
			g2.setFont(fLema);
			for (String linha : linhasLema) {
				desenharTextoCentralizado(g2, linha, fLema, centroX, y + fmLema.getAscent());
				y += fmLema.getHeight() * 1.22f;
			}
		}

		// linha do retiro (número + paróquia)
		y += gap;
		String numero = segueMe.getNumeroRomano() != null ? segueMe.getNumeroRomano().getNumeroRomano() : "";
		String paroquia = segueMe.getParoquia() != null && segueMe.getParoquia().getDescricao() != null
				? segueMe.getParoquia().getDescricao().toUpperCase()
				: "";
		g2.setFont(fRetiro);
		String linhaRetiro = (numero + " SEGUE-ME").trim();
		float wEsq = larguraTextoEspacado(g2, linhaRetiro, fRetiro, pt(0.6f));
		float raioSep = pt(1.6f);
		float gapSep = pt(5f);
		float wSep = gapSep * 2 + raioSep * 2;
		float wDir = larguraTextoEspacado(g2, "PARÓQUIA " + paroquia, fRetiro, pt(0.6f));
		float xIni = centroX - (wEsq + wSep + wDir) / 2f;
		g2.setColor(COR_SUBTEXTO);
		float avancoX = desenharTextoEspacadoEsq(g2, linhaRetiro, fRetiro, pt(0.6f), xIni, y + fmRetiro.getAscent());
		avancoX += gapSep;
		// separador desenhado (não é glifo de fonte — evita "tofu box" se o caractere não existir na fonte)
		g2.setColor(COR_ACCENT);
		g2.fill(new Ellipse2D.Float(avancoX, y + fmRetiro.getAscent() - raioSep * 1.5f, raioSep * 2, raioSep * 2));
		avancoX += raioSep * 2 + gapSep;
		g2.setColor(COR_SUBTEXTO);
		desenharTextoEspacadoEsq(g2, "PARÓQUIA " + paroquia, fRetiro, pt(0.6f), avancoX, y + fmRetiro.getAscent());
		y += fmRetiro.getHeight();

		// linha da data
		y += gap;
		String dataTexto = formatarPeriodo(segueMe.getDtInicio(), segueMe.getDtFim());
		if (!dataTexto.isEmpty()) {
			g2.setColor(COR_ACCENT);
			g2.setFont(fData);
			FontMetrics fmData = g2.getFontMetrics();
			float iconeW = pt(8f);
			float textoW = larguraTextoEspacado(g2, dataTexto, fData, pt(0.6f));
			float xData = centroX - (iconeW + pt(2f) + textoW) / 2f;
			desenharIconeCalendario(g2, xData, y + fmData.getAscent() - pt(6.2f), pt(6.6f));
			desenharTextoEspacadoEsq(g2, dataTexto, fData, pt(0.6f), xData + iconeW + pt(2f), y + fmData.getAscent());
		}
	}

	// ===== Linha pontilhada (talão) =====

	private static void desenharPerforacao(Graphics2D g2, float y) {
		g2.setColor(COR_PERF);
		float[] tracos = { pt(4f), pt(3f) };
		g2.setStroke(new BasicStroke(pt(1.6f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, tracos, 0f));
		g2.drawLine(Math.round(mm(7f)), Math.round(y), Math.round(LARGURA_PX - mm(7f)), Math.round(y));
	}

	// ===== Rodapé: selos (logo / QR) + chamada =====

	private static void desenharRodape(Graphics2D g2, BufferedImage logoImg, String linkPublico, float topoY,
			float altura) {
		g2.setColor(COR_RODAPE_BG);
		g2.fillRect(0, Math.round(topoY), LARGURA_PX, Math.round(altura));

		float padH = mm(7.5f);
		float seloLado = mm(19f);
		float centroY = topoY + mm(4.4f) + seloLado / 2f;

		// selo da logo (esquerda)
		float xLogo = padH;
		desenharSelo(g2, xLogo, centroY - seloLado / 2f, seloLado, logoImg, mm(2.2f), "SEGUE-ME");

		// selo do QR (direita)
		float xQr = LARGURA_PX - padH - seloLado;
		BufferedImage qr = gerarQrCode(linkPublico, Math.round(seloLado * 0.82f));
		desenharSelo(g2, xQr, centroY - seloLado / 2f, seloLado, qr, mm(1f), "TOQUE AQUI");

		// chamada central
		float xTextoIni = xLogo + seloLado + mm(4f);
		float xTextoFim = xQr - mm(4f);
		float centroTextoX = (xTextoIni + xTextoFim) / 2f;
		float larguraTexto = xTextoFim - xTextoIni;

		Font fLabel = BASE_EXTRABOLD.deriveFont(pt(9.4f));
		FontMetrics fmLabel = g2.getFontMetrics(fLabel);
		List<String> linhasLabel = quebrarLinhas("Escaneie e veja tudo sobre o encontro", fmLabel, larguraTexto);

		Font fSub = BASE_MEDIUM.deriveFont(pt(7.1f));
		FontMetrics fmSub = g2.getFontMetrics(fSub);
		List<String> linhasSub = quebrarLinhas(
				"Relação de seguimistas, seguidores e palestrantes — e um pouco da história da padroeira.", fmSub,
				larguraTexto);

		float alturaTotalTexto = linhasLabel.size() * fmLabel.getHeight() + mm(1f)
				+ linhasSub.size() * fmSub.getHeight() * 1.32f;
		float yTexto = centroY - alturaTotalTexto / 2f;

		g2.setColor(COR_TEXTO);
		g2.setFont(fLabel);
		for (String linha : linhasLabel) {
			desenharTextoCentralizado(g2, linha, fLabel, centroTextoX, yTexto + fmLabel.getAscent());
			yTexto += fmLabel.getHeight();
		}
		yTexto += mm(1f);
		g2.setColor(COR_SUBTEXTO);
		g2.setFont(fSub);
		for (String linha : linhasSub) {
			desenharTextoCentralizado(g2, linha, fSub, centroTextoX, yTexto + fmSub.getAscent());
			yTexto += fmSub.getHeight() * 1.32f;
		}
	}

	private static void desenharSelo(Graphics2D g2, float x, float y, float lado, BufferedImage conteudo,
			float paddingInterno, String legenda) {
		RoundRectangle2D caixa = new RoundRectangle2D.Float(x, y, lado, lado, mm(1.4f), mm(1.4f));
		g2.setColor(Color.WHITE);
		g2.fill(caixa);
		g2.setStroke(new BasicStroke(pt(1.4f)));
		g2.setColor(COR_SELO_BORDA);
		g2.draw(caixa);
		if (conteudo != null) {
			float areaUtil = lado - paddingInterno * 2;
			desenharImagemContida(g2, conteudo, x + lado / 2f, y + lado / 2f, areaUtil);
		}

		Font fCaption = BASE_BOLD.deriveFont(pt(6.4f));
		g2.setColor(COR_ACCENT);
		desenharTextoEspacado(g2, legenda, fCaption, pt(0.3f), x + lado / 2f, y + lado + mm(1.4f) + pt(5f));
	}

	/**
	 * Gera só a imagem do QR Code (sem o cartão completo — sem fontes
	 * customizadas, sem imagem da padroeira), pra quando a coordenação só quer
	 * imprimir/colar o código em algo próprio. Bem mais leve/rápido que
	 * {@link #gerar} porque não toca em {@code Font.createFont} nem em imagens.
	 */
	public static byte[] gerarApenasQr(String link, int tamanhoPx) throws IOException {
		return QRCode.from(link).to(ImageType.PNG).withSize(tamanhoPx, tamanhoPx).stream().toByteArray();
	}

	private static BufferedImage gerarQrCode(String link, int tamanhoPx) {
		try {
			ByteArrayOutputStream stream = QRCode.from(link).to(ImageType.PNG).withSize(tamanhoPx, tamanhoPx)
					.stream();
			return ImageIO.read(new ByteArrayInputStream(stream.toByteArray()));
		} catch (Exception e) {
			return null; // some do selo em branco, mas não derruba a geração do cartão
		}
	}

	// ===== Data (3 casos: mesmo mês / cruza o mês / cruza o ano) =====

	private static String formatarPeriodo(java.util.Date inicio, java.util.Date fim) {
		if (inicio == null) {
			return "";
		}
		Calendar ci = Calendar.getInstance();
		ci.setTime(inicio);
		if (fim == null) {
			return pad(ci.get(Calendar.DAY_OF_MONTH)) + " DE " + MESES[ci.get(Calendar.MONTH)] + " DE "
					+ ci.get(Calendar.YEAR);
		}
		Calendar cf = Calendar.getInstance();
		cf.setTime(fim);
		int di = ci.get(Calendar.DAY_OF_MONTH), df = cf.get(Calendar.DAY_OF_MONTH);
		int mi = ci.get(Calendar.MONTH), mf = cf.get(Calendar.MONTH);
		int yi = ci.get(Calendar.YEAR), yf = cf.get(Calendar.YEAR);
		if (yi == yf && mi == mf) {
			return pad(di) + " A " + pad(df) + " DE " + MESES[mi] + " DE " + yi;
		} else if (yi == yf) {
			return pad(di) + " DE " + MESES[mi] + " A " + pad(df) + " DE " + MESES[mf] + " DE " + yi;
		}
		return pad(di) + " DE " + MESES[mi] + " DE " + yi + " A " + pad(df) + " DE " + MESES[mf] + " DE " + yf;
	}

	private static String pad(int dia) {
		return dia < 10 ? "0" + dia : String.valueOf(dia);
	}

	// ===== Utilitários de desenho =====

	private static void desenharIconeCalendario(Graphics2D g2, float x, float y, float lado) {
		RoundRectangle2D corpo = new RoundRectangle2D.Float(x, y, lado, lado, lado * 0.18f, lado * 0.18f);
		g2.setStroke(new BasicStroke(pt(0.9f)));
		g2.draw(corpo);
		g2.drawLine(Math.round(x), Math.round(y + lado * 0.38f), Math.round(x + lado), Math.round(y + lado * 0.38f));
		g2.drawLine(Math.round(x + lado * 0.26f), Math.round(y - lado * 0.14f), Math.round(x + lado * 0.26f),
				Math.round(y + lado * 0.14f));
		g2.drawLine(Math.round(x + lado * 0.74f), Math.round(y - lado * 0.14f), Math.round(x + lado * 0.74f),
				Math.round(y + lado * 0.14f));
	}

	/** Desenha a imagem "contida" (sem cortar) num quadrado de {@code lado}, centralizada em (cx, cy). */
	private static void desenharImagemContida(Graphics2D g2, BufferedImage imagem, float cx, float cy, float lado) {
		if (imagem == null) {
			return;
		}
		float escala = Math.min(lado / imagem.getWidth(), lado / imagem.getHeight());
		int w = Math.round(imagem.getWidth() * escala);
		int h = Math.round(imagem.getHeight() * escala);
		int x = Math.round(cx - w / 2f);
		int y = Math.round(cy - h / 2f);
		g2.drawImage(imagem, x, y, w, h, null);
	}

	private static void desenharTextoCentralizado(Graphics2D g2, String texto, Font fonte, float centerX,
			float baselineY) {
		g2.setFont(fonte);
		FontMetrics fm = g2.getFontMetrics();
		g2.drawString(texto, centerX - fm.stringWidth(texto) / 2f, baselineY);
	}

	private static float larguraTextoEspacado(Graphics2D g2, String texto, Font fonte, float espacamento) {
		FontMetrics fm = g2.getFontMetrics(fonte);
		float largura = 0;
		for (int i = 0; i < texto.length(); i++) {
			largura += fm.charWidth(texto.charAt(i));
			if (i < texto.length() - 1) {
				largura += espacamento;
			}
		}
		return largura;
	}

	private static void desenharTextoEspacado(Graphics2D g2, String texto, Font fonte, float espacamento,
			float centerX, float baselineY) {
		float largura = larguraTextoEspacado(g2, texto, fonte, espacamento);
		desenharTextoEspacadoEsq(g2, texto, fonte, espacamento, centerX - largura / 2f, baselineY);
	}

	/** @return a posição X logo após o último caractere desenhado (útil pra encadear trechos). */
	private static float desenharTextoEspacadoEsq(Graphics2D g2, String texto, Font fonte, float espacamento,
			float x, float baselineY) {
		g2.setFont(fonte);
		FontMetrics fm = g2.getFontMetrics();
		float cursor = x;
		for (int i = 0; i < texto.length(); i++) {
			char c = texto.charAt(i);
			g2.drawString(String.valueOf(c), cursor, baselineY);
			cursor += fm.charWidth(c) + espacamento;
		}
		return cursor;
	}

	private static List<String> quebrarLinhas(String texto, FontMetrics fm, float larguraMaxPx) {
		List<String> linhas = new ArrayList<>();
		if (texto == null || texto.trim().isEmpty()) {
			return linhas;
		}
		String[] palavras = texto.trim().split("\\s+");
		StringBuilder atual = new StringBuilder();
		for (String palavra : palavras) {
			String tentativa = atual.length() == 0 ? palavra : atual + " " + palavra;
			if (fm.stringWidth(tentativa) > larguraMaxPx && atual.length() > 0) {
				linhas.add(atual.toString());
				atual = new StringBuilder(palavra);
			} else {
				atual = new StringBuilder(tentativa);
			}
		}
		if (atual.length() > 0) {
			linhas.add(atual.toString());
		}
		return linhas;
	}

	private static float mm(float valor) {
		return valor * MM;
	}

	private static float pt(float valor) {
		return valor * PT;
	}

	private static Font carregarFonte(String caminhoClasspath, String fallbackLogico) {
		try (InputStream in = GeradorCartaoQuadrante.class.getResourceAsStream(caminhoClasspath)) {
			if (in == null) {
				return new Font(fallbackLogico, Font.PLAIN, 1);
			}
			return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(1f);
		} catch (Exception e) {
			return new Font(fallbackLogico, Font.PLAIN, 1);
		}
	}
}
