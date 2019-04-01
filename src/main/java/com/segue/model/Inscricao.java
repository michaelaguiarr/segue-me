package com.segue.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "inscricao")
public class Inscricao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	// Padre ou casal/jovem ficha
	@ManyToOne
	@JoinColumn(name = "fk_responsavel_inscricao", foreignKey = @ForeignKey(name = "fk_responsavel_inscricao"))
	private Evento responsavelInscricao;

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_inscricao", columnDefinition = "timestamp with time zone")
	private Date dataInsccricao = new Date();

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_gerar_doc", columnDefinition = "timestamp with time zone default now()")
	private Calendar dataGerarDoc = Calendar.getInstance();

	@Column(columnDefinition = "BOOLEAN DEFAULT true", name = "mora_com_pais")
	private Boolean moraComPais;

	@ManyToOne
	@JoinColumn(name = "fk_endereco_id", foreignKey = @ForeignKey(name = "fk_endereco_id"))
	private Endereco endereco;

	private String escolaridade;
	private String situacaoEscolaridade;
	private String curso;
	private String instituicao;
	private String religiao;
	private String igreja;

	@ManyToOne
	@JoinColumn(name = "fk_paroquia_id", foreignKey = @ForeignKey(name = "fk_paroquia_id"))
	private Paroquia paroquia;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "sagramento_batismo")
	private Boolean sagramentoBatismo;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "sagramento_eucaristia")
	private Boolean sagramentoEucaristia;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "sagramento_crisma")
	private Boolean sagramentoCrisma;

	private String movimentos;

	@Column(name = "nome_convidou")
	private String nomeConvidou;

	@Column(name = "telefone_convidou")
	private String telefoneConvidou;

	@Column(name = "endereco_convidou")
	private String enderecoConvidou;

	@Column(name = "informacao_extras")
	private String informacaoExtras;

	@Enumerated(EnumType.STRING)
	private StatusInscricao statusInscricao;

	@OneToMany(mappedBy = "inscricao", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<ConviteInscricao> conviteInscricaos = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "fk_segue_me", foreignKey = @ForeignKey(name = "fk_segue_me"))
	private SegueMe segueMe;

	private Integer numero;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "ficha_do_padre")
	private Boolean fichaDoPadre;

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

	public Evento getResponsavelInscricao() {
		return responsavelInscricao;
	}

	public void setResponsavelInscricao(Evento responsavelInscricao) {
		this.responsavelInscricao = responsavelInscricao;
	}

	public Date getDataInsccricao() {
		return dataInsccricao;
	}

	public void setDataInsccricao(Date dataInsccricao) {
		this.dataInsccricao = dataInsccricao;
	}

	public Boolean getMoraComPais() {
		return moraComPais;
	}

	public void setMoraComPais(Boolean moraComPais) {
		this.moraComPais = moraComPais;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public String getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(String escolaridade) {
		this.escolaridade = escolaridade;
	}

	public String getSituacaoEscolaridade() {
		return situacaoEscolaridade;
	}

	public void setSituacaoEscolaridade(String situacaoEscolaridade) {
		this.situacaoEscolaridade = situacaoEscolaridade;
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		this.curso = curso;
	}

	public String getInstituicao() {
		return instituicao;
	}

	public void setInstituicao(String instituicao) {
		this.instituicao = instituicao;
	}

	public String getReligiao() {
		return religiao;
	}

	public void setReligiao(String religiao) {
		this.religiao = religiao;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public Boolean getSagramentoBatismo() {
		return sagramentoBatismo;
	}

	public void setSagramentoBatismo(Boolean sagramentoBatismo) {
		this.sagramentoBatismo = sagramentoBatismo;
	}

	public Boolean getSagramentoEucaristia() {
		return sagramentoEucaristia;
	}

	public void setSagramentoEucaristia(Boolean sagramentoEucaristia) {
		this.sagramentoEucaristia = sagramentoEucaristia;
	}

	public Boolean getSagramentoCrisma() {
		return sagramentoCrisma;
	}

	public void setSagramentoCrisma(Boolean sagramentoCrisma) {
		this.sagramentoCrisma = sagramentoCrisma;
	}

	public String getMovimentos() {
		return movimentos;
	}

	public void setMovimentos(String movimentos) {
		this.movimentos = movimentos;
	}

	public String getNomeConvidou() {
		return nomeConvidou;
	}

	public void setNomeConvidou(String nomeConvidou) {
		this.nomeConvidou = nomeConvidou;
	}

	public String getTelefoneConvidou() {
		return telefoneConvidou;
	}

	public void setTelefoneConvidou(String telefoneConvidou) {
		this.telefoneConvidou = telefoneConvidou;
	}

	public String getEnderecoConvidou() {
		return enderecoConvidou;
	}

	public void setEnderecoConvidou(String enderecoConvidou) {
		this.enderecoConvidou = enderecoConvidou;
	}

	public String getInformacaoExtras() {
		return informacaoExtras;
	}

	public void setInformacaoExtras(String informacaoExtras) {
		this.informacaoExtras = informacaoExtras;
	}

	public StatusInscricao getStatusInscricao() {
		return statusInscricao;
	}

	public void setStatusInscricao(StatusInscricao statusInscricao) {
		this.statusInscricao = statusInscricao;
	}

	public List<ConviteInscricao> getConviteInscricaos() {
		return conviteInscricaos;
	}

	public void setConviteInscricaos(List<ConviteInscricao> conviteInscricaos) {
		this.conviteInscricaos = conviteInscricaos;
	}

	public Calendar getDataGerarDoc() {
		return dataGerarDoc;
	}

	public void setDataGerarDoc(Calendar dataGerarDoc) {
		this.dataGerarDoc = dataGerarDoc;
	}

	public String getIgreja() {
		return igreja;
	}

	public void setIgreja(String igreja) {
		this.igreja = igreja;
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public Boolean getFichaDoPadre() {
		return fichaDoPadre;
	}

	public void setFichaDoPadre(Boolean fichaDoPadre) {
		this.fichaDoPadre = fichaDoPadre;
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
		Inscricao other = (Inscricao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
