package com.segue.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

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

@Entity
@Table(name = "segue_me")
public class SegueMe implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();
	private String titulo = "Encontro de Jovem com Cristo \"SEGUE-ME\"";
	private NumeroRomano numeroRomano;
	private Paroquia paroquia;
	private Date dtInicio;
	private Date dtFim;
	private String padroeiro;
	private String nomeclatura;
	@Column(columnDefinition = "BOOLEAN DEFAULT true")
	private boolean ativo = true;
	private byte[] imagem;
	private byte[] imagemFundo;
	private byte[] imagemRoda;
	@Column(name = "nome_cracha")
	private String nomeArquivoCracha;

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
		this.titulo = titulo;
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

	public String getNomeclatura() {
		return nomeclatura;
	}

	public void setNomeclatura(String nomeclatura) {
		this.nomeclatura = nomeclatura;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "data_inicio")
	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "data_fim")
	public Date getDtFim() {
		return dtFim;
	}

	public void setDtFim(Date dtFim) {
		this.dtFim = dtFim;
	}

	@Length(max = 100)
	@Column(length = 100)
	public String getPadroeiro() {
		return padroeiro;
	}

	public void setPadroeiro(String padroeiro) {
		this.padroeiro = padroeiro;
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

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public byte[] getImagem() {
		return imagem;
	}

	public void setImagem(byte[] imagem) {
		this.imagem = imagem;
	}

	@Column(name = "imagem_fundo")
	public byte[] getImagemFundo() {
		return imagemFundo;
	}

	public void setImagemFundo(byte[] imagemFundo) {
		this.imagemFundo = imagemFundo;
	}
	
	@Column(name = "imagem_roda")
	public byte[] getImagemRoda() {
		return imagemRoda;
	}

	public void setImagemRoda(byte[] imagemRoda) {
		this.imagemRoda = imagemRoda;
	}

	public String getNomeArquivoCracha() {
		return nomeArquivoCracha;
	}

	public void setNomeArquivoCracha(String nomeArquivoCracha) {
		this.nomeArquivoCracha = nomeArquivoCracha;
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
		SegueMe other = (SegueMe) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
