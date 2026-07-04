package com.segue.controller;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.segue.util.AppInfo;

@Named
@ApplicationScoped
public class ApplicationInfoController implements Serializable {

	private static final long serialVersionUID = 1L;

	private String versionApplication;

	/**
	 * Texto exibido no rodapé do login. Formato: {@code Versão X.Y.Z · dd/MM/yyyy · <commit>}.
	 * A data e o commit só aparecem quando o filtering do Maven os resolveu
	 * (ver {@link AppInfo}). Fica em cache — os metadados são fixos por deploy.
	 */
	public String getVersionApplication() {
		if (versionApplication == null) {
			versionApplication = montar();
		}
		return versionApplication;
	}

	private String montar() {
		StringBuilder sb = new StringBuilder("Versão ").append(AppInfo.versao());
		if (!AppInfo.build().isEmpty()) {
			sb.append(" · ").append(AppInfo.build());
		}
		if (!AppInfo.commit().isEmpty()) {
			sb.append(" · ").append(AppInfo.commit());
		}
		return sb.toString();
	}

}
