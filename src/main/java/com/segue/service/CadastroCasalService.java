package com.segue.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.segue.model.Casal;
import com.segue.repository.CasalRepository;
import com.segue.util.jpa.Transactional;

public class CadastroCasalService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CasalRepository repository;

	@Transactional
	public Casal salvar(Casal casal) throws NegocioException {
		Casal casalEleExiste = repository.findByNomeDtNascEle(casal.getNomeEle(), casal.getDataNascimentoEle());
		if (casalEleExiste != null && !casalEleExiste.equals(casal)) {
			throw new NegocioException("Casal já cadastrado no sistema!(ele)");
		}
		Casal casalElaExiste = repository.findByNomeDtNascEla(casal.getNomeEla(), casal.getDataNascimentoEla());
		if (casalElaExiste != null && !casalElaExiste.equals(casal)) {
			throw new NegocioException("Casal já cadastrado no sistema!(ela)");
		}
//		if (casal.getImagemELE() == null) {
//			throw new NegocioException("Informe a foto do Casal.");
//		}
		casal.setNomeElaSemAcento(casal.getNomeEla());
		casal.setNomeEleSemAcento(casal.getNomeEle());
		return repository.guardar(casal);
	}
	
	@Transactional
	public Casal atualizarNome(Casal casal) throws NegocioException {
		casal.setNomeElaSemAcento(casal.getNomeEla());
		casal.setNomeEleSemAcento(casal.getNomeEle());
		return repository.guardar(casal);
	}

	@Transactional
	public void remover(Casal casal) throws NegocioException {
		repository.remover(casal);
	}

	
	/**
	 * Busca seguidor por nome
	 * 
	 * @return
	 */
	public List<Casal> findByNome(String nome) {
		return repository.findByNome(nome);
	}
}
