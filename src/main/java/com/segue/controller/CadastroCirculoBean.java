package com.segue.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Circulo;
import com.segue.model.CorCirculo;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Usuario;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroCirculoService;
import com.segue.service.NegocioException;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroCirculoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroCirculoService service;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	private Seguranca seguranca;
	private Usuario usuarioLogado;
	private Circulo circulo;
	private Paroquia paroquia;

	private List<Paroquia> listaParaquia;
	private List<SegueMe> listaSegueMe;

	public CadastroCirculoBean() {
		limpar();
	}

	public void inicializar() {
		if (this.circulo == null) {
			limpar();
			this.circulo.setSegueMe(this.usuarioLogado.getSegueMe());	
		}
		this.seguranca = new Seguranca();
		this.usuarioLogado = this.seguranca.usuarioLogado();
		this.listaParaquia = paroquiaRepository.listaParoquias();
		this.paroquia = this.usuarioLogado.getSegueMe().getParoquia();
		this.listaSegueMe = segueMeRepository.findByParoquia(paroquia);
		
	}

	private void limpar() {
		this.listaParaquia = new ArrayList<>();
		this.listaSegueMe = new ArrayList<>();
		this.circulo = new Circulo();
		this.seguranca = new Seguranca();
		this.usuarioLogado = new Usuario();
		this.paroquia = new Paroquia();
	}

	public void salvar() {
		try {
			this.circulo = service.salvar(circulo);
			limpar();
			inicializar();
			FacesUtil.addInfoMessage("Círculo salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.circulo);
			FacesUtil.addInfoMessage(
					"Círculo " + this.circulo.getCorCirculo().getDescricao() + " excluído com sucesso.");
			limpar();
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	/**
	 * Carregar Segue-me;
	 */
	public void onSegueMeChange() {
		if (this.paroquia != null) {
			listaSegueMe = segueMeRepository.findByParoquia(paroquia);
			this.circulo.setSegueMe(null);
		}
	}

	public boolean isEditando() {
		return this.circulo.getId() != null;
	}

	public CorCirculo[] getCorCirculo() {
		return CorCirculo.values();
	}

	public Circulo getCirculo() {
		return circulo;
	}

	public void setCirculo(Circulo circulo) {
		this.circulo = circulo;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public List<Paroquia> getListaParaquia() {
		return listaParaquia;
	}

	public void setListaParaquia(List<Paroquia> listaParaquia) {
		this.listaParaquia = listaParaquia;
	}

	public List<SegueMe> getListaSegueMe() {
		return listaSegueMe;
	}

	public void setListaSegueMe(List<SegueMe> listaSegueMe) {
		this.listaSegueMe = listaSegueMe;
	}

}
