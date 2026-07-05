package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.segue.model.Circulo;
import com.segue.model.Equipe;
import com.segue.model.Evento;
import com.segue.model.Funcao;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.model.Sexo;
import com.segue.model.SituacaoSeguidor;
import com.segue.model.StatusConvite;
import com.segue.model.Usuario;
import com.segue.model.Enum.Estados;
import com.segue.repository.CirculoRepository;
import com.segue.repository.EquipeRepository;
import com.segue.repository.FuncaoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.repository.SexoRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroEventoSeguidorService;
import com.segue.service.CadastroSeguidorService;
import com.segue.service.NegocioException;
import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEventoSeguidorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastroEventoSeguidorService service;

	@Inject
	private CadastroSeguidorService cadastroSeguidorService;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private SexoRepository sexoRepository;

	@Inject
	private EquipeRepository equipeRepository;

	@Inject
	private FuncaoRepository funcaoRepository;

	@Inject
	private CirculoRepository circuloRepository;

	private Evento evento;
	private Seguranca seguranca;
	private Usuario usarioLogado;
	private Paroquia paroquia;
	private boolean exibirCirculo;

	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Equipe> listaEquipes;
	private List<Funcao> listaFuncaos;
	private List<Sexo> listaSexo = new ArrayList<>();
	private List<Circulo> listaCirculo = new ArrayList<>();

	public CadastroEventoSeguidorBean() {
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
			listaFuncaos = funcaoRepository.findByEquipe(this.evento.getEquipe());
			if (this.evento.getSegueMe() != null && this.evento.getFuncao().getId() == 58l
					|| this.evento.getFuncao().getId() == 59l) {
				listaCirculo = circuloRepository.findBySegueMe(this.evento.getSegueMe());
				exibirCirculo = true;
			}
		}
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.listaSexo = sexoRepository.findAll();
		this.listaEquipes = equipeRepository.listaALL();
	}

	public void carregarUsuarioLogado() {
		this.seguranca = new Seguranca();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.listaSexo = sexoRepository.findAll();
		this.listaEquipes = equipeRepository.listaALL();
		this.usarioLogado = this.seguranca.usuarioLogado();
		this.listaParoquia = paroquiaRepository.listaParoquias();
		this.paroquia = this.usarioLogado.getSegueMe().getParoquia();
		this.listaSegueMe = segueMeRepository.findByParoquia(paroquia);
		this.evento.setSegueMe(usarioLogado.getSegueMe());
		if (!seguranca.isGraficaAndComandante()) {
			this.evento.setEquipe(usarioLogado.getEquipe());
			listaFuncaos = funcaoRepository.findByEquipe(usarioLogado.getEquipe());
		}
	}

	private void limpar() {
		paroquia = new Paroquia();
		evento = new Evento();
		evento.setSeguidor(new Seguidor());
		evento.setCracha(true);
		exibirCirculo = false;
		usarioLogado = new Usuario();
		seguranca = new Seguranca();
		this.listaSegueMe = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
		this.listaEquipes = new ArrayList<>();
		this.listaFuncaos = new ArrayList<>();
	}

	public void salvar() throws IOException {
		try {
			this.evento.setUsuario(usarioLogado);
			this.evento = service.salvar(evento);
			limpar();
			carregarUsuarioLogado();
			FacesUtil.addInfoMessage("Ficha salva com sucesso!");
		} catch (NegocioException ne) {
			FacesUtil.addErrorMessage(ne.getMessage());
		}
	}

	public void excluir() {
		try {
			service.remover(this.evento);
			FacesUtil.addInfoMessage("Seguidor " + this.evento.getSeguidor().getApelido() + " excluído com sucesso.");
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
			exibirCirculo = false;
		}
	}

	/**
	 * Carregar funcao;
	 */
	public void onEquipeChange() {
		if (this.evento.getEquipe() != null) {
			listaFuncaos = funcaoRepository.findByEquipe(this.evento.getEquipe());
			this.evento.setFuncao(null);
			this.evento.setCirculo(null);
			exibirCirculo = false;
		}
	}

	/**
	 * Carregar funcao;
	 */
	public void onCirculoChange() {
		if (this.evento.getFuncao() != null) {
			if (this.evento.getSegueMe() != null && this.evento.getFuncao().getId() == 58l
					|| this.evento.getFuncao().getId() == 59l) {
				listaCirculo = circuloRepository.findBySegueMe(this.evento.getSegueMe());
				this.evento.setCirculo(null);
				exibirCirculo = true;
			}
		}
	}

	/**
	 * AutoComplete do seguidor: busca por nome, apelido ou telefone. O termo cru é
	 * enviado ao repositório, que normaliza tanto o nome quanto os dígitos do
	 * telefone.
	 */
	public List<Seguidor> buscaPessoas(String termo) {
		return cadastroSeguidorService.findByNome(termo);
	}


	/**
	 * AutoComplete Seguidor
	 */
	public List<Seguidor> buscaPessoasFicha(String nome) {
		return this.getSeguidorPorNomeFicha(nome);
	}

	private List<Seguidor> getSeguidorPorNomeFicha(String nome) {
		List<Seguidor> seguidores = cadastroSeguidorService.findByNomeFicha(NomeComInicialMaiscula.iniciaisMaiuscula(nome));
		return seguidores;
	}

	public boolean isEditando() {
		return this.evento.getId() != null;
	}

	public boolean isSeguidorValido() {
		return this.evento.getSeguidor().getId() != null;
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

	public List<Sexo> getListaSexo() {
		return listaSexo;
	}

	public List<Equipe> getListaEquipes() {
		return listaEquipes;
	}

	public void setListaEquipes(List<Equipe> listaEquipes) {
		this.listaEquipes = listaEquipes;
	}

	public List<Funcao> getListaFuncaos() {
		return listaFuncaos;
	}

	public void setListaFuncaos(List<Funcao> listaFuncaos) {
		this.listaFuncaos = listaFuncaos;
	}

	public void setListaParoquia(List<Paroquia> listaParoquia) {
		this.listaParoquia = listaParoquia;
	}

	public void setListaSexo(List<Sexo> listaSexo) {
		this.listaSexo = listaSexo;
	}

	public boolean isExibirCirculo() {
		return exibirCirculo;
	}

	public void setExibirCirculo(boolean exibirCirculo) {
		this.exibirCirculo = exibirCirculo;
	}

	public List<Circulo> getListaCirculo() {
		return listaCirculo;
	}

	public void setListaCirculo(List<Circulo> listaCirculo) {
		this.listaCirculo = listaCirculo;
	}

}
