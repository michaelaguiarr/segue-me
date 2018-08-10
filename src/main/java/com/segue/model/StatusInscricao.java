package com.segue.model;

public enum StatusInscricao {

	// TODO: Status do usuario
	INSCRITO("Inscrito"), PENDENTE("Pendente"), RECUSADO("Recusado"), APROVADO("Aprovado"), RESERVA("Reserva");

	private String nome;

	StatusInscricao(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}
	
	public String getCoresParaStyle() {
		switch (nome) {
		case "Pendente":
			return "Yellow";
		case "Inscrito":
			return "Blue";
		case "Reserva":
			return "Orange";
		case "Aprovado":
			return "Green";
		case "Recusado":
			return "Red";
		default:
			return "Silver";
		}
	}

}
