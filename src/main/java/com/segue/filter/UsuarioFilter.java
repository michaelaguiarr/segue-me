package com.segue.filter;

import java.io.Serializable;

import com.segue.model.Equipe;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;

public class UsuarioFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome;
	private SegueMe segueMe;
	private Paroquia paroquia;
	private Equipe equipe;

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

	public Equipe getEquipe() {
		return equipe;
	}

	public void setEquipe(Equipe equipe) {
		this.equipe = equipe;
	}
}
