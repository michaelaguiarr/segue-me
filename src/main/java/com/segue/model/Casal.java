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

import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.StringExtended;
import com.segue.util.jsf.FacesUtil;

@Entity
@Table(name = "casal")
public class Casal implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	@Column(name = "imagem_ele")
	private byte[] imagemELE;

	@Column(name = "nome_ele")
	private String nomeEle;
	
	@Column(name = "nome_ele_sem_acento")
	private String nomeEleSemAcento;

	@Column(name = "apelido_ele")
	private String apelidoEle;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_nascimento_ele", columnDefinition = "timestamp with time zone")
	private Date dataNascimentoEle;

	@Column(name = "naturalidade_ele")
	private String naturalidadeEle;

	@Column(name = "nacionalidade_ele")
	private String nacionalidadeEle;

	@Email
	@Column(name = "email_ele")
	private String emailEle;

	@Column(name = "telefone_ele_um")
	private String telefoneEleUm;

	@Column(name = "telefone_ele_dois")
	private String telefoneEleDois;

	@Column(name = "imagem_ela")
	private byte[] imagemELA;

	@Column(name = "nome_ela")
	private String nomeEla;
	
	@Column(name = "nome_ela_sem_acento")
	private String nomeElaSemAcento;

	@Column(name = "apelido_ela")
	private String apelidoEla;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_nascimento_ela", columnDefinition = "timestamp with time zone")
	private Date dataNascimentoEla;

	@Email
	@Column(name = "email_ela")
	private String emailEla;

	@Column(name = "naturalidade_ela")
	private String naturalidadeEla;

	@Column(name = "nacionalidade_ela")
	private String nacionalidadeEla;

	@Column(name = "telefone_ela_um")
	private String telefoneElaUm;

	@Column(name = "telefone_ela_dois")
	private String telefoneElaDois;

	@ManyToOne
	@JoinColumn(name = "fk_paroquia_id", foreignKey = @ForeignKey(name = "fk_paroquia_id"))
	private Paroquia paroquia;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_endereco_id", foreignKey = @ForeignKey(name = "fk_endereco_id"))
	private Endereco endereco;

	@Column(name = "telefone_residencial_um")
	private String telefoneresidencialUm;

	@Column(name = "telefone_residencial_dois")
	private String telefoneresidencialDois;

	@ManyToOne
	@JoinColumn(name = "fk_ecc_id", foreignKey = @ForeignKey(name = "fk_ecc_id"))
	private ECC ecc;

	private String movimentos;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_casament", columnDefinition = "timestamp with time zone")
	private Date dataCasamento;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "exibir_imagem_ele")
	private boolean exibirImagemEle;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "exibir_imagem_ela")
	private boolean exibirImagemEla;

	@Column(name = "igreja_frequenta")
	private String igrejaFrequenta;

	@Column(columnDefinition = "BOOLEAN DEFAULT true", name = "ativo")
	private boolean ativo = true;

	/**
	 * carregar foto
	 * 
	 * @return
	 */
	public String carregarImagemEle() {
		String imagemLocal = "../resources/images/avatar.png";
		try {
			if (imagemELE != null) {
				Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
						+ "resources/temp/casalEle");
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".jpg");
//				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(imagemELE);
					fos.close();
//				}
				return imagemLocal = "../resources/temp/casalEle/" + id + ".jpg";
			} else {
				return imagemLocal = "../resources/images/avatar.png";
			}
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
			return imagemLocal = "../resources/images/avatar.png";
		}
	}

	public String carregarImagemEla() {
		String imagemLocal = "../resources/images/avatar.png";
		try {
			if (imagemELA != null) {
				Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
						+ "resources/temp/casalEla");
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".jpg");
				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(imagemELA);
					fos.close();
				}
				return imagemLocal = "../resources/temp/casalEla/" + id + ".jpg";
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

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public byte[] getImagemELE() {
		return imagemELE;
	}

	public void setImagemELE(byte[] imagemELE) {
		this.imagemELE = imagemELE;
	}

	public String getNomeEle() {
		return nomeEle;
	}

	public void setNomeEle(String nomeEle) {
		if (nomeEle != null) {
			this.nomeEle = NomeComInicialMaiscula.iniciaisMaiuscula(nomeEle);
			setNomeEleSemAcento(nomeEle);
		} else {
			this.nomeEle = nomeEle;
		}
	}

	public String getApelidoEle() {
		return apelidoEle;
	}

	public void setApelidoEle(String apelidoEle) {
		this.apelidoEle = apelidoEle.toUpperCase();
	}

	public Date getDataNascimentoEle() {
		return dataNascimentoEle;
	}

	public void setDataNascimentoEle(Date dataNascimentoEle) {
		this.dataNascimentoEle = dataNascimentoEle;
	}

	public String getNaturalidadeEle() {
		return naturalidadeEle;
	}

	public void setNaturalidadeEle(String naturalidadeEle) {
		this.naturalidadeEle = naturalidadeEle;
	}

	public String getEmailEle() {
		return emailEle;
	}

	public void setEmailEle(String emailEle) {
		this.emailEle = emailEle.toLowerCase();
	}

	public String getTelefoneEleUm() {
		return telefoneEleUm;
	}

	public void setTelefoneEleUm(String telefoneEleUm) {
		this.telefoneEleUm = telefoneEleUm;
	}

	public String getTelefoneEleDois() {
		return telefoneEleDois;
	}

	public void setTelefoneEleDois(String telefoneEleDois) {
		this.telefoneEleDois = telefoneEleDois;
	}

	public byte[] getImagemELA() {
		return imagemELA;
	}

	public void setImagemELA(byte[] imagemELA) {
		this.imagemELA = imagemELA;
	}

	public String getNomeEla() {
		return nomeEla;
	}

	public void setNomeEla(String nomeEla) {
		if (nomeEla != null) {
			this.nomeEla = NomeComInicialMaiscula.iniciaisMaiuscula(nomeEla);
			setNomeElaSemAcento(this.nomeEla);
		} else {
			this.nomeEla = nomeEla;
		}
	}

	public String getNomeEleSemAcento() {
		return nomeEleSemAcento;
	}

	
	public String getNomeElaSemAcento() {
		return nomeElaSemAcento;
	}

	public void setNomeElaSemAcento(String nomeElaSemAcento) {
		this.nomeElaSemAcento = StringExtended.toASCII(nomeElaSemAcento.toUpperCase());
	}

	public void setNomeEleSemAcento(String nomeEleSemAcento) {
		this.nomeEleSemAcento = StringExtended.toASCII(nomeEleSemAcento.toUpperCase());
	}

	public String getApelidoEla() {
		return apelidoEla;
	}

	public void setApelidoEla(String apelidoEla) {
		this.apelidoEla = apelidoEla.toUpperCase();
	}

	public Date getDataNascimentoEla() {
		return dataNascimentoEla;
	}

	public void setDataNascimentoEla(Date dataNascimentoEla) {
		this.dataNascimentoEla = dataNascimentoEla;
	}

	public String getEmailEla() {
		return emailEla;
	}

	public void setEmailEla(String emailEla) {
		this.emailEla = emailEla.toLowerCase();
	}

	public String getNaturalidadeEla() {
		return naturalidadeEla;
	}

	public void setNaturalidadeEla(String naturalidadeEla) {
		this.naturalidadeEla = naturalidadeEla;
	}

	public String getTelefoneElaUm() {
		return telefoneElaUm;
	}

	public void setTelefoneElaUm(String telefoneElaUm) {
		this.telefoneElaUm = telefoneElaUm;
	}

	public String getTelefoneElaDois() {
		return telefoneElaDois;
	}

	public void setTelefoneElaDois(String telefoneElaDois) {
		this.telefoneElaDois = telefoneElaDois;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public String getTelefoneresidencialUm() {
		return telefoneresidencialUm;
	}

	public void setTelefoneresidencialUm(String telefoneresidencialUm) {
		this.telefoneresidencialUm = telefoneresidencialUm;
	}

	public String getTelefoneresidencialDois() {
		return telefoneresidencialDois;
	}

	public void setTelefoneresidencialDois(String telefoneresidencialDois) {
		this.telefoneresidencialDois = telefoneresidencialDois;
	}

	public ECC getEcc() {
		return ecc;
	}

	public void setEcc(ECC ecc) {
		this.ecc = ecc;
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

	public Date getDataCasamento() {
		return dataCasamento;
	}

	public void setDataCasamento(Date dataCasamento) {
		this.dataCasamento = dataCasamento;
	}

	public boolean isExibirImagemEle() {
		return exibirImagemEle;
	}

	public void setExibirImagemEle(boolean exibirImagemEle) {
		this.exibirImagemEle = exibirImagemEle;
	}

	public boolean isExibirImagemEla() {
		return exibirImagemEla;
	}

	public void setExibirImagemEla(boolean exibirImagemEla) {
		this.exibirImagemEla = exibirImagemEla;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public String getNacionalidadeEle() {
		return nacionalidadeEle;
	}

	public void setNacionalidadeEle(String nacionalidadeEle) {
		if (nacionalidadeEle != null) {
			this.nacionalidadeEle = NomeComInicialMaiscula.iniciaisMaiuscula(nacionalidadeEle);
		} else {
			this.nacionalidadeEle = nacionalidadeEle;
		}
	}

	public String getNacionalidadeEla() {
		return nacionalidadeEla;
	}

	public void setNacionalidadeEla(String nacionalidadeEla) {
		if (nacionalidadeEla != null) {
			this.nacionalidadeEla = NomeComInicialMaiscula.iniciaisMaiuscula(nacionalidadeEla);
		} else {
			this.nacionalidadeEla = nacionalidadeEla;
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
		Casal other = (Casal) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
