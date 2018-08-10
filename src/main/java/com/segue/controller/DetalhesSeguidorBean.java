package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Evento;
import com.segue.model.Seguidor;
import com.segue.repository.EventoRepository;
import com.segue.util.Constants;

@Named
@ViewScoped
public class DetalhesSeguidorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository eventoRepository;

	private Seguidor seguidor;

	private List<Evento> listaEvento;

	public DetalhesSeguidorBean() {
		limpar();
	}

	public void inicializar() {
		if (this.seguidor == null) {
			limpar();
		} else {
			listaEvento = eventoRepository.filtradosHistorico(seguidor);
		}
	}

	private void limpar() {
		seguidor = new Seguidor();
		listaEvento = new ArrayList<>();
	}

	public void editar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/seguidor/cadastro-seguidor.xhtml?seguidor=" + seguidor.getId());
	}
	
	public Seguidor getSeguidor() {
		return seguidor;
	}

	public void setSeguidor(Seguidor seguidor) {
		this.seguidor = seguidor;
	}

	public List<Evento> getListaEvento() {
		return listaEvento;
	}

	public void setListaEvento(List<Evento> listaEvento) {
		this.listaEvento = listaEvento;
	}

}
