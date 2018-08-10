package com.segue.filter;

import java.io.Serializable;
import java.util.Date;

import com.segue.model.Evento;
import com.segue.model.ParaQuemVenda;
import com.segue.model.SegueMe;
import com.segue.model.StatusVenda;

public class VendaFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private Date dtInicio;
	private Date dtFim;
	private Evento evento;
	private StatusVenda status;
	private ParaQuemVenda paraQuemVenda;
	private SegueMe segueMe;

	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	public Date getDtFim() {
		return dtFim;
	}

	public void setDtFim(Date dtFim) {
		this.dtFim = dtFim;
	}

	public StatusVenda getStatus() {
		return status;
	}

	public void setStatus(StatusVenda status) {
		this.status = status;
	}

	public ParaQuemVenda getParaQuemVenda() {
		return paraQuemVenda;
	}

	public void setParaQuemVenda(ParaQuemVenda paraQuemVenda) {
		this.paraQuemVenda = paraQuemVenda;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

}
