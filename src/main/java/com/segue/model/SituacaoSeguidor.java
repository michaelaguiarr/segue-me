package com.segue.model;

public enum SituacaoSeguidor {

	ATIVO("Ativo"), INATIVO("Inativo"), INSCRITO("Inscrito") ;

	private String nome;

	SituacaoSeguidor(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

}
