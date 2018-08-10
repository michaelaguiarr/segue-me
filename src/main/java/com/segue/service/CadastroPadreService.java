package com.segue.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.segue.model.Padre;
import com.segue.repository.PadreRepository;
import com.segue.util.jpa.Transactional;

public class CadastroPadreService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PadreRepository repository;

	@Transactional
	public Padre salvar(Padre padre) throws NegocioException {
		Padre padreExiste = repository.findByNomeDtNascimento(padre.getNome(), padre.getDataNascimento());
		if (padreExiste != null && !padreExiste.equals(padre)) {
			throw new NegocioException("Padre já cadastrado no sistema!Por favor faça uma pesquisa");
		}
		return repository.guardar(padre);
	}

	@Transactional
	public void remover(Padre padre) throws NegocioException {
		repository.remover(padre);
	}

	
	/**
	 * Busca seguidor por nome
	 * 
	 * @return
	 */
	public List<Padre> findByNome(String nome) {
		return repository.findByNome(nome);
	}
}
