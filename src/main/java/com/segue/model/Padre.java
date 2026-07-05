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
import javax.validation.constraints.NotBlank;

import com.segue.util.NomeComInicialMaiscula;
import com.segue.util.jsf.FacesUtil;

@Entity
@Table(name = "padre")
public class Padre implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	private byte[] imagem;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "exibir_imagem")
	private boolean exibirImagem;

	@NotBlank
	private String nome;
	private String apelido;

	@Column(name = "telefone_um")
	private String telefoneUm;

	@Email
	private String email;

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_nascimento", columnDefinition = "date")
	private Date dataNascimento;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ordenacao", columnDefinition = "timestamp with time zone")
	private Date dataOrdenacao;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_endereco_id", foreignKey = @ForeignKey(name = "fk_endereco_id"))
	private Endereco endereco;

	@ManyToOne
	@JoinColumn(name = "fk_paroquia_id", foreignKey = @ForeignKey(name = "fk_paroquia_id"))
	private Paroquia paroquia;

	@Column(columnDefinition = "BOOLEAN DEFAULT true", name = "ativo")
	private boolean ativo;

	/**
	 * carregar foto
	 * 
	 * @return
	 */
	public String carregarImagem() {
		String imagemLocal = "../resources/images/avatar.png";
		try {
			if (imagem != null) {
				Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
						+ "resources/temp/padre");
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".jpg");
				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(imagem);
					fos.close();
				}
				return imagemLocal = "../resources/temp/padre/" + id + ".jpg";
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
		} else {
			this.nome = nome;
		}
	}

	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido.toUpperCase();
	}

	public String getTelefoneUm() {
		return telefoneUm;
	}

	public void setTelefoneUm(String telefoneUm) {
		this.telefoneUm = telefoneUm;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public Date getDataOrdenacao() {
		return dataOrdenacao;
	}

	public void setDataOrdenacao(Date dataOrdenacao) {
		this.dataOrdenacao = dataOrdenacao;
	}

	public byte[] getImagem() {
		return imagem;
	}

	public void setImagem(byte[] imagem) {
		this.imagem = imagem;
	}

	public Paroquia getParoquia() {
		return paroquia;
	}

	public void setParoquia(Paroquia paroquia) {
		this.paroquia = paroquia;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public boolean isExibirImagem() {
		return exibirImagem;
	}

	public void setExibirImagem(boolean exibirImagem) {
		this.exibirImagem = exibirImagem;
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
		Padre other = (Padre) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
