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

import org.hibernate.validator.constraints.Length;

import com.segue.util.NomeComInicialMaiscula;

@Entity
@Table(name = "ecc")
public class ECC implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();
	private String titulo = "Encontro de Casais com Cristo - ECC ";
	private NumeroRomano numeroRomano;
	private Paroquia paroquia;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Length(max = 150)
	@Column(nullable = false, length = 150)
	public String getTitulo() {
		return this.titulo;
	}

	public void setTitulo(String titulo) {
		if (titulo != null) {
			this.titulo = NomeComInicialMaiscula.iniciaisMaiuscula(titulo);
		} else {
			this.titulo = titulo;
		}
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "paroquia_id", nullable = false)
	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "fk_numero_cod", foreignKey = @ForeignKey(name = "fk_numero_cod"))
	public NumeroRomano getNumeroRomano() {
		return numeroRomano;
	}

	public void setNumeroRomano(NumeroRomano numeroRomano) {
		this.numeroRomano = numeroRomano;
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
		ECC other = (ECC) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
