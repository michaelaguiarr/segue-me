package com.segue.model;

public enum StatusConvite {

	CONVIDADO("Convidado"), PARTICIPANDO("Participando"), DESISTIU("Desistiu"), RECUSOU("Recusou");

	private String nome;

	StatusConvite(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}
	
	public String getCoresParaStyle() {
		switch (nome) {
		case "Convidado":
			return "Yellow";
		case "Participando":
			return "Blue";
		case "Desistiu":
			return "Orange";
		case "Recusou":
			return "Red";
		default:
			return "Silver";
		}
	}

}
