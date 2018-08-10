package com.segue.service;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.inject.Inject;

import com.segue.model.Venda;
import com.segue.repository.VendaRepository;
import com.segue.util.jpa.Transactional;

public class CadastroVendaService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private VendaRepository repository;

	@Transactional
	public Venda salvar(Venda venda) throws NegocioException {
		BigDecimal zero = BigDecimal.ZERO;
		if (venda.getValorTotal() == zero) {
			throw new NegocioException("Informe um valor maior que ZERO!");
		}
		return repository.guardar(venda);
	}

	@Transactional
	public void remover(Venda venda) throws NegocioException {
		repository.remover(venda);
	}

}
