
package com.segue.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.segue.filter.EventoCasalFilter;
import com.segue.filter.EventoFilter;
import com.segue.filter.EventoPadreFilter;
import com.segue.filter.EventoSeguidorFilter;
import com.segue.filter.EventoSeguimistaFilter;
import com.segue.filter.PalestraCasalFilter;
import com.segue.filter.PalestraConvidadoFilter;
import com.segue.filter.PalestraPadreFilter;
import com.segue.filter.PalestraSeguidorFilter;
import com.segue.model.Casal;
import com.segue.model.Equipe;
import com.segue.model.Evento;
import com.segue.model.Padre;
import com.segue.model.Palestra;
import com.segue.model.PalestranteConvidado;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.model.StatusInscricao;
import com.segue.service.NegocioException;

public class EventoRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Evento porId(Long id) {
		return manager.find(Evento.class, id);
	}

	public Evento guardar(Evento evento) {
		return evento = manager.merge(evento);
	}

	public void remover(Evento evento) throws NegocioException {
		try {
			evento = porId(evento.getId());
			manager.remove(evento);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Não pode ser excluído.");
		}
	}

	public List<Evento> listaALL() {
		return manager.createQuery("SELECT e FROM Evento e " + "JOIN FETCH e.seguidor s " + "JOIN FETCH e.casal c "
				+ "JOIN FETCH e.padre p " + "JOIN FETCH e.palestranteConvidado pc " + "JOIN FETCH e.segueMe sm "
				+ "JOIN FETCH e.equipe e " + "JOIN FETCH e.funcao f " + "JOIN FETCH e.inscricao i "
				+ "JOIN FETCH e.palestra p ", Evento.class).getResultList();
	}

	public List<Evento> listaALLCrachaSeguidor() {
		return manager.createQuery("SELECT e FROM Evento e " + "JOIN FETCH e.seguidor s " + "JOIN FETCH e.segueMe sm "
				+ "JOIN FETCH e.equipe e " + "JOIN FETCH e.funcao f " + "WHERE e.cracha = true AND e.seguidor != null ",
				Evento.class).getResultList();
	}

	public List<Evento> filtradosInscricao(EventoFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.INNER);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (filtro.getId() != null) {
			predicates.add(builder.equal(inscricaoJoin.get("id"), filtro.getId()));
		}

		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(seguidorJoin.get("nome")), builder.asc(root.get("segueMe")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosSeguidor(EventoSeguidorFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.INNER);
		From<?, ?> enderecoJoin = (From<?, ?>) seguidorJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) seguidorJoin.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeSeguidorJoin = (From<?, ?>) seguidorJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) seguidorJoin.fetch("circulo", JoinType.LEFT);
		From<?, ?> sexoJoin = (From<?, ?>) seguidorJoin.fetch("sexo", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(seguidorJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}

		if (filtro.getEquipe() != null) {
			predicates.add(builder.equal(root.get("equipe"), filtro.getEquipe()));
		}

		if (filtro.getFuncao() != null) {
			predicates.add(builder.equal(root.get("funcao"), filtro.getFuncao()));
		}
		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNotNull(root.get("funcao")));
		predicates.add(builder.isNotNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(equipeJoin.get("ordem")),
				builder.asc(funcaoJoin.get("ordem")), builder.asc(seguidorJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosCasal(EventoCasalFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.INNER);
		From<?, ?> enderecoJoin = (From<?, ?>) casalJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> eccJoin = (From<?, ?>) casalJoin.fetch("ecc", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNomeEle())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("nomeEle")),
					"%" + filtro.getNomeEle().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelidoEle())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("apelidoEle")),
					"%" + filtro.getApelidoEle().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getNomeEla())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("nomeEla")),
					"%" + filtro.getNomeEla().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelidoEla())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("apelidoEla")),
					"%" + filtro.getApelidoEla().toLowerCase() + "%"));
		}

		if (filtro.getEquipe() != null) {
			predicates.add(builder.equal(root.get("equipe"), filtro.getEquipe()));
		}

		if (filtro.getFuncao() != null) {
			predicates.add(builder.equal(root.get("funcao"), filtro.getFuncao()));
		}
		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNotNull(root.get("funcao")));
		predicates.add(builder.isNotNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(equipeJoin.get("ordem")),
				builder.asc(funcaoJoin.get("ordem")), builder.asc(casalJoin.get("nomeEle")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosSeguimista(EventoSeguimistaFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> enderecoJoin = (From<?, ?>) seguidorJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) seguidorJoin.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeSeguidorJoin = (From<?, ?>) seguidorJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) seguidorJoin.fetch("circulo", JoinType.LEFT);
		From<?, ?> sexoJoin = (From<?, ?>) seguidorJoin.fetch("sexo", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.INNER);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(seguidorJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}

		if (filtro.getCirculo() != null) {
			predicates.add(builder.equal(seguidorJoin.get("circulo"), filtro.getCirculo()));
		}

		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.equal(inscricaoJoin.get("statusInscricao"), StatusInscricao.APROVADO));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(circuloJoin.get("corCirculo")),
				builder.asc(seguidorJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosPadre(EventoPadreFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> enderecoJoin = (From<?, ?>) seguidorJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) seguidorJoin.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeSeguidorJoin = (From<?, ?>) seguidorJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) seguidorJoin.fetch("circulo", JoinType.LEFT);
		From<?, ?> sexoJoin = (From<?, ?>) seguidorJoin.fetch("sexo", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.INNER);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(padreJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(padreJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}

		if (filtro.getEquipe() != null) {
			predicates.add(builder.equal(root.get("equipe"), filtro.getEquipe()));
		}

		if (filtro.getFuncao() != null) {
			predicates.add(builder.equal(root.get("funcao"), filtro.getFuncao()));
		}
		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNotNull(root.get("funcao")));
		predicates.add(builder.isNotNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(equipeJoin.get("ordem")),
				builder.asc(funcaoJoin.get("ordem")), builder.asc(padreJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosPalestraCasal(PalestraCasalFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.INNER);
		From<?, ?> enderecoJoin = (From<?, ?>) casalJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> eccJoin = (From<?, ?>) casalJoin.fetch("ecc", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.INNER);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNomeEle())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("nomeEle")),
					"%" + filtro.getNomeEle().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelidoEle())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("apelidoEle")),
					"%" + filtro.getApelidoEle().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getNomeEla())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("nomeEla")),
					"%" + filtro.getNomeEla().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelidoEla())) {
			predicates.add(builder.like(builder.lower(casalJoin.get("apelidoEla")),
					"%" + filtro.getApelidoEla().toLowerCase() + "%"));
		}

		if (filtro.getPalestra() != null) {
			predicates.add(builder.equal(root.get("palestra"), filtro.getPalestra()));
		}

		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNull(root.get("funcao")));
		predicates.add(builder.isNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(palestraJoin.get("ordem")),
				builder.asc(casalJoin.get("nomeEle")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosPalestraSeguidor(PalestraSeguidorFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.INNER);
		From<?, ?> enderecoJoin = (From<?, ?>) seguidorJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) seguidorJoin.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeSeguidorJoin = (From<?, ?>) seguidorJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) seguidorJoin.fetch("circulo", JoinType.LEFT);
		From<?, ?> sexoJoin = (From<?, ?>) seguidorJoin.fetch("sexo", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.INNER);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(seguidorJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(seguidorJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}

		if (filtro.getPalestra() != null) {
			predicates.add(builder.equal(root.get("palestra"), filtro.getPalestra()));
		}

		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNull(root.get("funcao")));
		predicates.add(builder.isNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(palestraJoin.get("ordem")),
				builder.asc(seguidorJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosPalestraPadre(PalestraPadreFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> enderecoJoin = (From<?, ?>) seguidorJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) seguidorJoin.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeSeguidorJoin = (From<?, ?>) seguidorJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) seguidorJoin.fetch("circulo", JoinType.LEFT);
		From<?, ?> sexoJoin = (From<?, ?>) seguidorJoin.fetch("sexo", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.INNER);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.INNER);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(padreJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(padreJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}

		if (filtro.getPalestra() != null) {
			predicates.add(builder.equal(root.get("palestra"), filtro.getPalestra()));
		}

		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNull(root.get("funcao")));
		predicates.add(builder.isNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(palestraJoin.get("ordem")),
				builder.asc(padreJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}
	
	

	public List<Evento> filtradosPalestraConvidado(PalestraConvidadoFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> enderecoJoin = (From<?, ?>) seguidorJoin.fetch("endereco", JoinType.LEFT);
		From<?, ?> paroquiaJoin = (From<?, ?>) seguidorJoin.fetch("paroquia", JoinType.LEFT);
		From<?, ?> segueMeSeguidorJoin = (From<?, ?>) seguidorJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> circuloJoin = (From<?, ?>) seguidorJoin.fetch("circulo", JoinType.LEFT);
		From<?, ?> sexoJoin = (From<?, ?>) seguidorJoin.fetch("sexo", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.INNER);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.INNER);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.INNER);

		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(
					builder.like(builder.lower(palestranteConvidadoJoin.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}

		if (StringUtils.isNotBlank(filtro.getApelido())) {
			predicates.add(builder.like(builder.lower(palestranteConvidadoJoin.get("apelido")),
					"%" + filtro.getApelido().toLowerCase() + "%"));
		}

		if (filtro.getPalestra() != null) {
			predicates.add(builder.equal(root.get("palestra"), filtro.getPalestra()));
		}

		if (filtro.getCracha() != "TODOS") {
			if (filtro.getCracha().equals("SIM")) {
				predicates.add(builder.equal(root.get("cracha"), true));
			} else {
				predicates.add(builder.equal(root.get("cracha"), false));
			}
		}

		predicates.add(builder.isNull(root.get("funcao")));
		predicates.add(builder.isNull(root.get("equipe")));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("segueMe")), builder.asc(palestraJoin.get("ordem")),
				builder.asc(palestranteConvidadoJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosHistorico(Seguidor seguidor) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.INNER);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);

		predicates.add(builder.equal(root.get("seguidor"), seguidor));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("numeroRomano")), builder.asc(segueMeJoin.get("dtInicio")),
				builder.asc(equipeJoin.get("ordem")), builder.asc(funcaoJoin.get("ordem")),
				builder.asc(seguidorJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosHistoricoCasal(Casal casal) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.INNER);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);

		predicates.add(builder.equal(root.get("casal"), casal));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("numeroRomano")), builder.asc(segueMeJoin.get("dtInicio")),
				builder.asc(equipeJoin.get("ordem")), builder.asc(funcaoJoin.get("ordem")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	public List<Evento> filtradosHistoricoPadre(Padre padre) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.INNER);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);

		predicates.add(builder.equal(root.get("padre"), padre));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("numeroRomano")), builder.asc(segueMeJoin.get("dtInicio")),
				builder.asc(equipeJoin.get("ordem")), builder.asc(funcaoJoin.get("ordem")),
				builder.asc(padreJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}
	
	
	public List<Evento> filtradosHistoricoConvidado(PalestranteConvidado palestranteConvidado) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Evento> criteriaQuery = builder.createQuery(Evento.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Evento> root = criteriaQuery.from(Evento.class);
		From<?, ?> seguidorJoin = (From<?, ?>) root.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) root.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) root.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) root.fetch("palestranteConvidado", JoinType.INNER);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) root.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) root.fetch("palestra", JoinType.LEFT);

		predicates.add(builder.equal(root.get("palestranteConvidado"), palestranteConvidado));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(segueMeJoin.get("numeroRomano")), builder.asc(segueMeJoin.get("dtInicio")),
				builder.asc(equipeJoin.get("ordem")), builder.asc(funcaoJoin.get("ordem")),
				builder.asc(seguidorJoin.get("nome")));

		TypedQuery<Evento> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	/**
	 * Procura seguidor por SegueMe, nome e Dt Nascimento
	 * 
	 * @param email
	 * @return
	 */
	public Evento findByNomeSegueMeDtNascimento(String nome, SegueMe segueMe, Date dtnascimento) {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s "
							+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "LEFT JOIN e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE e.segueMe = :segue "
							+ "AND s.nome like :nome " + " AND s.dataNascimento = :dtnascimento", Evento.class)
					.setParameter("dtnascimento", dtnascimento).setParameter("segue", segueMe)
					.setParameter("nome", nome).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByIdInscrito(Long id) throws NegocioException {
		try {
			return manager.createQuery(
					"SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s " + "LEFT JOIN e.casal c "
							+ "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "LEFT JOIN e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "JOIN FETCH e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE i.id = :id ",
					Evento.class).setParameter("id", id).getSingleResult();
		} catch (NoResultException nr) {
			throw new NegocioException("Ficha não pode ser encontrada.");
		}
	}

	public Evento findBySeguidorEventoEquipe(Seguidor seguidor, SegueMe segueMe, Equipe equipe)
			throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s "
							+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "JOIN FETCH e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE e.seguidor = :seguidor "
							+ "AND e.segueMe = :segueme " + "AND e.equipe = :equipe ", Evento.class)
					.setParameter("seguidor", seguidor).setParameter("segueme", segueMe).setParameter("equipe", equipe)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByCasalEventoEquipe(Casal casal, SegueMe segueMe, Equipe equipe) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "JOIN FETCH e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "JOIN FETCH e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE e.casal = :casal "
							+ "AND e.segueMe = :segueme " + "AND e.equipe = :equipe ", Evento.class)
					.setParameter("casal", casal).setParameter("segueme", segueMe).setParameter("equipe", equipe)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByPadreEventoEquipe(Padre padre, SegueMe segueMe, Equipe equipe) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "LEFT JOIN e.casal c " + "JOIN FETCH e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "JOIN FETCH e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p " + "WHERE e.padre = :padre "
							+ "AND e.segueMe = :segueme " + "AND e.equipe = :equipe ", Evento.class)
					.setParameter("padre", padre).setParameter("segueme", segueMe).setParameter("equipe", equipe)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByCasalEventoPalestra(Casal casal, SegueMe segueMe, Palestra palestra) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "JOIN FETCH e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "JOIN FETCH e.palestra p " + "WHERE e.casal = :casal "
							+ "AND e.segueMe = :segueme " + "AND e.palestra = :palestra ", Evento.class)
					.setParameter("casal", casal).setParameter("segueme", segueMe).setParameter("palestra", palestra)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findBySeguidorEventoPalestra(Seguidor seguidor, SegueMe segueMe, Palestra palestra)
			throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s "
							+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "JOIN FETCH e.palestra p " + "WHERE e.seguidor = :seguidor "
							+ "AND e.segueMe = :segueme " + "AND e.palestra = :palestra ", Evento.class)
					.setParameter("seguidor", seguidor).setParameter("segueme", segueMe)
					.setParameter("palestra", palestra).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Evento findByPadreEventoPalestra(Padre padre, SegueMe segueMe, Palestra palestra) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "LEFT JOIN e.casal c " + "JOIN FETCH e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "JOIN FETCH e.palestra p " + "WHERE e.padre = :padre "
							+ "AND e.segueMe = :segueme " + "AND e.palestra = :palestra ", Evento.class)
					.setParameter("padre", padre).setParameter("segueme", segueMe).setParameter("palestra", palestra)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}
	
	
	public Evento findByConvidadoEventoPalestra(PalestranteConvidado convidado, SegueMe segueMe, Palestra palestra)
			throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(e) FROM Evento e " + "LEFT JOIN e.seguidor s "
							+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "JOIN FETCH e.palestranteConvidado pc "
							+ "JOIN FETCH e.segueMe sm " + "LEFT JOIN e.equipe eq " + "LEFT JOIN e.funcao f "
							+ "LEFT JOIN e.inscricao i " + "JOIN FETCH e.palestra p " + "WHERE e.palestranteConvidado = :palestranteConvidado "
							+ "AND e.segueMe = :segueme " + "AND e.palestra = :palestra ", Evento.class)
					.setParameter("palestranteConvidado", convidado).setParameter("segueme", segueMe)
					.setParameter("palestra", palestra).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Evento> findBySeguidorNome(String nome, SegueMe segueMe) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " + "JOIN FETCH e.seguidor s "
					+ "LEFT JOIN e.casal c " + "LEFT JOIN e.padre p " + "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " + "JOIN FETCH e.equipe eq " + "LEFT JOIN e.funcao f "
					+ "LEFT JOIN e.inscricao i " + "LEFT JOIN e.palestra p "
					+ "where (s.apelido LIKE :apelido OR s.nome LIKE :nome OR eq.titulo LIKE :apelido) AND e.segueMe = :segueme ",
					Evento.class).setParameter("nome", "%" + nome + "%")
					.setParameter("apelido", "%" + nome.toUpperCase() + "%").setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Evento> findByCasalNome(String nome, SegueMe segueMe) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " 
					+ "LEFT JOIN e.seguidor s "
					+ "JOIN FETCH e.casal c " 
					+ "LEFT JOIN e.padre p " 
					+ "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " 
					+ "JOIN FETCH e.equipe eq " 
					+ "LEFT JOIN e.funcao f "
					+ "LEFT JOIN e.inscricao i " 
					+ "LEFT JOIN e.palestra p "
					+ "WHERE (lower(c.nomeEle) LIKE lower(:nomeEle) "
					+ "	OR lower(c.apelidoEle) LIKE lower(:apelidoEle) "
					+ " OR eq.titulo LIKE :apelido) AND e.segueMe = :segueme ",
					Evento.class)
					.setParameter("nomeEle", "%" + nome + "%")
					.setParameter("apelidoEle", "%" + nome + "%")
					.setParameter("apelido", "%" + nome.toUpperCase() + "%")
					.setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
	
	public List<Evento> findByPadreNome(String nome, SegueMe segueMe) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " 
					+ "LEFT JOIN e.seguidor s "
					+ "LEFT JOIN e.casal c " 
					+ "JOIN FETCH e.padre p " 
					+ "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " 
					+ "JOIN FETCH e.equipe eq " 
					+ "LEFT JOIN e.funcao f "
					+ "LEFT JOIN e.inscricao i " 
					+ "LEFT JOIN e.palestra pp "
					+ "WHERE (lower(p.nome) LIKE lower(:nome) "
					+ "	OR lower(p.apelido) LIKE lower(:nome) "
					+ " OR eq.titulo LIKE :apelido) AND e.segueMe = :segueme ",
					Evento.class)
					.setParameter("nome", "%" + nome + "%")
					.setParameter("apelido", "%" + nome.toUpperCase() + "%")
					.setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}
	
	public List<Evento> findBySeguimistaNome(String nome, SegueMe segueMe) {
		try {
			return manager.createQuery("SELECT distinct(e) FROM Evento e " 
					+ "JOIN FETCH e.seguidor s "
					+ "LEFT JOIN e.casal c " 
					+ "LEFT JOIN e.padre p " 
					+ "LEFT JOIN e.palestranteConvidado pc "
					+ "JOIN FETCH e.segueMe sm " 
					+ "LEFT JOIN e.equipe eq " 
					+ "LEFT JOIN e.funcao f "
					+ "JOIN FETCH e.inscricao i " 
					+ "LEFT JOIN e.palestra pp "
					+ "WHERE (lower(s.nome) LIKE lower(:nome) "
					+ "	OR lower(s.apelido) LIKE lower(:nome)) AND e.segueMe = :segueme ",
					Evento.class)
					.setParameter("nome", "%" + nome + "%")
					.setParameter("segueme", segueMe)
					.setMaxResults(30).getResultList();
		} catch (NoResultException nr) {
			return null;
		}
	}

}