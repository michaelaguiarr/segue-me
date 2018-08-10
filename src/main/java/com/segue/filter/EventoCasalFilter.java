package com.segue.filter;

import java.io.Serializable;

import com.segue.model.Equipe;
import com.segue.model.Funcao;
import com.segue.model.Paroquia;
import com.segue.model.SegueMe;

public class EventoCasalFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private SegueMe segueMe;
	private Paroquia paroquia;
	private String nomeEle;
	private String apelidoEle;
	private String nomeEla;
	private String apelidoEla;
	private Equipe equipe;
	private Funcao funcao;
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

	public Equipe getEquipe() {
		return equipe;
	}

	public void setEquipe(Equipe equipe) {
		this.equipe = equipe;
	}

	public Funcao getFuncao() {
		return funcao;
	}

	public void setFuncao(Funcao funcao) {
		this.funcao = funcao;
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

	public String getCracha() {
		return cracha;
	}

	public void setCracha(String cracha) {
		this.cracha = cracha;
	}

}
