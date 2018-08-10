package com.segue.model.Enum;

public enum GrupoIdade {

	GRUPO1 ("16 a 19 Anos"),
	GRUPO2 ("20 a 23 Anos");
	
	private String descricao;

	GrupoIdade(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
