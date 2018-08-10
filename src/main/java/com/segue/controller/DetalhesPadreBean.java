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
import com.segue.model.Padre;
import com.segue.repository.EventoRepository;
import com.segue.util.Constants;

@Named
@ViewScoped
public class DetalhesPadreBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository eventoRepository;

	private Padre padre;

	private List<Evento> listaEvento;

	public DetalhesPadreBean() {
		limpar();
	}

	public void inicializar() {
		if (this.padre == null) {
			limpar();
		} else {
			listaEvento = eventoRepository.filtradosHistoricoPadre(padre);
		}
	}

	private void limpar() {
		padre = new Padre();
		listaEvento = new ArrayList<>();
	}

	public void editar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/padre/cadastro-padre.xhtml?padre=" + padre.getId());
	}

	public List<Evento> getListaEvento() {
		return listaEvento;
	}

	public void setListaEvento(List<Evento> listaEvento) {
		this.listaEvento = listaEvento;
	}

	public Padre getPadre() {
		return padre;
	}

	public void setPadre(Padre padre) {
		this.padre = padre;
	}

}
