package com.segue.filter;

import java.io.Serializable;
import java.util.Date;

import com.segue.model.Circulo;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;

public class SeguidorFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome;
	private Date dtNascimento;
	private SegueMe segueMe;
	private Paroquia paroquia;
	private Circulo circulo;
	private String situacao = "ATIVO";

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

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public Circulo getCirculo() {
		return circulo;
	}

	public void setCirculo(Circulo circulo) {
		this.circulo = circulo;
	}

	public String getSituacao() {
		return situacao;
	}

	public void setSituacao(String situacao) {
		this.situacao = situacao;
	}
}
