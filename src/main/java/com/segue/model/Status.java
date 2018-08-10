package com.segue.model;

public enum Status {

	// TODO: Status do usuario
	AUTORIZADO("Autorizado"), PENDENTE("Pendente"), DESATIVADO("Desativado");

	private String nome;

	Status(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	public String getCoresParaStyle() {
		switch (nome) {
		case "Pendente":
			return "Yellow";
		case "Autorizado":
			return "Blue";
		case "Desativado":
			return "Red";
		default:
			return "Silver";
		}
	}

}
