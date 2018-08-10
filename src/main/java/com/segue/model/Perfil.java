package com.segue.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.StringExtended;

@Entity
@Table(name = "perfil")
public class Perfil implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	@NotNull
	private String nome;

	private String descricao;

	@ManyToMany
	@JoinTable(name = "perfil_modulo", joinColumns = {
			@JoinColumn(name = "fk_usuario_perfil_cod", foreignKey = @ForeignKey(name = "fk_usuario_perfil_cod")) }, inverseJoinColumns = {
					@JoinColumn(name = "fk_aplicacao_modulo_cod", foreignKey = @ForeignKey(name = "fk_aplicacao_modulo_cod")) })
	private List<AplicacaoModulo> modulos = new ArrayList<>();

	public String getNomeComIniciaisMaiusculas() {
		return NomeComInicialMaiscula.iniciaisMaiuscula(nome);
	}

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

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = StringExtended.toASCII(nome.toUpperCase());

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

	public List<AplicacaoModulo> getModulos() {
		return modulos;
	}

	public void setModulos(List<AplicacaoModulo> modulos) {
		this.modulos = modulos;
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
		Perfil other = (Perfil) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
