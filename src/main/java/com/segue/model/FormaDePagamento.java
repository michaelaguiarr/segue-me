package com.segue.model;

public enum FormaDePagamento {

	FIADO("Fiado"),
	DINHEIRO("Dinheiro");
	
	private String descricao;

	FormaDePagamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
	
	public String getCoresParaStyle() {
		switch (descricao) {
		case "Fiado":
			return "Yellow";
		case "Deposito":
			return "Blue";
		case "Dinheiro":
			return "Green";
		default:
			return "Silver";
		}
	}

}
