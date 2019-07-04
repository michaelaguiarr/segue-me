package com.segue.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import javax.faces.context.FacesContext;
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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.segue.util.Icons;
import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.StringExtended;
import com.segue.util.jsf.FacesUtil;

@Entity
@Table(name = "seguidor")
public class Seguidor implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	@NotBlank
	private String nome;
	
	@Column(name = "nome_sem_acento")
	private String nomeSemAcento;
	private String apelido;

	@Column(name = "telefone_um")
	private String telefoneUm;

	@Column(name = "telefone_dois")
	private String telefoneDois;

	@Column(name = "telefone_tres")
	private String telefoneTres;

	@Column(name = "telefone_quatro")
	private String telefoneQuatro;

	@Email
	private String email;

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_nascimento")
	private Date dataNascimento;

	private String naturalidade;

	private String nacionalidade;

	@Column(name = "filiacao_pai")
	private String filiacaoPai;

	@Column(name = "telefone_pai")
	private String telefonePai;

	@Column(name = "filiacao_mae")
	private String filiacaoMae;

	@Column(name = "telefone_mae")
	private String telefoneMae;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_endereco_id", foreignKey = @ForeignKey(name = "fk_endereco_id"))
	private Endereco endereco;

	@Column(name = "escolaridade")
	private String escolaridade;
	private String situacaoEscolaridade;
	@Column(name = "curso")
	private String curso;
	private String instituicao;
	private String religiao;

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

	@Enumerated(EnumType.STRING)
	private SituacaoSeguidor situacaoSeguidor;

	@Column(name = "rede_social_facebook")
	private String redeSocialFacebook;

	@Column(name = "rede_social_instagram")
	private String redeSocialInstagram;

	@Column(name = "rede_social_twitter")
	private String redeSocialTwitter;

	private byte[] imagem;

	@ManyToOne
	@JoinColumn(name = "fk_segue_id", foreignKey = @ForeignKey(name = "fk_segue_id"))
	private SegueMe segueMe;

	@ManyToOne
	@JoinColumn(name = "fk_circulo_id", foreignKey = @ForeignKey(name = "fk_circulo_id"))
	private Circulo circulo;

	@Column(columnDefinition = "BOOLEAN DEFAULT true", name = "ativo_circulo")
	private boolean ativoCirculo;

	@ManyToOne
	@JoinColumn(name = "fk_sexo_id", foreignKey = @ForeignKey(name = "fk_sexo_id"))
	private Sexo sexo;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "exibir_imagem")
	private boolean exibirImagem;

	@Enumerated(EnumType.STRING)
	@Column(name = "cor_circulo")
	private CorCirculo corCirculo;

	@Column(name = "tocar_instrumento")
	private String tocarInstrumento;

	@Column(name = "igreja_frequenta")
	private String igrejaFrequenta;

	/**
	 * Exibe icone para sexo
	 * 
	 * @return
	 */
	public String getIconeParaSexo() {
		return Icons.getIconBySexo(sexo);
	}

	public String carregarImagem() {
		String imagemLocal = "../resources/images/avatar.png";
		try {
			if (imagem != null) {
				System.out.println("passou");
				Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
						+ "resources/temp/seguidor");
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".jpg");
				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(imagem);
					fos.close();
				}
				return imagemLocal = "../resources/temp/seguidor/" + id + ".jpg";
			} else {
				return imagemLocal = "../resources/images/avatar.png";
			}
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
			return imagemLocal = "../resources/images/avatar.png";
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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
		if (nome != null) {
			this.nome = NomeComInicialMaiscula.iniciaisMaiuscula(nome);
			this.nomeSemAcento = StringExtended.toASCII(nome.toUpperCase());
		} else {
			this.nome = nome;
		}
	}

	public String getTelefoneUm() {
		return telefoneUm;
	}

	public void setTelefoneUm(String telefoneUm) {
		this.telefoneUm = telefoneUm;
	}

	public String getTelefoneDois() {
		return telefoneDois;
	}

	public void setTelefoneDois(String telefoneDois) {
		this.telefoneDois = telefoneDois;
	}

	public String getTelefoneTres() {
		return telefoneTres;
	}

	public void setTelefoneTres(String telefoneTres) {
		this.telefoneTres = telefoneTres;
	}

	public String getTelefoneQuatro() {
		return telefoneQuatro;
	}

	public void setTelefoneQuatro(String telefoneQuatro) {
		this.telefoneQuatro = telefoneQuatro;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getNaturalidade() {
		return naturalidade;
	}

	public void setNaturalidade(String naturalidade) {
		this.naturalidade = naturalidade;
	}

	public String getNacionalidade() {
		return nacionalidade;
	}

	public void setNacionalidade(String nacionalidade) {
		if (nacionalidade != null) {
			this.nacionalidade = NomeComInicialMaiscula.iniciaisMaiuscula(nacionalidade);
		} else {
			this.nacionalidade = nacionalidade;
		}
	}

	public String getFiliacaoPai() {
		return filiacaoPai;
	}

	public void setFiliacaoPai(String filiacaoPai) {
		if (filiacaoPai != null) {
			this.filiacaoPai = NomeComInicialMaiscula.iniciaisMaiuscula(filiacaoPai);
		} else {
			this.filiacaoPai = filiacaoPai;
		}
	}

	public String getTelefonePai() {
		return telefonePai;
	}

	public void setTelefonePai(String telefonePai) {
		this.telefonePai = telefonePai;
	}

	public String getFiliacaoMae() {
		return filiacaoMae;
	}

	public void setFiliacaoMae(String filiacaoMae) {
		if (filiacaoMae != null) {
			this.filiacaoMae = NomeComInicialMaiscula.iniciaisMaiuscula(filiacaoMae);
		} else {
			this.filiacaoMae = filiacaoMae;
		}
	}

	public String getTelefoneMae() {
		return telefoneMae;
	}

	public void setTelefoneMae(String telefoneMae) {
		this.telefoneMae = telefoneMae;
	}

	public String getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(String escolaridade) {
		if (escolaridade != null) {
			this.escolaridade = NomeComInicialMaiscula.iniciaisMaiuscula(escolaridade);
		} else {
			this.escolaridade = escolaridade;
		}

	}

	public String getSituacaoEscolaridade() {
		return situacaoEscolaridade;
	}

	public void setSituacaoEscolaridade(String situacaoEscolaridade) {
		if (situacaoEscolaridade != null) {
			this.situacaoEscolaridade = NomeComInicialMaiscula.iniciaisMaiuscula(situacaoEscolaridade);
		} else {
			this.situacaoEscolaridade = situacaoEscolaridade;
		}
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		if (curso != null) {
			this.curso = NomeComInicialMaiscula.iniciaisMaiuscula(curso);
		} else {
			this.curso = curso;
		}
	}

	public String getInstituicao() {
		return instituicao;
	}

	public void setInstituicao(String instituicao) {
		if (instituicao != null) {
			this.instituicao = NomeComInicialMaiscula.iniciaisMaiuscula(instituicao);
		} else {
			this.instituicao = instituicao;
		}
	}

	public String getReligiao() {
		return religiao;
	}

	public void setReligiao(String religiao) {
		if (religiao != null) {
			this.religiao = NomeComInicialMaiscula.iniciaisMaiuscula(religiao);
		} else {
			this.religiao = religiao;
		}
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
		if (movimentos != null) {
			this.movimentos = NomeComInicialMaiscula.iniciaisMaiuscula(movimentos);
		} else {
			this.movimentos = movimentos;
		}
	}

	public SituacaoSeguidor getSituacaoSeguidor() {
		return situacaoSeguidor;
	}

	public void setSituacaoSeguidor(SituacaoSeguidor situacaoSeguidor) {
		this.situacaoSeguidor = situacaoSeguidor;
	}

	public String getRedeSocialFacebook() {
		return redeSocialFacebook;
	}

	public void setRedeSocialFacebook(String redeSocialFacebook) {
		this.redeSocialFacebook = redeSocialFacebook.toLowerCase();
	}

	public String getRedeSocialInstagram() {
		return redeSocialInstagram;
	}

	public void setRedeSocialInstagram(String redeSocialInstagram) {
		this.redeSocialInstagram = redeSocialInstagram.toLowerCase();
	}

	public String getRedeSocialTwitter() {
		return redeSocialTwitter;
	}

	public void setRedeSocialTwitter(String redeSocialTwitter) {
		this.redeSocialTwitter = redeSocialTwitter.toLowerCase();
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido.toUpperCase();
	}

	public byte[] getImagem() {
		return imagem;
	}

	public void setImagem(byte[] imagem) {
		this.imagem = imagem;
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
	}

	public Circulo getCirculo() {
		return circulo;
	}

	public void setCirculo(Circulo circulo) {
		this.circulo = circulo;
	}

	public boolean isAtivoCirculo() {
		return ativoCirculo;
	}

	public void setAtivoCirculo(boolean ativoCirculo) {
		this.ativoCirculo = ativoCirculo;
	}

	public Sexo getSexo() {
		return sexo;
	}

	public void setSexo(Sexo sexo) {
		this.sexo = sexo;
	}

	public boolean isExibirImagem() {
		return exibirImagem;
	}

	public void setExibirImagem(boolean exibirImagem) {
		this.exibirImagem = exibirImagem;
	}

	public CorCirculo getCorCirculo() {
		return corCirculo;
	}

	public void setCorCirculo(CorCirculo corCirculo) {
		this.corCirculo = corCirculo;
	}

	public String getTocarInstrumento() {
		return tocarInstrumento;
	}

	public void setTocarInstrumento(String tocarInstrumento) {
		if (tocarInstrumento != null) {
			this.tocarInstrumento = NomeComInicialMaiscula.iniciaisMaiuscula(tocarInstrumento);
		} else {
			this.tocarInstrumento = tocarInstrumento;
		}
	}

	public String getIgrejaFrequenta() {
		return igrejaFrequenta;
	}

	public void setIgrejaFrequenta(String igrejaFrequenta) {
		if (igrejaFrequenta != null) {
			this.igrejaFrequenta = NomeComInicialMaiscula.iniciaisMaiuscula(igrejaFrequenta);
		} else {
			this.igrejaFrequenta = igrejaFrequenta;
		}
	}
	
	public String getNomeSemAcento() {
		return nomeSemAcento;
	}

	public void setNomeSemAcento(String nomeSemAcento) {
		this.nomeSemAcento = StringExtended.toASCII(nomeSemAcento.toUpperCase());
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
		Seguidor other = (Seguidor) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
