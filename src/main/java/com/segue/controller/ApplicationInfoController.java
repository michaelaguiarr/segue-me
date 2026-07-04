package com.segue.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class ApplicationInfoController implements Serializable {

	private static final long serialVersionUID = 1L;

	private String versionApplication;

	/**
	 * Versão exibida no rodapé do login. Lida de {@code /application.properties},
	 * que o Maven filtra a partir de {@code ${project.version}} e
	 * {@code ${maven.build.timestamp}} (ver bloco {@code <resources>} do pom.xml).
	 *
	 * Fica em cache (bean {@code @ApplicationScoped}) — a versão é fixa por deploy.
	 * Não lança exceção para a EL e esconde valores não resolvidos pelo filtering.
	 */
	public String getVersionApplication() {
		if (versionApplication == null) {
			versionApplication = carregarVersao();
		}
		return versionApplication;
	}

	private String carregarVersao() {
		Properties props = new Properties();
		try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
			if (in != null) {
				props.load(in);
			}
		} catch (IOException e) {
			return "";
		}

		String versao = valorResolvido(props.getProperty("versao"));
		String build = valorResolvido(props.getProperty("build"));

		StringBuilder sb = new StringBuilder("Versão ").append(versao);
		if (!build.isEmpty()) {
			sb.append(" · ").append(build);
		}
		return sb.toString();
	}

	/** Ignora valores que o filtering do Maven não resolveu (ex.: "${...}"). */
	private String valorResolvido(String valor) {
		if (valor == null || valor.contains("${")) {
			return "";
		}
		return valor.trim();
	}

}
