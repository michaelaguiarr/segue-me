package com.segue.util.report;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.hibernate.jdbc.Work;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

/**
 * Gera os crachás de UMA equipe unindo, num único PDF, os relatórios informados
 * (tipicamente o de seguidor e o de casal por equipe). Cada relatório é preenchido
 * na mesma {@code connection} do Hibernate; os que voltarem VAZIOS (ex.: o de
 * seguidor numa equipe só de casais) são simplesmente pulados — nunca lança erro
 * por falta de dados, ao contrário do {@link ExecutorRelatorioDownload}. A junção
 * usa o iText 2.1.7 ({@code com.lowagie}).
 */
public class ExecutorCrachaEquipe implements Work {

	private final List<String> relatorios;
	private final Map<String, Object> parametros;
	private final HttpServletResponse response;
	private final String nomeArquivoSaida;

	private boolean relatorioGerado;

	public ExecutorCrachaEquipe(List<String> relatorios, Map<String, Object> parametros, HttpServletResponse response,
			String nomeArquivoSaida) {
		this.relatorios = relatorios;
		this.parametros = parametros;
		this.response = response;
		this.nomeArquivoSaida = nomeArquivoSaida;
	}

	@Override
	public void execute(Connection connection) throws SQLException {
		try {
			List<byte[]> pdfs = new ArrayList<>();
			for (String caminho : relatorios) {
				InputStream stream = getClass().getResourceAsStream(caminho);
				if (stream == null) {
					continue;
				}
				JasperPrint print = JasperFillManager.fillReport(stream, parametros, connection);
				if (print.getPages().isEmpty()) {
					continue;
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				JRPdfExporter exporter = new JRPdfExporter();
				exporter.setExporterInput(new SimpleExporterInput(print));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
				exporter.exportReport();
				pdfs.add(baos.toByteArray());
			}

			this.relatorioGerado = !pdfs.isEmpty();
			if (!this.relatorioGerado) {
				return; // nada a imprimir nesta equipe — o controller apenas não completa a resposta
			}

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + this.nomeArquivoSaida + "\"");
			concatenar(pdfs, response.getOutputStream());
		} catch (Exception e) {
			throw new SQLException("Erro ao gerar os crachás da equipe " + e.getMessage(), e);
		}
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

	public boolean isRelatorioGerado() {
		return relatorioGerado;
	}
}
