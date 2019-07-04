package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Casal;
import com.segue.model.Evento;
import com.segue.repository.EventoRepository;
import com.segue.util.Constants;

@Named
@ViewScoped
public class DetalhesCasalBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EventoRepository eventoRepository;

	private Casal casal;

	private List<Evento> listaEvento;

	public DetalhesCasalBean() {
		limpar();
	}

	public void inicializar() {
		if (this.casal == null) {
			limpar();
		} else {
			listaEvento = eventoRepository.filtradosHistoricoCasal(casal);
		}
	}

	private void limpar() {
		casal = new Casal();
		listaEvento = new ArrayList<>();
	}

	public void editar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/casal/cadastro-casal.xhtml?casal=" + casal.getId());
	}
	
	public void editarPublic() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/casal-public/cadastro-casal.xhtml?casal=" + casal.getId());
	}

	public Casal getCasal() {
		return casal;
	}

	public void setCasal(Casal casal) {
		this.casal = casal;
	}

	public List<Evento> getListaEvento() {
		return listaEvento;
	}

	public void setListaEvento(List<Evento> listaEvento) {
		this.listaEvento = listaEvento;
	}

}
