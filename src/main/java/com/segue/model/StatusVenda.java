package com.segue.model;

public enum StatusVenda {

	PAGO("Pago"),
	ABERTO("Aberto"), 
	CANCELADO("Cancelado");
	
	private String descricao;

	StatusVenda(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
	
	public String getCoresParaStyle() {
		switch (descricao) {
		case "Aberto":
			return "Yellow";
		case "Pago":
			return "Blue";
		case "Cancelado":
			return "Red";
		default:
			return "Silver";
		}
	}

}
