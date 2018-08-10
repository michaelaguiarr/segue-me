package com.segue.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.ECC;
import com.segue.model.NumeroRomano;
import com.segue.model.Paroquia;
import com.segue.model.Enum.Estados;
import com.segue.repository.NumeroRomanoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.service.CadastroEccService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEccBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroEccService service;

	@Inject
	private ParoquiaRepository paroquias;

	@Inject
	private NumeroRomanoRepository numeroRomanoRepoditory;

	private ECC ecc;

	private List<Paroquia> paroquiasFiltro;

	private List<NumeroRomano> numeroRomanosFiltro;

	public CadastroEccBean() {
		limpar();
	}

	public void inicializar() {
		if (this.ecc == null) {
			limpar();
		}
		paroquiasFiltro = paroquias.listaParoquias();
		numeroRomanosFiltro = numeroRomanoRepoditory.listaNumerosRomanos();
	}

	private void limpar() {
		ecc = new ECC();
	}

	public void salvar() {
		try {
			this.ecc = service.salvar(this.ecc);
			limpar();
			FacesUtil.addInfoMessage("Evento ECC salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.ecc);
			FacesUtil.addInfoMessage("Evento " + this.ecc.getTitulo() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public Estados[] getEstados() {
		return Estados.values();
	}

	public ECC getEcc() {
		return ecc;
	}

	public void setEcc(ECC ecc) {
		this.ecc = ecc;
	}

	public List<Paroquia> getParoquiasFiltro() {
		return paroquiasFiltro;
	}

	public List<NumeroRomano> getNumeroRomanosFiltro() {
		return numeroRomanosFiltro;
	}

	public boolean isEditando() {
		return this.ecc.getId() != null;
	}

}
