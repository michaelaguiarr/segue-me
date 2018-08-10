package com.segue.service;

import java.io.Serializable;

import javax.inject.Inject;

import com.segue.model.SegueMe;
import com.segue.repository.SegueMeRepository;
import com.segue.util.jpa.Transactional;

public class CadastroEjcService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SegueMeRepository ejcs;

	@Transactional
	public SegueMe salvar(SegueMe segueMe) throws NegocioException {
		return ejcs.guardar(segueMe);
	}

	@Transactional
	public void remover(SegueMe segueMe) throws NegocioException {
		ejcs.remover(segueMe);
	}

}
