package com.segue.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.segue.model.AplicacaoModulo;
import com.segue.repository.AplicacaoModuloRepository;

public class AplicacaoModuloService implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Inject
	private AplicacaoModuloRepository aplicacaoModuloRepository;
	
	/**
	 * Lista todos
	 * @return
	 */
	public List<AplicacaoModulo> findAll(){
		return aplicacaoModuloRepository.findAll();
	}
}
