package com.segue.filter;

import java.io.Serializable;

import com.segue.model.Palestra;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;

public class PalestraCasalFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private SegueMe segueMe;
	private Paroquia paroquia;
	private String nomeEle;
	private String apelidoEle;
	private String nomeEla;
	private String apelidoEla;
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

	public String getNomeEle() {
		return nomeEle;
	}

	public void setNomeEle(String nomeEle) {
		this.nomeEle = nomeEle;
	}

	public String getNomeEla() {
		return nomeEla;
	}

	public void setNomeEla(String nomeEla) {
		this.nomeEla = nomeEla;
	}

	public String getApelidoEle() {
		return apelidoEle;
	}

	public void setApelidoEle(String apelidoEle) {
		this.apelidoEle = apelidoEle;
	}

	public String getApelidoEla() {
		return apelidoEla;
	}

	public void setApelidoEla(String apelidoEla) {
		this.apelidoEla = apelidoEla;
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
