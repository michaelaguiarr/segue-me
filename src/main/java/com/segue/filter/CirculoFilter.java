package com.segue.filter;

import java.io.Serializable;

import com.segue.model.CorCirculo;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;

public class CirculoFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome;
	private String status = "TODOS";
	private SegueMe segueMe;
	private CorCirculo[] cor;
	private Paroquia paroquia;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	public CorCirculo[] getCor() {
		return cor;
	}

	public void setCor(CorCirculo[] cor) {
		this.cor = cor;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

}
