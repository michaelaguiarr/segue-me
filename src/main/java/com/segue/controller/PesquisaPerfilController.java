package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Perfil;
import com.segue.service.PerfilService;

@Named
@ViewScoped
public class PesquisaPerfilController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PerfilService perfilService;

	private Perfil perfil;

	private List<Perfil> listaPerfil;

	@PostConstruct
	public void init() {
		perfil = new Perfil();
		listaPerfil = perfilService.findAll();
	}

	/**
	 * Seleciona perfil na tabela e redireciona para pagina cadastro-perfil
	 */
	public void perfilSelecionado() {
		String context = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();

		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(context + "/perfil/cadastro-perfil.xhtml?id=" + perfil.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	/**
	 * Atualiza lista
	 */
	public void limpar() {
		perfil = new Perfil();
		listaPerfil = perfilService.findAll();
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}

	public List<Perfil> getListaPerfil() {
		return listaPerfil;
	}

}
