package com.segue.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Metadados da build — versão, data e hash do commit — lidos uma única vez de
 * {@code /application.properties}, que o Maven filtra a partir de
 * {@code ${project.version}}, {@code ${maven.build.timestamp}} e
 * {@code ${git.commit.id.abbrev}} (ver {@code <resources>} e o plugin
 * git-commit-id no pom.xml).
 *
 * Valores que o filtering não resolveu (ex.: build sem git ou sem filtering)
 * ficam com {@code "${...}"} no arquivo e são devolvidos como string vazia.
 * Usado tanto pelo rodapé do login ({@code ApplicationInfoController}) quanto
 * pelo {@code HealthCheckServlet}.
 */
public final class AppInfo {

	private static final Properties PROPS = carregar();

	private AppInfo() {
	}

	private static Properties carregar() {
		Properties p = new Properties();
		try (InputStream in = AppInfo.class.getResourceAsStream("/application.properties")) {
			if (in != null) {
				p.load(in);
			}
		} catch (IOException e) {
			// mantém vazio — os getters devolvem ""
		}
		return p;
	}

	private static String resolvido(String chave) {
		String valor = PROPS.getProperty(chave);
		if (valor == null || valor.contains("${")) {
			return "";
		}
		return valor.trim();
	}

	public static String versao() {
		return resolvido("versao");
	}

	public static String build() {
		return resolvido("build");
	}

	public static String commit() {
		return resolvido("commit");
	}

}
