package com.segue.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import com.segue.model.Inscricao;
import com.segue.model.SegueMe;
import com.segue.model.Seguidor;
import com.segue.service.NegocioException;

public class InscricaoRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Inscricao porId(Long id) {
		return manager.find(Inscricao.class, id);
	}

	public Inscricao guardar(Inscricao inscricao) {
		return inscricao = manager.merge(inscricao);
	}

	public void remover(Inscricao inscricao) throws NegocioException {
		try {
			inscricao = porId(inscricao.getId());
			manager.remove(inscricao);
			manager.flush();
		} catch (PersistenceException e) {
			throw new NegocioException("Inscricao não pode ser excluído.");
		}
	}

	public List<Inscricao> listaALL() {
		return manager.createQuery("SELECT i FROM Inscricao i " + "JOIN FETCH i.seguidor s "
				+ "JOIN FETCH i.responsavelInscricao rp " + "JOIN FETCH i.paroquia p " + "JOIN FETCH i.endereco e ",
				Inscricao.class).getResultList();
	}

	/**
	 * Procura usuario pela email
	 * 
	 * @param email
	 * @return
	 */
	public Inscricao findBySeguidorSegueMe(Seguidor seguidor, SegueMe segueMe) {
		try {
			return manager
					.createQuery("SELECT distinct(i) FROM Inscricao i " + "JOIN FETCH i.seguidor s "
							+ "JOIN FETCH i.responsavelInscricao rp " + "JOIN FETCH i.paroquia p "
							+ "JOIN FETCH i.endereco e " + "WHERE e.segueMe = :segue " + "AND i.seguidor  = :seguidor ",
							Inscricao.class)
					.setParameter("segue", segueMe).setParameter("segue", segueMe).setParameter("seguidor", seguidor)
					.getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public Inscricao findByIdInscricao(Long id) throws NegocioException {
		try {
			return manager
					.createQuery("SELECT distinct(i) FROM Inscricao i " 
							+ "LEFT JOIN i.responsavelInscricao rp "
							+ "LEFT JOIN i.paroquia p "
							+ "LEFT JOIN i.endereco e "
							+ "WHERE i.id = :id ", Inscricao.class)
					.setParameter("id", id).getSingleResult();
		} catch (NoResultException nr) {
			throw new NegocioException("Ficha não pode ser encontrada.");
		}
	}

}