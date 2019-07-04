package com.segue.filter;

import java.io.Serializable;
import java.util.Date;

import com.segue.model.Paroquia;
import com.segue.model.SegueMe;

public class CasalFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nomeEle;
	private String apelidoEle;
	private String nomeEla;
	private String apelidoEla;
	private SegueMe segueMe;
	private Paroquia paroquia;

	private Date dtNascimento;

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

	public Date getDtNascimento() {
		return dtNascimento;
	}

	public void setDtNascimento(Date dtNascimento) {
		this.dtNascimento = dtNascimento;
	}

}
