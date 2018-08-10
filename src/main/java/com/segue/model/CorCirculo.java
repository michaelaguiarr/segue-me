package com.segue.model;

public enum CorCirculo {

	AMARELO("Amarelo"),
	AZUL("Azul"), 
	LARANJA("Laranja"), 
	ROSA("Rosa"),
	VERDE("Verde"), 
	VERMELHO("Vermelho"),
	ROXO("Roxo"), 
	CINZA("Cinza bebê"),
	INATIVO("Inativo");
	
	private String descricao;

	CorCirculo(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
	
	public String getCoresParaStyle() {
		switch (descricao) {
		case "Amarelo":
			return "Yellow";
		case "Azul":
			return "Blue";
			
		case "Laranja":
			return "Orange";
			
		case "Rosa":
			return "Pink";
		case "VERDE":
			return "Green";
		case "Vermelho":
			return "Red";
		case "Roxo":
			return "Indigo";
		default:
			return "Silver";
		}
	}

}
