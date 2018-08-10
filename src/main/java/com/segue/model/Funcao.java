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
import javax.validation.constraints.NotNull;

import com.segue.util.NomeComInicialMaiscula;

@Entity
@Table(name = "funcao")
public class Funcao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	private String descricao;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "fk_equipe_id", foreignKey = @ForeignKey(name = "fk_equipe_id"))
	private Equipe equipe;

	@NotNull
	private Integer ordem;

	@Column(columnDefinition = "BOOLEAN DEFAULT false")
	private boolean administrador;

	@Column(columnDefinition = "BOOLEAN DEFAULT false")
	private boolean administradorGeral;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		if (descricao != null) {
			this.descricao = NomeComInicialMaiscula.iniciaisMaiuscula(descricao);
		} else {
			this.descricao = descricao;
		}
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public Equipe getEquipe() {
		return equipe;
	}

	public void setEquipe(Equipe equipe) {
		this.equipe = equipe;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public boolean isAdministrador() {
		return administrador;
	}

	public void setAdministrador(boolean administrador) {
		this.administrador = administrador;
	}

	public boolean isAdministradorGeral() {
		return administradorGeral;
	}

	public void setAdministradorGeral(boolean administradorGeral) {
		this.administradorGeral = administradorGeral;
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
		Funcao other = (Funcao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
