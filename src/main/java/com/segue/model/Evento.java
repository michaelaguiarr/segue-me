package com.segue.model;

import java.io.Serializable;
import java.util.Calendar;

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
import javax.persistence.Transient;

@Entity
@Table(name = "evento_segue_me")
public class Evento implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", columnDefinition = "timestamp with time zone default now()")
	private Calendar timestamp = Calendar.getInstance();

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "fk_seguidor_id", foreignKey = @ForeignKey(name = "fk_seguidor_id"))
	private Seguidor seguidor;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "fk_casal_id", foreignKey = @ForeignKey(name = "fk_casal_id"))
	private Casal casal;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "fk_padre_id", foreignKey = @ForeignKey(name = "fk_padre_id"))
	private Padre padre;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "fk_palestrante_convidado_id", foreignKey = @ForeignKey(name = "fk_palestrante_convidado_id"))
	private PalestranteConvidado palestranteConvidado;

	@ManyToOne
	@JoinColumn(name = "fk_segue_me_id", foreignKey = @ForeignKey(name = "fk_segue_me_id"))
	private SegueMe segueMe;

	@ManyToOne
	@JoinColumn(name = "fk_equipe_id", foreignKey = @ForeignKey(name = "fk_equipe_id"))
	private Equipe equipe;

	@ManyToOne
	@JoinColumn(name = "fk_funcao_id", foreignKey = @ForeignKey(name = "fk_funcao_id"))
	private Funcao funcao;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "fk_inscricao_id", foreignKey = @ForeignKey(name = "fk_inscricao_id"))
	private Inscricao inscricao;

	@ManyToOne
	@JoinColumn(name = "fk_palestra_id", foreignKey = @ForeignKey(name = "fk_palestra_id"))
	private Palestra palestra;

	@ManyToOne
	@JoinColumn(name = "fk_usuario_id", foreignKey = @ForeignKey(name = "fk_usuario_id"))
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	private StatusConvite statusConvite;

	@Column(name = "motivo_recusa")
	private String motivoRecusa;

	@Column(name = "idade_no_evento")
	private Integer idade;

	@Column(columnDefinition = "BOOLEAN DEFAULT true", name = "cracha")
	private Boolean cracha;

	@ManyToOne
	@JoinColumn(name = "fk_circulo_id", foreignKey = @ForeignKey(name = "fk_circulo_id"))
	private Circulo circulo;

	/**
	 * Marcação temporária (não persistida) usada nas telas de listagem para
	 * selecionar em lote quais crachás (re)imprimir ou marcar como impressos.
	 */
	@Transient
	private boolean selecionado;

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

	public Seguidor getSeguidor() {
		return seguidor;
	}

	public void setSeguidor(Seguidor seguidor) {
		this.seguidor = seguidor;
	}

	public Casal getCasal() {
		return casal;
	}

	public void setCasal(Casal casal) {
		this.casal = casal;
	}

	public Padre getPadre() {
		return padre;
	}

	public void setPadre(Padre padre) {
		this.padre = padre;
	}

	public SegueMe getSegueMe() {
		return segueMe;
	}

	public void setSegueMe(SegueMe segueMe) {
		this.segueMe = segueMe;
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

	public Inscricao getInscricao() {
		return inscricao;
	}

	public void setInscricao(Inscricao inscricao) {
		this.inscricao = inscricao;
	}

	public StatusConvite getStatusConvite() {
		return statusConvite;
	}

	public void setStatusConvite(StatusConvite statusConvite) {
		this.statusConvite = statusConvite;
	}

	public String getMotivoRecusa() {
		return motivoRecusa;
	}

	public void setMotivoRecusa(String motivoRecusa) {
		this.motivoRecusa = motivoRecusa;
	}

	public Integer getIdade() {
		return idade;
	}

	public void setIdade(Integer idade) {
		this.idade = idade;
	}

	public PalestranteConvidado getPalestranteConvidado() {
		return palestranteConvidado;
	}

	public void setPalestranteConvidado(PalestranteConvidado palestranteConvidado) {
		this.palestranteConvidado = palestranteConvidado;
	}

	public Palestra getPalestra() {
		return palestra;
	}

	public void setPalestra(Palestra palestra) {
		this.palestra = palestra;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Boolean getCracha() {
		return cracha;
	}

	public void setCracha(Boolean cracha) {
		this.cracha = cracha;
	}

	public Circulo getCirculo() {
		return circulo;
	}

	public void setCirculo(Circulo circulo) {
		this.circulo = circulo;
	}

	public boolean isSelecionado() {
		return selecionado;
	}

	public void setSelecionado(boolean selecionado) {
		this.selecionado = selecionado;
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
		Evento other = (Evento) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
