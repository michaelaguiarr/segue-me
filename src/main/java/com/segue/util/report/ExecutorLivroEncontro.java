package com.segue.util.report;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.hibernate.jdbc.Work;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import com.segue.service.NegocioException;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

/**
 * Gera o "Livro do Encontro": um único PDF concatenando, nesta ordem,
 * <ol>
 * <li>Capa e <li>História — PDFs externos enviados por retiro;
 * <li>Relação de seguimistas, <li>Quadrante de seguidores, <li>Quadrante de
 * palestrantes — relatórios Jasper.
 * </ol>
 * Tudo é lido FRESCO do banco pela {@code connection} do Hibernate (via
 * {@code doWork}): a capa/história por SQL direto (para não depender do
 * {@code SegueMe} do usuário logado, que é um snapshot do login) e os Jasper por
 * {@code fillReport}. A concatenação usa o iText 2.1.7 ({@code com.lowagie}). Seções
 * ausentes (capa/história não enviadas, ou relatório sem dados) são puladas.
 */
public class ExecutorLivroEncontro implements Work {

	private final Long segueMeId;
	private final HttpServletResponse response;
	private final String nomeArquivoSaida;

	private boolean relatorioGerado;

	public ExecutorLivroEncontro(Long segueMeId, HttpServletResponse response, String nomeArquivoSaida) {
		this.segueMeId = segueMeId;
		this.response = response;
		this.nomeArquivoSaida = nomeArquivoSaida;
	}

	@Override
	public void execute(Connection connection) throws SQLException {
		try {
			List<byte[]> pdfs = new ArrayList<>();

			// 1-Capa e 2-História (PDFs externos, lidos frescos do banco)
			byte[][] externos = carregarArquivosExternos(connection);
			if (ehPdf(externos[0])) {
				pdfs.add(externos[0]);
			}
			if (ehPdf(externos[1])) {
				pdfs.add(externos[1]);
			}

			// 3-Relação de seguimistas, 4-Quadrante de seguidores, 5-Quadrante de palestrantes
			adicionarJasper(pdfs, "/jasper/relacaoSeguimista.jasper", paramsBase(), connection);
			adicionarJasper(pdfs, "/jasper/quadranteSeguidorLayout.jasper", paramsQuadrante(), connection);
			adicionarJasper(pdfs, "/jasper/quadrantePalestraLayout.jasper", paramsQuadrante(), connection);

			this.relatorioGerado = !pdfs.isEmpty();
			if (!this.relatorioGerado) {
				throw new NegocioException("Nada para gerar — sem capa/história e sem dados nos relatórios.");
			}

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + this.nomeArquivoSaida + "\"");

			concatenar(pdfs, response.getOutputStream());
		} catch (Exception e) {
			throw new SQLException("Erro ao gerar o livro do encontro " + e.getMessage(), e);
		}
	}

	/** Lê os bytes da capa e da história direto da tabela (fresco, não do entity cacheado). */
	private byte[][] carregarArquivosExternos(Connection connection) throws SQLException {
		try (PreparedStatement ps = connection
				.prepareStatement("SELECT arquivo_capa, arquivo_historia FROM segue_me WHERE id = ?")) {
			ps.setLong(1, segueMeId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new byte[][] { rs.getBytes(1), rs.getBytes(2) };
				}
			}
		}
		return new byte[][] { null, null };
	}

	/** Preenche um relatório Jasper na connection e adiciona seu PDF à lista (pula se vazio). */
	private void adicionarJasper(List<byte[]> pdfs, String caminho, Map<String, Object> params, Connection connection)
			throws Exception {
		InputStream stream = getClass().getResourceAsStream(caminho);
		JasperPrint print = JasperFillManager.fillReport(stream, params, connection);
		if (print.getPages().isEmpty()) {
			return;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JRPdfExporter exporter = new JRPdfExporter();
		exporter.setExporterInput(new SimpleExporterInput(print));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
		exporter.exportReport();
		pdfs.add(baos.toByteArray());
	}

	/** Concatena vários PDFs (bytes) num só, preservando todas as páginas, via iText 2.1.7. */
	private void concatenar(List<byte[]> pdfs, OutputStream out) throws Exception {
		Document documento = new Document();
		PdfCopy copy = new PdfCopy(documento, out);
		documento.open();
		for (byte[] pdf : pdfs) {
			PdfReader reader = new PdfReader(pdf);
			int totalPaginas = reader.getNumberOfPages();
			for (int pagina = 1; pagina <= totalPaginas; pagina++) {
				copy.addPage(copy.getImportedPage(reader, pagina));
			}
			copy.freeReader(reader);
			reader.close();
		}
		documento.close();
	}

	private Map<String, Object> paramsBase() {
		Map<String, Object> params = new HashMap<>();
		params.put(JRParameter.REPORT_LOCALE, new Locale("pt", "BR"));
		params.put("segueMe", segueMeId);
		return params;
	}

	private Map<String, Object> paramsQuadrante() {
		Map<String, Object> params = paramsBase();
		params.put("pagina", 0);
		return params;
	}

	/** Confere a assinatura %PDF para um upload errado não corromper o merge. */
	private boolean ehPdf(byte[] bytes) {
		return bytes != null && bytes.length > 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D'
				&& bytes[3] == 'F';
	}

	public boolean isRelatorioGerado() {
		return relatorioGerado;
	}
}
