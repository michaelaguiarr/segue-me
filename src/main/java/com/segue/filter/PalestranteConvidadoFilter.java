package com.segue.filter;

import java.io.Serializable;
import java.util.Date;

public class PalestranteConvidadoFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome;
	private Date dtNascimento;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Date getDtNascimento() {
		return dtNascimento;
	}

	public void setDtNascimento(Date dtNascimento) {
		this.dtNascimento = dtNascimento;
	}

}
