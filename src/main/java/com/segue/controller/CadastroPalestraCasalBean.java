package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Casal;
import com.segue.model.ECC;
import com.segue.model.Evento;
import com.segue.model.Palestra;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.SituacaoSeguidor;
import com.segue.model.StatusConvite;
import com.segue.model.Usuario;
import com.segue.model.Enum.Estados;
import com.segue.repository.ECCRepository;
import com.segue.repository.PalestraRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroCasalService;
import com.segue.service.CadastroPalestraCasalService;
import com.segue.service.NegocioException;
import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroPalestraCasalBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroPalestraCasalService service;

	@Inject
	private CadastroCasalService cadastroCasalService;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private PalestraRepository palestraRepository;

	@Inject
	private ECCRepository eccRepository;

	private Evento evento;
	private Seguranca seguranca;
	private Usuario usarioLogado;
	private Paroquia paroquia;

	private List<Paroquia> listaParoquia;
	private List<Paroquia> listaParoquiaECC;
	private List<SegueMe> listaSegueMe;
	private List<Palestra> listaPalestra;
	private List<ECC> listaEccs;

	public CadastroPalestraCasalBean() {
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
			this.listaEccs = eccRepository.findByParoquia(this.evento.getCasal().getParoquia());
		}
		if (this.evento.getCasal() != null) {
			this.listaEccs = eccRepository.findByParoquia(this.evento.getCasal().getParoquia());
		}
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.listaPalestra = palestraRepository.listaAtivo();
		this.listaParoquiaECC = paroquiaRepository.listaParoquias();
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
		evento.setCracha(true);
		evento.setCasal(new Casal());
		evento.setPalestra(new Palestra());
		usarioLogado = new Usuario();
		seguranca = new Seguranca();
		this.listaSegueMe = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
		this.listaPalestra = new ArrayList<>();
	}

	public void salvar() throws IOException {
		try {
			this.evento.setUsuario(usarioLogado);
			this.evento = service.salvar(evento);
			limpar();
			carregarUsuarioLogado();
			FacesUtil.addInfoMessage("Salvo com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.evento);
			FacesUtil.addInfoMessage("Palestra " + this.evento.getPalestra().getNome() + " excluído com sucesso.");
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
			this.evento.setEquipe(null);
			this.evento.setFuncao(null);
			this.evento.setCirculo(null);
		}
	}

	public void onEccChange() {
		if (this.evento.getCasal().getParoquia() != null) {
			listaEccs = eccRepository.findByParoquia(this.evento.getCasal().getParoquia());
			this.evento.getCasal().setEcc(null);
		}
	}

	/**
	 * AutoComplete Seguidor
	 */
	public List<Casal> buscaPessoas(String nome) {
		return this.getSeguidorPorNome(nome);
	}

	private List<Casal> getSeguidorPorNome(String nome) {
		List<Casal> casais = cadastroCasalService.findByNome(NomeComInicialMaiscula.iniciaisMaiuscula(nome));
		return casais;
	}

	public boolean isEditando() {
		return this.evento.getId() != null;
	}

	public boolean isSeguidorValido() {
		return this.evento.getCasal().getId() != null;
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

	public void setListaParoquia(List<Paroquia> listaParoquia) {
		this.listaParoquia = listaParoquia;
	}

	public List<ECC> getListaEccs() {
		return listaEccs;
	}

	public void setListaEccs(List<ECC> listaEccs) {
		this.listaEccs = listaEccs;
	}

	public List<Paroquia> getListaParoquiaECC() {
		return listaParoquiaECC;
	}

	public void setListaParoquiaECC(List<Paroquia> listaParoquiaECC) {
		this.listaParoquiaECC = listaParoquiaECC;
	}

	public List<Palestra> getListaPalestra() {
		return listaPalestra;
	}

	public void setListaPalestra(List<Palestra> listaPalestra) {
		this.listaPalestra = listaPalestra;
	}

}
