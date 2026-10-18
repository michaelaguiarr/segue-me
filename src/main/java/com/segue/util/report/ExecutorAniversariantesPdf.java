package com.segue.util.report;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.segue.controller.DashboardBean.Aniversariante;
import com.segue.model.SegueMe;

/**
 * Monta, direto com iText 2.1.7 ({@code com.lowagie}) a partir dos dados já
 * carregados no {@code DashboardBean}, o PDF dos aniversariantes do mês —
 * no MESMO estilo "do encontro" dos demais relatórios "com layout"
 * ({@code relacaoSeguimistaCirculo.jrxml}, {@code quadranteSeguidorLayout.jrxml}):
 * logo + foto do padroeiro no cabeçalho, nome/paróquia/padroeiro do retiro,
 * fundo e rodapé do Segue-Me. Diferente dos demais relatórios, não depende de
 * um modelo Jasper (.jrxml/.jasper) — os dados já estão em memória, então não
 * há necessidade de reconsultar o banco nem de recompilar relatório algum; as
 * MESMAS imagens materializadas por {@code FotoService.materializarImagensSegueMe}
 * (usadas pelos relatórios Jasper) são lidas direto do disco pelo caminho físico.
 */
public final class ExecutorAniversariantesPdf {

	private static final Color COR_TITULO = new Color(0xb9, 0x6c, 0x08);
	private static final Color COR_TEXTO = new Color(0x2b, 0x20, 0x18);
	private static final Color COR_SUBTITULO = new Color(0x6e, 0x5f, 0x52);
	private static final Color COR_VAZIO = new Color(0x9c, 0x8d, 0x7e);
	private static final Color COR_CABECALHO_BG = new Color(0xf6, 0xf1, 0xe9);
	private static final Color COR_HOJE_BG = new Color(0xfb, 0xee, 0xd8);
	private static final String FONTE = "Comic Sans MS";

	private ExecutorAniversariantesPdf() {
	}

	public static byte[] gerar(SegueMe segueMe, String webappRoot, String mesAtualNome,
			List<Aniversariante> seguimistas, List<Aniversariante> servico, List<Aniversariante> casaisNascimento,
			List<Aniversariante> casamento) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document documento = new Document(PageSize.A4, 36, 36, 185, 65);
		PdfWriter writer = PdfWriter.getInstance(documento, baos);
		writer.setPageEvent(new CabecalhoRodapeEncontro(segueMe, webappRoot, "Aniversariantes de " + mesAtualNome));
		documento.open();

		adicionarSecao(documento, "Seguimistas", "Círculo", seguimistas);
		adicionarSecao(documento, "Seguidores de serviço", "Equipe", servico);
		adicionarSecao(documento, "Casais de serviço — aniversário", "Equipe", casaisNascimento);
		adicionarSecao(documento, "Casais de serviço — aniversário de casamento", "Equipe", casamento);

		documento.close();
		return baos.toByteArray();
	}

	private static void adicionarSecao(Document documento, String titulo, String rotuloGrupo,
			List<Aniversariante> lista) throws Exception {
		Font fSecao = FontFactory.getFont(FONTE, 13, Font.BOLD, COR_TEXTO);
		documento.add(new Paragraph(titulo + " (" + lista.size() + ")", fSecao));

		if (lista.isEmpty()) {
			Font fVazio = FontFactory.getFont(FONTE, 10, Font.ITALIC, COR_VAZIO);
			documento.add(new Paragraph("Nenhum aniversariante neste mês.", fVazio));
			documento.add(new Paragraph(" "));
			return;
		}

		PdfPTable tabela = new PdfPTable(new float[] { 1f, 4f, 3f });
		tabela.setWidthPercentage(100);
		tabela.setSpacingBefore(6);
		tabela.setSpacingAfter(14);

		Font fCabecalho = FontFactory.getFont(FONTE, 8.5f, Font.BOLD, COR_VAZIO);
		adicionarCelulaCabecalho(tabela, "Dia", fCabecalho);
		adicionarCelulaCabecalho(tabela, "Nome", fCabecalho);
		adicionarCelulaCabecalho(tabela, rotuloGrupo, fCabecalho);

		Font fCelula = FontFactory.getFont(FONTE, 10, COR_TEXTO);
		Font fCelulaBold = FontFactory.getFont(FONTE, 10, Font.BOLD, COR_TEXTO);
		Font fApelido = FontFactory.getFont(FONTE, 9, COR_VAZIO);
		Font fHoje = FontFactory.getFont(FONTE, 9, Font.BOLD, COR_TITULO);

		for (Aniversariante a : lista) {
			Color bg = a.isHoje() ? COR_HOJE_BG : null;

			adicionarCelula(tabela, String.valueOf(a.getDia()), a.isHoje() ? fCelulaBold : fCelula, bg);

			Paragraph nome = new Paragraph();
			nome.add(new Chunk(a.getNome(), a.isHoje() ? fCelulaBold : fCelula));
			if (a.isTemApelido()) {
				nome.add(new Chunk(" (" + a.getApelido() + ")", fApelido));
			}
			if (a.isHoje()) {
				nome.add(new Chunk("  •  hoje!", fHoje));
			}
			PdfPCell celNome = new PdfPCell(nome);
			celNome.setPadding(5);
			celNome.setBorderColor(new Color(0xeb, 0xe3, 0xd6));
			if (bg != null) {
				celNome.setBackgroundColor(bg);
			}
			tabela.addCell(celNome);

			adicionarCelula(tabela, a.getGrupo() != null && !a.getGrupo().trim().isEmpty() ? a.getGrupo() : "—",
					fCelula, bg);
		}

		documento.add(tabela);
	}

	private static void adicionarCelulaCabecalho(PdfPTable tabela, String texto, Font fonte) {
		PdfPCell cel = new PdfPCell(new Paragraph(texto, fonte));
		cel.setPadding(5);
		cel.setBackgroundColor(COR_CABECALHO_BG);
		cel.setBorderColor(new Color(0xeb, 0xe3, 0xd6));
		tabela.addCell(cel);
	}

	private static void adicionarCelula(PdfPTable tabela, String texto, Font fonte, Color bg) {
		PdfPCell cel = new PdfPCell(new Paragraph(texto, fonte));
		cel.setPadding(5);
		cel.setBorderColor(new Color(0xeb, 0xe3, 0xd6));
		if (bg != null) {
			cel.setBackgroundColor(bg);
		}
		tabela.addCell(cel);
	}

	/**
	 * Cabeçalho (logo + foto do padroeiro + nome/paróquia/padroeiro do retiro +
	 * título do relatório), fundo e rodapé repetidos em toda página — réplica, em
	 * iText, do {@code pageHeader}/{@code background}/{@code pageFooter} que os
	 * relatórios "com layout" (.jrxml) usam. As imagens são as MESMAS já
	 * materializadas em disco por {@code FotoService.materializarImagensSegueMe}
	 * ({@code resources/temp/segueMe|segueMeFundo|segueMeRoda/<id>.jpg}) — se
	 * alguma não existir (retiro sem imagem anexada), é simplesmente omitida, sem
	 * quebrar a geração (mesmo comportamento do {@code onErrorType="Blank"} do
	 * Jasper).
	 */
	private static class CabecalhoRodapeEncontro extends PdfPageEventHelper {

		private final SegueMe segueMe;
		private final String tituloRelatorio;
		private final Image logo;
		private final Image padroeiro;
		private final Image fundo;
		private final Image rodape;

		CabecalhoRodapeEncontro(SegueMe segueMe, String webappRoot, String tituloRelatorio) {
			this.segueMe = segueMe;
			this.tituloRelatorio = tituloRelatorio;
			File raiz = new File(webappRoot);
			this.logo = carregarImagem(new File(raiz, "resources/images/segue-me.png"));
			this.padroeiro = carregarImagem(new File(raiz, "resources/temp/segueMe/" + segueMe.getId() + ".jpg"));
			this.fundo = carregarImagem(new File(raiz, "resources/temp/segueMeFundo/" + segueMe.getId() + ".jpg"));
			this.rodape = carregarImagem(new File(raiz, "resources/temp/segueMeRoda/" + segueMe.getId() + ".jpg"));
		}

		private static Image carregarImagem(File arquivo) {
			try {
				if (!arquivo.isFile()) {
					return null;
				}
				return Image.getInstance(arquivo.getAbsolutePath());
			} catch (Exception e) {
				return null; // imagem ausente/inválida: segue sem ela, sem abortar o PDF
			}
		}

		@Override
		public void onStartPage(PdfWriter writer, Document document) {
			if (fundo == null) {
				return;
			}
			try {
				PdfContentByte under = writer.getDirectContentUnder();
				float pw = document.getPageSize().getWidth();
				float ph = document.getPageSize().getHeight();
				Image img = Image.getInstance(fundo);
				img.scaleToFit(pw - 40, ph - 210);
				img.setAbsolutePosition((pw - img.getScaledWidth()) / 2, 55);
				under.addImage(img);
			} catch (Exception ignored) {
				// fundo é só decorativo — nunca derruba a geração do PDF
			}
		}

		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			try {
				PdfContentByte cb = writer.getDirectContent();
				float pw = document.getPageSize().getWidth();
				float ph = document.getPageSize().getHeight();
				float centerX = pw / 2;
				float topY = ph - 24;

				if (logo != null) {
					Image img = Image.getInstance(logo);
					img.scaleToFit(72, 54);
					img.setAbsolutePosition(36, topY - img.getScaledHeight());
					cb.addImage(img);
				}
				if (padroeiro != null) {
					Image img = Image.getInstance(padroeiro);
					img.scaleToFit(54, 54);
					img.setAbsolutePosition(pw - 36 - img.getScaledWidth(), topY - img.getScaledHeight());
					cb.addImage(img);
				}

				Font fLinha1 = FontFactory.getFont(FONTE, 15, Font.BOLD, COR_TITULO);
				Font fLinha = FontFactory.getFont(FONTE, 9.5f, COR_SUBTITULO);
				float y = topY - 8;

				String numero = segueMe.getNumeroRomano() != null ? segueMe.getNumeroRomano().getNumeroRomano() : "";
				ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(numero + " SEGUE-ME", fLinha1),
						centerX, y, 0);
				y -= 16;
				if (segueMe.getParoquia() != null && segueMe.getParoquia().getDescricao() != null) {
					ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
							new Phrase("Paróquia " + segueMe.getParoquia().getDescricao(), fLinha), centerX, y, 0);
					y -= 12;
				}
				if (segueMe.getPadroeiro() != null && !segueMe.getPadroeiro().trim().isEmpty()) {
					ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(segueMe.getPadroeiro(), fLinha),
							centerX, y, 0);
					y -= 12;
				}
				if (segueMe.getNomeclatura() != null && !segueMe.getNomeclatura().trim().isEmpty()) {
					ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
							new Phrase(segueMe.getNomeclatura() + " do Encontro", fLinha), centerX, y, 0);
					y -= 12;
				}

				y -= 8;
				Font fTituloRel = FontFactory.getFont(FONTE, 18, Font.BOLD, COR_TEXTO);
				ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(tituloRelatorio, fTituloRel), centerX,
						y, 0);

				y -= 10;
				cb.setLineWidth(1f);
				cb.setColorStroke(COR_TITULO);
				cb.moveTo(36, y);
				cb.lineTo(pw - 36, y);
				cb.stroke();

				if (rodape != null) {
					Image img = Image.getInstance(rodape);
					img.scaleToFit(pw - 72, 34);
					img.setAbsolutePosition((pw - img.getScaledWidth()) / 2, 20);
					cb.addImage(img);
				}
			} catch (Exception ignored) {
				// cabeçalho/rodapé são decorativos — nunca derruba a geração do PDF
			}
		}
	}
}
