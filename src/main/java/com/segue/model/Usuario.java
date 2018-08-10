package com.segue.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.segue.util.Icons;
import com.segue.util.StringExtended;

@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	@NotBlank
	private String nome;

	@Email
	@Column(unique = true)
	@NotBlank
	private String email;

	@NotNull
	@Column(name = "telefone_ddd")
	private Integer telefoneDDD;

	@NotNull
	@Column(name = "telefone_numero")
	private Integer telefoneNumero;

	@ManyToOne
	@JoinColumn(name = "fk_sexo_cod", foreignKey = @ForeignKey(name = "fk_sexo_cod"))
	@NotNull
	private Sexo sexo;

	@Temporal(TemporalType.DATE)
	@Column(name = "data_nascimento")
	@NotNull
	private Date dataNascimento;

	@NotBlank
	@Column(length = 32)
	private String senha;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultimo_acesso", columnDefinition = "timestamp with time zone")
	private Calendar ultimoAcesso = Calendar.getInstance();

	// TODO: ultima alteracao nos dados do usuario
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_alteracao", columnDefinition = "timestamp with time zone")
	private Calendar ultimaAlteracao = Calendar.getInstance();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_senha", columnDefinition = "timestamp with time zone")
	private Calendar ultimaSenha = Calendar.getInstance();

	@NotNull
	@Enumerated(EnumType.STRING)
	private Status status;

	@ManyToOne
	@JoinColumn(name = "fk_usuario_perfil_cod", foreignKey = @ForeignKey(name = "fk_usuario_perfil_cod"))
	private Perfil perfil;

	@ManyToOne
	@JoinColumn(name = "fk_segue_cod", foreignKey = @ForeignKey(name = "fk_segue_cod"))
	private SegueMe segueMe;

	@ManyToOne
	@JoinColumn(name = "fk_equipe_cod", foreignKey = @ForeignKey(name = "fk_equipe_cod"))
	private Equipe equipe;

	@ManyToOne
	@JoinColumn(name = "fk_funcao_cod", foreignKey = @ForeignKey(name = "fk_funcao_cod"))
	private Funcao funcao;

	private byte[] imagem;

	/**
	 * Numero completo do usuario
	 * 
	 * @return
	 */
	public String getNumeroCompleto() {
		String numeroCompleto = "(" + this.telefoneDDD + ")" + this.telefoneNumero;
		return numeroCompleto;
	}

	public void setNumeroCompleto(String numeroCompleto) {
		String ddd = numeroCompleto.substring(1, 3);
		String numero = numeroCompleto.substring(4, 13);

		setTelefoneDDD(Integer.valueOf(ddd));
		setTelefoneNumero(Integer.valueOf(numero));
	}

	/**
	 * Exibe icone para sexo
	 * 
	 * @return
	 */
	public String getIconeParaSexo() {
		return Icons.getIconBySexo(sexo);
	}

	/**
	 * Formata numero de telefone com DDD e DDI
	 * 
	 * @return
	 */
	public String getTelefone() {
		return " (" + telefoneDDD + ") " + telefoneNumero;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = StringExtended.toASCII(nome.toUpperCase());
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public Calendar getUltimaAlteracao() {
		return ultimaAlteracao;
	}

	public void setUltimaAlteracao(Calendar ultimaAlteracao) {
		this.ultimaAlteracao = ultimaAlteracao;
	}

	public Calendar getUltimoAcesso() {
		return ultimoAcesso;
	}

	public void setUltimoAcesso(Calendar ultimoAcesso) {
		this.ultimoAcesso = ultimoAcesso;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getTelefoneDDD() {
		return telefoneDDD;
	}

	public void setTelefoneDDD(Integer telefoneDDD) {
		this.telefoneDDD = telefoneDDD;
	}

	public Integer getTelefoneNumero() {
		return telefoneNumero;
	}

	public void setTelefoneNumero(Integer telefoneNumero) {
		this.telefoneNumero = telefoneNumero;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public Sexo getSexo() {
		return sexo;
	}

	public void setSexo(Sexo sexo) {
		this.sexo = sexo;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Calendar getUltimaSenha() {
		return ultimaSenha;
	}

	public void setUltimaSenha(Calendar ultimaSenha) {
		this.ultimaSenha = ultimaSenha;
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

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	public byte[] getImagem() {
		return imagem;
	}

	public void setImagem(byte[] imagem) {
		this.imagem = imagem;
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
		Usuario other = (Usuario) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
