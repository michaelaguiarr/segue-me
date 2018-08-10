package com.segue.security;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.segue.model.AplicacaoModulo;
import com.segue.model.Status;
import com.segue.model.Usuario;
import com.segue.repository.UsuarioRepository;
import com.segue.util.cdi.CDIServiceLocator;

public class AppUserDetailsService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UsuarioRepository repository = CDIServiceLocator.getBean(UsuarioRepository.class);
		Usuario usuario = repository.findByUsuarioEmailSingle(email);

		UsuarioSistema user = null;

		if (usuario != null && usuario.getStatus() == Status.AUTORIZADO) {
			user = new UsuarioSistema(usuario, getGrupos(usuario));
			usuario.setUltimoAcesso(Calendar.getInstance());
			//usuario = repository.save(usuario);
		} else {
			throw new UsernameNotFoundException("Usuário não encontrado.");
		}
		return user;
	}

	private Collection<? extends GrantedAuthority> getGrupos(Usuario usuario) {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().getNome().toUpperCase()));

		for (AplicacaoModulo aplicacaoModulo : usuario.getPerfil().getModulos()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + aplicacaoModulo.getNome().toUpperCase()));
		}

		return authorities;
	}

}
