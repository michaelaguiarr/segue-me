package com.segue.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public AppUserDetailsService userDetailsService() {
		return new AppUserDetailsService();
	}

	@Bean
	public DaoAuthenticationProvider authProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(new Md5PasswordEncoder());
		return authProvider;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		JsfLoginUrlAuthenticationEntryPoint jsfLoginEntry = new JsfLoginUrlAuthenticationEntryPoint();
		jsfLoginEntry.setLoginFormUrl("/login.xhtml");
		jsfLoginEntry.setRedirectStrategy(new JsfRedirectStrategy());

		JsfAccessDeniedHandler jsfDeniedEntry = new JsfAccessDeniedHandler();
		jsfDeniedEntry.setLoginPath("/access.xhtml");
		jsfDeniedEntry.setContextRelative(true);

		http
				.csrf().disable()
				.headers().frameOptions().sameOrigin()
				.and()

				.authorizeRequests()
				.antMatchers("/login.xhtml", "/error", "/404", "/javax.faces.resource/**", "/login/**", "/seguidor-public/**", "/casal-public/**").permitAll()
				.antMatchers("/dashboard.xhtml", "/access", "/meu-perfil/meu-perfil.xhtml", "/conversationScoped/**").authenticated()
				.antMatchers("/casal/**").hasRole("GERENCIAR_CASAL")
				.antMatchers("/circulo/**").hasRole("GERENCIAR_CIRCULO")
				.antMatchers("/ecc/**").hasRole("GERENCIAR_ECC")
				.antMatchers("/equipe/**").hasRole("GERENCIAR_EQUIPE")
				.antMatchers("/funcao/**").hasRole("GERENCIAR_FUNCAO")
				.antMatchers("/inscricao/**").hasRole("GERENCIAR_FICHAS")
				.antMatchers("/padre/**").hasRole("GERENCIAR_PADRE")
				.antMatchers("/palestra/**").hasRole("GERENCIAR_PALESTRA")
				.antMatchers("/paroquia/**").hasRole("GERENCIAR_PAROQUIA")
				.antMatchers("/perfil/**").hasRole("GERENCIAR_PERFIL")
				.antMatchers("/segue-me/**").hasRole("GERENCIAR_SEGUE_ME")
				.antMatchers("/seguidor/**").hasRole("GERENCIAR_SEGUIDOR")
				.antMatchers("/usuario/**").hasRole("GERENCIAR_USUARIOS")
				.antMatchers("/venda/**").hasRole("GERENCIAR_VENDAS")
				.antMatchers("/evento-casal/**").hasRole("GERENCIAR_EVENTO")
				.antMatchers("/evento-padre/**").hasRole("GERENCIAR_EVENTO")
				.antMatchers("/evento-seguidor/**").hasRole("GERENCIAR_EVENTO")
				.antMatchers("/relatorio-dirigente/**").hasRole("GERENCIAR_RELATORIO_DIR")
				.and()

				
				.formLogin()
					.loginPage("/login.xhtml")
					.failureUrl("/login.xhtml?invalid=true")
					.and()

				.logout()
					.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.and()

				.exceptionHandling()
					.accessDeniedPage("/access")
					.authenticationEntryPoint(jsfLoginEntry)
					.accessDeniedHandler(jsfDeniedEntry);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder builder) throws Exception {
		builder.authenticationProvider(authProvider());

	}

}