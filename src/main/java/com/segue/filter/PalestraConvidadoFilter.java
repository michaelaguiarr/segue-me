package com.segue.filter;

import java.io.Serializable;

import com.segue.model.Palestra;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;

public class PalestraConvidadoFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private SegueMe segueMe;
	private Paroquia paroquia;
	private String nome;
	private String apelido;
	private Palestra palestra;
	private String cracha = "TODOS";

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

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido;
	}

	public Palestra getPalestra() {
		return palestra;
	}

	public void setPalestra(Palestra palestra) {
		this.palestra = palestra;
	}

	public String getCracha() {
		return cracha;
	}

	public void setCracha(String cracha) {
		this.cracha = cracha;
	}

}
