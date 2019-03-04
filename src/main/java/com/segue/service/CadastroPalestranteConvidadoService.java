package com.segue.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.segue.model.PalestranteConvidado;
import com.segue.repository.PalestranteConvidadoRepository;
import com.segue.util.jpa.Transactional;

public class CadastroPalestranteConvidadoService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PalestranteConvidadoRepository repository;

	@Transactional
	public PalestranteConvidado salvar(PalestranteConvidado palestranteConvidado) throws NegocioException {
		PalestranteConvidado seguidorExiste = repository.findByNomeDtNascimento(palestranteConvidado.getNome(),
				palestranteConvidado.getDataNascimento());
		if (seguidorExiste != null && !seguidorExiste.equals(palestranteConvidado)) {
			throw new NegocioException("Palestrante Convidado já cadastrado no sistema!Por favor faça uma pesquisa");
		}
		if (palestranteConvidado.getImagem() == null) {
			throw new NegocioException("Informe a foto!");
		}
		return repository.guardar(palestranteConvidado);
	}

	@Transactional
	public void remover(PalestranteConvidado seguidor) throws NegocioException {
		repository.remover(seguidor);
	}

	
	/**
	 * Busca seguidor por nome
	 * 
	 * @return
	 */
	public List<PalestranteConvidado> findByNome(String nome) {
		return repository.findByNome(nome);
	}
}
