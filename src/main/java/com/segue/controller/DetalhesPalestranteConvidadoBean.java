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
import com.segue.model.PalestranteConvidado;
import com.segue.repository.EventoRepository;
import com.segue.util.Constants;

@Named
@ViewScoped
public class DetalhesPalestranteConvidadoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository eventoRepository;

	private PalestranteConvidado palestranteConvidado;

	private List<Evento> listaEvento;

	public DetalhesPalestranteConvidadoBean() {
		limpar();
	}

	public void inicializar() {
		if (this.palestranteConvidado == null) {
			limpar();
		} else {
			listaEvento = eventoRepository.filtradosHistoricoConvidado(palestranteConvidado);
		}
	}

	private void limpar() {
		palestranteConvidado = new PalestranteConvidado();
		listaEvento = new ArrayList<>();
	}

	public void editar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/palestranteConvidado/cadastro-convidado.xhtml?convidado=" + palestranteConvidado.getId());
	}

	public PalestranteConvidado getPalestranteConvidado() {
		return palestranteConvidado;
	}

	public void setPalestranteConvidado(PalestranteConvidado palestranteConvidado) {
		this.palestranteConvidado = palestranteConvidado;
	}

	public List<Evento> getListaEvento() {
		return listaEvento;
	}

	public void setListaEvento(List<Evento> listaEvento) {
		this.listaEvento = listaEvento;
	}

}
