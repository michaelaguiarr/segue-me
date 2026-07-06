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

import com.segue.util.Icons;
import com.segue.util.jsf.FacesUtil;

@Entity
@Table(name = "palestrante_convidado")
public class PalestranteConvidado implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

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

	private String naturalidade;

	private String nacionalidade;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_endereco_id", foreignKey = @ForeignKey(name = "fk_endereco_id"))
	private Endereco endereco;
	
	@ManyToOne
	@JoinColumn(name = "fk_sexo_id", foreignKey = @ForeignKey(name = "fk_sexo_id"))
	private Sexo sexo;

	private byte[] imagem;

	@Column(columnDefinition = "BOOLEAN DEFAULT false", name = "exibir_imagem")
	private boolean exibirImagem;

	@Column(columnDefinition = "BOOLEAN DEFAULT true", name = "ativo")
	private boolean ativo = true;

	/**
	 * Construtor padrão exigido pela JPA.
	 */
	public PalestranteConvidado() {
	}

	/**
	 * Construtor de projeção da listagem de pesquisa: só os campos exibidos na
	 * tabela, sem o blob {@code imagem} (foto). A foto é carregada sob demanda ao
	 * abrir o diálogo ({@code PesquisaPalestranteConvidadoBean.carregarFoto}).
	 */
	public PalestranteConvidado(Integer id, Calendar timestamp, String nome, String apelido, String telefoneUm,
			Sexo sexo) {
		this.id = id;
		this.timestamp = timestamp;
		this.nome = nome;
		this.apelido = apelido;
		this.telefoneUm = telefoneUm;
		this.sexo = sexo;
	}

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
						+ "resources/temp/palestranteConvidado");
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".jpg");
				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(imagem);
					fos.close();
				}
				return imagemLocal = "../resources/temp/palestranteConvidado/" + id + ".jpg";
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
		this.nome = nome;
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
		this.nacionalidade = nacionalidade;
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
		this.apelido = apelido;
	}

	public Sexo getSexo() {
		return sexo;
	}

	public void setSexo(Sexo sexo) {
		this.sexo = sexo;
	}

	public byte[] getImagem() {
		return imagem;
	}

	public void setImagem(byte[] imagem) {
		this.imagem = imagem;
	}

	public boolean isExibirImagem() {
		return exibirImagem;
	}

	public void setExibirImagem(boolean exibirImagem) {
		this.exibirImagem = exibirImagem;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
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
		PalestranteConvidado other = (PalestranteConvidado) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
