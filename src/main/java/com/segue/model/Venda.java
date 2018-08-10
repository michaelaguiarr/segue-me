package com.segue.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "venda_segue_me")
public class Venda implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	@Temporal(TemporalType.DATE)
	@Column(name = "data_venda")
	private Date dataVenda;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "fk_evento_id", foreignKey = @ForeignKey(name = "fk_evento_id"))
	private Evento evento;

	@ManyToOne
	@JoinColumn(name = "fk_usuario_id", foreignKey = @ForeignKey(name = "fk_usuario_id"))
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_venda")
	private StatusVenda statusVenda;

	@Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorTotal = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(name = "para_quem_venda")
	private ParaQuemVenda paraQuemVenda;

	@ManyToOne
	@JoinColumn(name = "fk_segue_me_id", foreignKey = @ForeignKey(name = "fk_segue_me_id"))
	private SegueMe segueMe;

	private String obs;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public StatusVenda getStatusVenda() {
		return statusVenda;
	}

	public void setStatusVenda(StatusVenda statusVenda) {
		this.statusVenda = statusVenda;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getObs() {
		return obs;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}

	public ParaQuemVenda getParaQuemVenda() {
		return paraQuemVenda;
	}

	public void setParaQuemVenda(ParaQuemVenda paraQuemVenda) {
		this.paraQuemVenda = paraQuemVenda;
	}

	public Date getDataVenda() {
		return dataVenda;
	}

	public void setDataVenda(Date dataVenda) {
		this.dataVenda = dataVenda;
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Venda other = (Venda) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	};

	@Transient
	public boolean isAberto() {
		return StatusVenda.ABERTO.equals(this.statusVenda);
	}

	@Transient
	public boolean isNaoAberto() {
		return !this.isAberto();
	}

	@Transient
	public boolean isPago() {
		return StatusVenda.PAGO.equals(this.statusVenda);
	}

	@Transient
	public boolean isNaoPago() {
		return !this.isPago();
	}

	@Transient
	public boolean isCancelado() {
		return StatusVenda.CANCELADO.equals(this.statusVenda);
	}

	@Transient
	public boolean isNaoCancelado() {
		return !this.isCancelado();
	}

	@Transient
	public boolean isNaoAlteravel() {
		return !this.isAlteravel();
	}

	@Transient
	public boolean isAlteravel() {
		return this.isAberto();
	}

}
