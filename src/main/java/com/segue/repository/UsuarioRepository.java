package com.segue.repository;

import java.io.Serializable;
import java.util.ArrayList;
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

import com.segue.filter.UsuarioFilter;
import com.segue.model.Status;
import com.segue.model.Usuario;
import com.segue.service.NegocioException;

public class UsuarioRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	/**
	 * Persiste usuario
	 * 
	 * @param usuario
	 * @return
	 */
	public Usuario save(Usuario usuario) {
		return usuario = manager.merge(usuario);
	}

	/**
	 * Persiste lista de Usuarios
	 * 
	 * @param usuarios
	 * @return
	 * @throws NegocioException
	 */
	public ArrayList<Usuario> saveList(ArrayList<Usuario> usuarios) throws NegocioException {
		for (Usuario usuario : usuarios) {
			try {
				manager.merge(usuario);
				manager.flush();
			} catch (PersistenceException e) {
				throw new NegocioException("Usuário não pode ser inserido. Verifique seu arquivo.");
			}
		}
		return usuarios;
	}

	/**
	 * Carrega usuario com id (necessario utilizar converter)
	 * 
	 * @param id
	 * @return
	 */
	public Usuario findById(Integer id) {
		return manager.find(Usuario.class, id);
	}

	/**
	 * Remove usuario do banco (utilizado para remover solicitacao) - Muito cuidade
	 * ao utilizar
	 * 
	 * @param usuario
	 */
	public void remove(Usuario usuario) {
		manager.remove(manager.getReference(Usuario.class, usuario.getId()));
	}

	/**
	 * Procura usuario pela email
	 * 
	 * @param email
	 * @return
	 */
	public Usuario findByEmail(String email) {
		try {
			return manager
					.createQuery("SELECT distinct(u) FROM Usuario u " + "JOIN FETCH u.sexo " + "JOIN FETCH u.perfil p "
							+ "JOIN FETCH u.segueMe s " + "JOIN FETCH u.equipe e " + "JOIN FETCH u.funcao f "
							+ "JOIN FETCH p.modulos  " + "WHERE u.email = :email", Usuario.class)
					.setParameter("email", email).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	/**
	 * Procura usuario por cod e senha. Uso na funcao de recupera senha.
	 * 
	 * @param codigo,
	 *            hash
	 * @return
	 */
	public Usuario findByCodigoSenha(Integer codigo, String hash) {
		try {
			return manager
					.createQuery(
							"SELECT distinct(u) FROM Usuario u " + "JOIN FETCH u.sexo " + "JOIN FETCH u.perfil p "
									+ "JOIN FETCH p.modulos  " + "WHERE u.cod = :cod AND u.senha = :senha",
							Usuario.class)
					.setParameter("cod", codigo).setParameter("senha", hash).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	/**
	 * Carrega todos
	 * 
	 * @return
	 */
	public List<Usuario> findAll() {
		return manager.createQuery("SELECT distinct(u) FROM Usuario u " + "JOIN FETCH u.sexo "
				+ "JOIN FETCH u.perfil p " + "JOIN FETCH u.segueMe s " + "JOIN FETCH u.equipe e "
				+ "JOIN FETCH u.funcao f " + "JOIN FETCH p.modulos  " + "ORDER BY u.nome", Usuario.class)
				.getResultList();
	}

	/**
	 * Carrega usuario pelo nome
	 * 
	 * @return
	 */
	public List<Usuario> findByUsuarioNome(String nome) {
		return manager
				.createQuery("SELECT distinct(u) FROM Usuario u " + "JOIN FETCH u.sexo " + "JOIN FETCH u.perfil p "
						+ "JOIN FETCH u.segueMe s " + "JOIN FETCH u.equipe e " + "JOIN FETCH u.funcao f "
						+ "JOIN FETCH p.modulos  " + "WHERE u.nome LIKE :nome ORDER BY u.nome", Usuario.class)
				.setParameter("nome", nome + "%").getResultList();
	}

	/**
	 * Carrega usuarios pelo status
	 * 
	 * @return
	 */
	public List<Usuario> findByUsuarioStatus(Status status) {
		return manager
				.createQuery("SELECT distinct(u) FROM Usuario u " + "JOIN FETCH u.sexo " + "JOIN FETCH u.segueMe s "
						+ "JOIN FETCH u.equipe e " + "JOIN FETCH u.funcao f "
						+ "WHERE u.status = :status ORDER BY u.nome", Usuario.class)
				.setParameter("status", status).getResultList();
	}

	/**
	 * Procura usuario por cod e senha. Uso na funcao de recupera senha.
	 * 
	 * @param matricula,
	 *            senha
	 * @return Usuario
	 */
	public Usuario findByMatriculaAndSenha(String email, String senha) {
		try {
			return manager
					.createQuery(
							"SELECT distinct(u) FROM Usuario u " + "JOIN FETCH u.sexo " + "LEFT JOIN u.perfil p "
									+ "JOIN FETCH u.segueMe s " + "JOIN FETCH u.equipe e " + "JOIN FETCH u.funcao f "
									+ "JOIN FETCH p.modulos  " + "WHERE u.email = :email AND u.senha = :senha",
							Usuario.class)
					.setParameter("email", email).setParameter("senha", senha).getSingleResult();
		} catch (NoResultException nr) {
			return null;
		}
	}

	public List<Usuario> filtrados(UsuarioFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Usuario> criteriaQuery = builder.createQuery(Usuario.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Usuario> root = criteriaQuery.from(Usuario.class);
		From<?, ?> sexoJoin = (From<?, ?>) root.fetch("sexo", JoinType.INNER);
		From<?, ?> perfilJoin = (From<?, ?>) root.fetch("perfil", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.INNER);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.INNER);

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.like(builder.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}
		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (filtro.getEquipe() != null) {
			predicates.add(builder.equal(root.get("equipe"), filtro.getEquipe()));
		}

		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("segueMe")));

		TypedQuery<Usuario> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}
	
	
	public List<Usuario> filtradosSolicitacao(UsuarioFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Usuario> criteriaQuery = builder.createQuery(Usuario.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Usuario> root = criteriaQuery.from(Usuario.class);
		From<?, ?> sexoJoin = (From<?, ?>) root.fetch("sexo", JoinType.INNER);
		From<?, ?> perfilJoin = (From<?, ?>) root.fetch("perfil", JoinType.LEFT);
		From<?, ?> segueMeJoin = (From<?, ?>) root.fetch("segueMe", JoinType.INNER);
		From<?, ?> equipeJoin = (From<?, ?>) root.fetch("equipe", JoinType.INNER);
		From<?, ?> funcaoJoin = (From<?, ?>) root.fetch("funcao", JoinType.INNER);

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.like(builder.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
		}
		if (filtro.getParoquia() != null) {
			predicates.add(builder.equal(segueMeJoin.get("paroquia"), filtro.getParoquia()));
		}

		if (filtro.getSegueMe() != null) {
			predicates.add(builder.equal(root.get("segueMe"), filtro.getSegueMe()));
		}
		if (filtro.getEquipe() != null) {
			predicates.add(builder.equal(root.get("equipe"), filtro.getEquipe()));
		}

		predicates.add(builder.equal(root.get("status"), Status.PENDENTE));
		criteriaQuery.select(root);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get("nome")), builder.asc(root.get("segueMe")));

		TypedQuery<Usuario> query = manager.createQuery(criteriaQuery);
		return query.getResultList();
	}

	/**
	 * Verifica se usuario existe atraves da matricula, respondendo resultado unico
	 * 
	 * @param matricula
	 * @return
	 */
	public Usuario findByUsuarioEmailSingle(String email) {
		Usuario usuario = null;
		try {
			usuario = this.manager
					.createQuery("from Usuario u " + "JOIN FETCH u.sexo " + "JOIN FETCH u.perfil p "
							+ "JOIN FETCH u.segueMe s " + "JOIN FETCH u.equipe e " + "JOIN FETCH u.funcao f "
							+ "JOIN FETCH p.modulos  " + "where u.email = :email", Usuario.class)
					.setParameter("email", email).getSingleResult();
		} catch (NoResultException e) {
			// Nenhum usuário encontrado informado
		}
		return usuario;
	}

}
