package com.segue.model;

public enum ParaQuemVenda {

	CASAL("Casal"), SEGUIMISTA("Seguimista"), SEGUIDOR("Seguidor"), PADRE("Padre/Bispo"), DESCONHECIDO("Desconhecido");

	private String descricao;

	ParaQuemVenda(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
