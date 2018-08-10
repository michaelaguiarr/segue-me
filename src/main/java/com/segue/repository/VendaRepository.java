package com.segue.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.segue.filter.VendaFilter;
import com.segue.model.Venda;
import com.segue.service.NegocioException;

public class VendaRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Venda porId(Long id) {
		return manager.find(Venda.class, id);
	}

	public Venda guardar(Venda venda) {
		return venda = manager.merge(venda);
	}

	public void remover(Venda venda) throws NegocioException {
		try {
			venda = porId(venda.getId());
			manager.remove(venda);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Venda não pode ser excluído.");
		}
	}

	public List<Venda> listaALL() {
		return manager.createQuery("SELECT b FROM Venda b ORDER BY b.timestamp ", Venda.class).getResultList();
	}
	
	public List<Venda> filtrados(VendaFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Venda> criteriaQuery = builder.createQuery(Venda.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Venda> root = criteriaQuery.from(Venda.class);
		From<?, ?> eventoJoin = (From<?, ?>) root.fetch("evento", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> seguidorJoin = (From<?, ?>) eventoJoin.fetch("seguidor", JoinType.LEFT);
		From<?, ?> casalJoin = (From<?, ?>) eventoJoin.fetch("casal", JoinType.LEFT);
		From<?, ?> padreJoin = (From<?, ?>) eventoJoin.fetch("padre", JoinType.LEFT);
		From<?, ?> palestranteConvidadoJoin = (From<?, ?>) eventoJoin.fetch("palestranteConvidado", JoinType.LEFT);
		From<?, ?> funcaoJoin = (From<?, ?>) eventoJoin.fetch("funcao", JoinType.LEFT);
		From<?, ?> eventoSegueMeJoin = (From<?, ?>) eventoJoin.fetch("segueMe", JoinType.LEFT);
		From<?, ?> equipeJoin = (From<?, ?>) eventoJoin.fetch("equipe", JoinType.LEFT);
		From<?, ?> inscricaoJoin = (From<?, ?>) eventoJoin.fetch("inscricao", JoinType.LEFT);
		From<?, ?> palestraJoin = (From<?, ?>) eventoJoin.fetch("palestra", JoinType.LEFT);
		From<?, ?> usuarioJoin = (From<?, ?>) root.fetch("usuario", JoinType.INNER);
		From<?, ?> numeroRomanoJoin = (From<?, ?>) segueMeJoin.fetch("numeroRomano", JoinType.LEFT);
 
		if (filtro.getDtInicio() != null) {
			Calendar calendar = Calendar.getInstance();
			predicates.add(builder.between(root.get("dataVenda"), filtro.getDtInicio(), calendar.getTime()));
		}
		
		if (filtro.getEvento() != null) {
			predicates.add(builder.equal(root.get("evento"), filtro.getEvento()));
		}
		
		if (filtro.getParaQuemVenda() != null) {
			predicates.add(builder.equal(root.get("paraQuemVenda"), filtro.getParaQuemVenda()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}

		if (filtro.getStatus() != null) {
			predicates.add(builder.equal(root.get("statusVenda"), filtro.getStatus()));
		}

		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("timestamp")));

		TypedQuery<Venda> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

}