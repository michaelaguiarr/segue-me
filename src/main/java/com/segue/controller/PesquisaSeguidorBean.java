package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.segue.filter.SeguidorFilter;
import com.segue.model.Circulo;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.model.Usuario;
import com.segue.repository.CirculoRepository;
import com.segue.repository.ParoquiaRepository;
import com.segue.repository.SegueMeRepository;
import com.segue.repository.SeguidorRepository;
import com.segue.security.Seguranca;
import com.segue.service.CadastroSeguidorService;
import com.segue.util.Constants;
import com.segue.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaSeguidorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SeguidorRepository repository;

	@Inject
	private SegueMeRepository segueMeRepository;

	@Inject
	private ParoquiaRepository paroquiaRepository;

	@Inject
	private CirculoRepository circuloRepository;

	@Inject
	private CadastroSeguidorService service;

	private Seguranca seguranca;
	private Usuario usuarioLogado;
	private Seguidor seguidor;

	private SeguidorFilter filter;

	private List<Seguidor> listaSeguidor;
	private LazyDataModel<Seguidor> modelo;
	private List<Paroquia> listaParoquia;
	private List<SegueMe> listaSegueMe;
	private List<Circulo> listaCirculo;

	private boolean novoSeguidor = false;

	public PesquisaSeguidorBean() {
		seguidor = new Seguidor();
		filter = new SeguidorFilter();
		this.listaSegueMe = new ArrayList<>();
		this.listaSeguidor = new ArrayList<>();
		this.listaParoquia = new ArrayList<>();
		this.listaCirculo = new ArrayList<>();
		this.novoSeguidor = false;

	}

	public void inicializar() {
		this.seguranca = new Seguranca();
		if (this.seguranca.usuarioLogado() != null) {
			this.usuarioLogado = this.seguranca.usuarioLogado();
			this.listaParoquia = paroquiaRepository.listaParoquias();
			filter.setParoquia(this.usuarioLogado.getSegueMe().getParoquia());
			this.listaSegueMe = segueMeRepository.findByParoquia(filter.getParoquia());
			filter.setSegueMe(this.usuarioLogado.getSegueMe());
			listaCirculo = circuloRepository.findBySegueMe(filter.getSegueMe());
			this.modelo = criarModelo();
			this.novoSeguidor = false;
		}

	}

	/**
	 * Cria o modelo de paginação server-side: cada página é buscada sob demanda
	 * ({@code repository.filtradosPaginado}) e o total via {@code repository.contar},
	 * sempre com o filtro atual — só 25 linhas por vez vêm para a memória.
	 */
	private LazyDataModel<Seguidor> criarModelo() {
		return new LazyDataModel<Seguidor>() {
			private static final long serialVersionUID = 1L;

			@Override
			public List<Seguidor> load(int first, int pageSize, String sortField, SortOrder sortOrder,
					Map<String, Object> filters) {
				setRowCount(repository.contar(filter).intValue());
				boolean asc = sortOrder != SortOrder.DESCENDING;
				return repository.filtradosPaginado(filter, first, pageSize, sortField, asc);
			}

			/**
			 * Converte o rowKey (Seguidor.id) de volta no objeto ao selecionar a linha.
			 * Obrigatório em LazyDataModel: o PrimeFaces não usa o algoritmo básico de
			 * rowKey para modelos lazy. Buscamos na página já carregada para reaproveitar
			 * a projeção e não recarregar a imagem (blob EAGER).
			 */
			@Override
			@SuppressWarnings("unchecked")
			public Seguidor getRowData(String rowKey) {
				List<Seguidor> pagina = (List<Seguidor>) getWrappedData();
				if (pagina != null) {
					for (Seguidor seguidor : pagina) {
						if (seguidor.getId() != null && seguidor.getId().toString().equals(rowKey)) {
							return seguidor;
						}
					}
				}
				return null;
			}
		};
	}

	/**
	 * Pesquisa usuario pelo nome, paroquia, Segue-me e Equipe
	 */
	public void pesquisar() {
		// Paginação server-side: recriar o modelo reinicia na 1ª página e
		// re-consulta com o filtro atual. Lista vazia é tratada pelo emptyMessage.
		this.modelo = criarModelo();
	}

	public void pesquisarPublic() {
		if (filter.getTelefone().isEmpty()) {
			FacesUtil.addErrorMessage("Informe o Telefone completo!");
		} else {
			listaSeguidor = repository.filtradosPublic(this.filter);
			if (listaSeguidor.isEmpty()) {
				this.novoSeguidor = true;
				FacesUtil.addErrorMessage("Nenhum Resultado Encontrado!");
			}else {
				this.novoSeguidor = false;
			}
		}
	}

	public void atulizarNome() {
		try {
			List<Seguidor> list = this.repository.findAll();
			for (Seguidor s : list) {
				service.atualizarNome(s);
			}
			inicializar();
			FacesUtil.addInfoMessage("Deu Certo.Atulizado com sucesso!");
		} catch (Exception e) {
			e.printStackTrace();
			FacesUtil.addErrorMessage("Deu merda!");
		}
	}

	/**
	 * Carrega o seguidor completo (com a foto) sob demanda ao abrir o diálogo de
	 * foto. A listagem é projetada sem o blob {@code imagem} por performance, então
	 * aqui recarregamos o registro pelo id para exibir a imagem.
	 */
	public void carregarFoto(Integer id) {
		this.seguidor = repository.findById(id);
	}

	/**
	 * Carregar Segue-me;
	 */
	public void onSegueMeChange() {
		this.filter.setSegueMe(null);
		this.filter.setCirculo(null);
		this.listaCirculo = new ArrayList<>();
		if (this.filter.getParoquia() != null) {
			listaSegueMe = segueMeRepository.findByParoquia(this.filter.getParoquia());
			this.filter.setSegueMe(null);
			this.filter.setCirculo(null);
		}
	}

	/**
	 * Carregar funcao;
	 */
	public void onCirculoChange() {
		if (this.filter.getSegueMe() != null) {
			listaCirculo = circuloRepository.findBySegueMe(filter.getSegueMe());
			this.filter.setCirculo(null);
		}
	}

	/**
	 * Seleciona bairro na lista
	 * 
	 * @throws IOException
	 */
	public void selecionar() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/seguidor/detalhe-seguidor.xhtml?seguidor=" + seguidor.getId());
	}

	public void selecionarPublic() throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(Constants.CONTEXT + "/seguidor-public/detalhe-seguidor.xhtml?seguidor=" + seguidor.getId());
	}

	public List<Paroquia> getListaParoquia() {
		return listaParoquia;
	}

	public void setListaParoquia(List<Paroquia> listaParoquia) {
		this.listaParoquia = listaParoquia;
	}

	public List<SegueMe> getListaSegueMe() {
		return listaSegueMe;
	}

	public void setListaSegueMe(List<SegueMe> listaSegueMe) {
		this.listaSegueMe = listaSegueMe;
	}

	public Seguidor getSeguidor() {
		return seguidor;
	}

	public void setSeguidor(Seguidor seguidor) {
		this.seguidor = seguidor;
	}

	public SeguidorFilter getFilter() {
		return filter;
	}

	public void setFilter(SeguidorFilter filter) {
		this.filter = filter;
	}

	public List<Seguidor> getListaSeguidor() {
		return listaSeguidor;
	}

	public void setListaSeguidor(List<Seguidor> listaSeguidor) {
		this.listaSeguidor = listaSeguidor;
	}

	public LazyDataModel<Seguidor> getModelo() {
		return modelo;
	}

	public List<Circulo> getListaCirculo() {
		return listaCirculo;
	}

	public void setListaCirculo(List<Circulo> listaCirculo) {
		this.listaCirculo = listaCirculo;
	}

	public boolean isNovoSeguidor() {
		return novoSeguidor;
	}

	public void setNovoSeguidor(boolean novoSeguidor) {
		this.novoSeguidor = novoSeguidor;
	}

	public boolean isNovo() {
		return this.listaSeguidor != null && !this.listaSeguidor.isEmpty();
	}


}
