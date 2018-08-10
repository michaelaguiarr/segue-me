package com.segue.filter;

import java.io.Serializable;

import com.segue.model.Paroquia;
import com.segue.model.SegueMe;

public class PadreFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome;
	private String apelido;
	private SegueMe segueMe;
	private Paroquia paroquia;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
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

	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido;
	}

}
