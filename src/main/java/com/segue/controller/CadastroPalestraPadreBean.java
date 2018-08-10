package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Evento;
import com.segue.model.Padre;
import com.segue.model.Palestra;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.SituacaoSeguidor;
import com.segue.model.StatusConvite;
import com.segue.model.Usuario;
import com.segue.model.Enum.Estados;
import com.segue.repository.PalestraRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroPadreService;
import com.segue.service.CadastroPalestraPadreService;
import com.segue.service.NegocioException;
import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroPalestraPadreBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroPalestraPadreService service;

	@Inject
	private CadastroPadreService cadastroPadreService;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private PalestraRepository palestraRepository;

	private Evento evento;
	private Seguranca seguranca;
	private Usuario usarioLogado;
	private Paroquia paroquia;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Palestra> listaPalestras;

	public CadastroPalestraPadreBean() {
		limpar();
	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		if (this.evento == null) {
			limpar();
			carregarUsuarioLogado();
			this.paroquia = this.evento.getSegueMe().getParoquia();
			this.listaSegueMe = segueMeRepository.findByParoquia(this.paroquia);
		} else {
			this.seguranca = new Seguranca();
			this.usarioLogado = this.seguranca.usuarioLogado();
			this.paroquia = this.evento.getSegueMe().getParoquia();
			this.listaSegueMe = segueMeRepository.findByParoquia(this.paroquia);
		}
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.listaPalestras = palestraRepository.listaAtivo();
	}

	public void carregarUsuarioLogado() {
		this.seguranca = new Seguranca();
		this.usarioLogado = this.seguranca.usuarioLogado();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.paroquia = this.usarioLogado.getSegueMe().getParoquia();
		this.listaSegueMe = segueMeRepository.findByParoquia(paroquia);
		this.evento.setSegueMe(usarioLogado.getSegueMe());
	}

	private void limpar() {
		paroquia = new Paroquia();
		evento = new Evento();
		evento.setPadre(new Padre());
		evento.setCracha(true);
		usarioLogado = new Usuario();
		seguranca = new Seguranca();
		this.listaSegueMe = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
	}

	public void salvar() throws IOException {
		try {
			this.evento.setUsuario(usarioLogado);
			this.evento = service.salvar(evento);
			inicializar();
			FacesUtil.addInfoMessage("Padre/Bispo salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.evento);
			FacesUtil.addInfoMessage("Palestrante " + this.evento.getPadre().getApelido() + " excluído com sucesso.");
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
			this.evento.setSegueMe(null);
		}
	}

	/**
	 * AutoComplete Seguidor
	 */
	public List<Padre> buscaPessoas(String nome) {
		return this.getPadrePorNome(nome);
	}

	private List<Padre> getPadrePorNome(String nome) {
		List<Padre> seguidores = cadastroPadreService.findByNome(NomeComInicialMaiscula.iniciaisMaiuscula(nome));
		return seguidores;
	}

	public boolean isEditando() {
		return this.evento.getId() != null;
	}

	public boolean isPadreValido() {
		return this.evento.getPadre().getId() != null;
	}

	public SituacaoSeguidor[] getSituacaoSeguidor() {
		return SituacaoSeguidor.values();
	}

	public Estados[] getEstados() {
		return Estados.values();
	}

	public StatusConvite[] getStatusConvites() {
		return StatusConvite.values();
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public List<SegueMe> getListaSegueMe() {
		return listaSegueMe;
	}

	public void setListaSegueMe(List<SegueMe> listaSegueMe) {
		this.listaSegueMe = listaSegueMe;
	}

	public List<Paroquia> getListaParoquia() {
		return listaParoquia;
	}

	public List<Palestra> getListaPalestras() {
		return listaPalestras;
	}

	public void setListaPalestras(List<Palestra> listaPalestras) {
		this.listaPalestras = listaPalestras;
	}

	public void setListaParoquia(List<Paroquia> listaParoquia) {
		this.listaParoquia = listaParoquia;
	}

}
