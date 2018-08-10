package com.segue.service;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import com.segue.model.Usuario;
import com.segue.repository.UsuarioRepository;
import com.segue.security.Seguranca;
import com.segue.util.ConverteParaMD5;
import com.segue.util.jpa.Transactional;

public class CadastroUsuarioService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UsuarioRepository repository;
	
	@Inject
	private Seguranca seguranca;

	@Transactional
	public Usuario salvar(Usuario usuario) throws NegocioException {
		Usuario usuarioEmail = repository.findByEmail(usuario.getEmail());
		if (usuarioEmail != null && !usuarioEmail.equals(usuario)) {
			throw new NegocioException("Email informado já existe!");
		}
		return repository.save(usuario);
	}

	@Transactional
	public void remover(Usuario usuario) throws NegocioException {
		repository.remove(usuario);
	}

	/**
	 * Confere senha atual
	 * 
	 * @param senhaAtual
	 * @param usuario
	 * @return
	 * @throws NegocioException
	 */
	public boolean businessSenha(String senhaAtual, Usuario usuario) throws NegocioException {
		boolean mudarSenha = ConverteParaMD5.toMD5(senhaAtual).equals(usuario.getSenha());
		if (!mudarSenha) {
			throw new NegocioException("Senha atual inválida!");
		}
		return mudarSenha;
	}

	/**
	 * Busca usuario logado - (somente o usuario logado)
	 * 
	 */
	public Usuario loadUsuarioLogado(Usuario usuario) {
		try {
			usuario = repository.findById(usuario.getId());
			Usuario usuarioLogado = seguranca.getUsuarioLogado().getUsuario();
			if (!usuario.equals(usuarioLogado)) {
				String context = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect(context + "/access?faces-redirect=true");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return usuario;
	}
}
