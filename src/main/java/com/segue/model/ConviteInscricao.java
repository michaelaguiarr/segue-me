package com.segue.model;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "convite_inscricao")
public class ConviteInscricao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	@ManyToOne
	@JoinColumn(name = "fk_inscricao_id", foreignKey = @ForeignKey(name = "fk_inscricao_id"))
	private Inscricao inscricao;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_convite", columnDefinition = "timestamp with time zone")
	private Calendar dataConvite;

	private String casal;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "aceitou")
	private boolean aceitou;

	@Column(name = "motivo_recusa")
	private String motivoRecusa;

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

	public Calendar getDataConvite() {
		return dataConvite;
	}

	public void setDataConvite(Calendar dataConvite) {
		this.dataConvite = dataConvite;
	}

	public String getCasal() {
		return casal;
	}

	public void setCasal(String casal) {
		this.casal = casal;
	}

	public boolean isAceitou() {
		return aceitou;
	}

	public void setAceitou(boolean aceitou) {
		this.aceitou = aceitou;
	}

	public String getMotivoRecusa() {
		return motivoRecusa;
	}

	public void setMotivoRecusa(String motivoRecusa) {
		this.motivoRecusa = motivoRecusa;
	}

	public Inscricao getInscricao() {
		return inscricao;
	}

	public void setInscricao(Inscricao inscricao) {
		this.inscricao = inscricao;
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
		ConviteInscricao other = (ConviteInscricao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
