package com.segue.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DualListModel;

import com.segue.model.AplicacaoModulo;
import com.segue.model.Perfil;
import com.segue.repository.AplicacaoModuloRepository;
import com.segue.service.CadastroPerfilService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroPerfilBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroPerfilService service;

	@Inject
	private AplicacaoModuloRepository moduloRepository;

	private Perfil perfil;

	private DualListModel<AplicacaoModulo> listaModulos;

	public CadastroPerfilBean() {
		limpar();
	}

	public void inicializar() {
		List<AplicacaoModulo> lista = moduloRepository.findAll();
		if (this.perfil == null) {
			limpar();
			this.listaModulos = new DualListModel<>(lista, new ArrayList<>());
		} else {
			lista.removeAll(this.perfil.getModulos());
			this.listaModulos = new DualListModel<>(lista, new ArrayList<>(this.perfil.getModulos()));
		}
	}

	private void limpar() {
		perfil = new Perfil();
	}

	public void salvar() {
		try {
			perfil.setModulos(listaModulos.getTarget());
			this.perfil = service.salvar(perfil);
			List<AplicacaoModulo> lista = moduloRepository.findAll();
			listaModulos = new DualListModel<>(lista, new ArrayList<>());
			limpar();
			FacesUtil.addInfoMessage("Perfil Salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.perfil);
			FacesUtil.addInfoMessage("Perfil " + this.perfil.getNome() + " excluída com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public boolean isEditando() {
		return this.perfil.getId() != null;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}

	public DualListModel<AplicacaoModulo> getListaModulos() {
		return listaModulos;
	}

	public void setListaModulos(DualListModel<AplicacaoModulo> listaModulos) {
		this.listaModulos = listaModulos;
	}

}
