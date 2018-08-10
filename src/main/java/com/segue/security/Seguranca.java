package com.segue.security;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.segue.model.Usuario;
import com.segue.util.jsf.FacesUtil;

@Named
@RequestScoped
public class Seguranca {

	private ExternalContext externalContext = usuarioSessao();

	public String getNomeUsuario() {
		String nome = null;

		UsuarioSistema usuarioLogado = getUsuarioLogado();

		if (usuarioLogado != null) {
			nome = usuarioLogado.getUsuario().getNome();
		}

		return nome;
	}

	public String getPerfilUsuario() {
		String perfil = "";

		UsuarioSistema usuarioLogado = getUsuarioLogado();

		if (usuarioLogado != null) {
			perfil = usuarioLogado.getUsuario().getPerfil().getNomeComIniciaisMaiusculas();
		}

		return perfil;
	}

	public String getDataUltimoAcesso() {
		String data = "";
		UsuarioSistema usuarioLogado = getUsuarioLogado();
		if (usuarioLogado != null) {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			data = df.format(usuarioLogado.getUsuario().getUltimoAcesso().getTime());
		}
		return data;
	}

	public String getImagemPerfil() {
		String imagemDoUsuario = "images/avatar.png";
		UsuarioSistema usuarioLogado = getUsuarioLogado();
		try {
			if (usuarioLogado.getUsuario().getImagem() != null) {
				Integer id = usuarioLogado.getUsuario().getId();
				Path path = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/").toString()
						+ "/resources/temp/usuario");
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				path = Paths.get(path.toRealPath() + "/" + id + ".jpg");
				if (!Files.exists(path)) {
					FileOutputStream fos = new FileOutputStream(path.toString());
					fos.write(usuarioLogado.getUsuario().getImagem());
					fos.close();
				}
				return imagemDoUsuario = "usuario/" + id + ".jpg";

			} else {
				imagemDoUsuario = "usuario/avatar.png";
				return imagemDoUsuario;
			}
		} catch (IOException e) {
			FacesUtil.addErrorMessage(e.getMessage());
			return imagemDoUsuario = "usuario/avatar.png";
		}
	}

	public Usuario usuarioLogado() {
		UsuarioSistema usuarioLogado = getUsuarioLogado();
		return usuarioLogado.getUsuario();
	}

	@Produces
	@UsuarioLogado
	public UsuarioSistema getUsuarioLogado() {

		UsuarioSistema usuario = null;

		UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) FacesContext
				.getCurrentInstance().getExternalContext().getUserPrincipal();

		if (auth != null && auth.getPrincipal() != null) {
			usuario = (UsuarioSistema) auth.getPrincipal();
		}

		return usuario;
	}

	public boolean isAdministradorGeral() {
		UsuarioSistema usuarioLogado = getUsuarioLogado();
		return usuarioLogado.getUsuario().getFuncao().isAdministradorGeral();
	}

	public boolean isAdministrador() {
		UsuarioSistema usuarioLogado = getUsuarioLogado();
		if (usuarioLogado.getUsuario().getEquipe().getId() == 6 || isAdministradorGeral()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isGraficaAndComandante() {
		UsuarioSistema usuarioLogado = getUsuarioLogado();
		if (usuarioLogado.getUsuario().getEquipe().getId() == 15 || usuarioLogado.getUsuario().getEquipe().getId() == 7
				|| isAdministrador() || isAdministradorGeral()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isGrafica() {
		UsuarioSistema usuarioLogado = getUsuarioLogado();
		if (usuarioLogado.getUsuario().getEquipe().getId() == 15) {
			return true;
		} else {
			return false;
		}
	}

	public ExternalContext usuarioSessao() {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getExternalContext();
	}

	public boolean isDeveloper() {
		return externalContext.isUserInRole("DEVELOPER");
	}

	public boolean isGerenciarUsuario() {
		return externalContext.isUserInRole("GERENCIAR_USUARIOS");
	}

	public boolean isGerenciarPerfil() {
		return externalContext.isUserInRole("GERENCIAR_PERFIL");
	}

	public boolean isGerenciarSegueMe() {
		return externalContext.isUserInRole("GERENCIAR_SEGUE_ME");
	}

	public boolean isGerenciarEcc() {
		return externalContext.isUserInRole("GERENCIAR_ECC");
	}

	public boolean isGerenciarEquipe() {
		return externalContext.isUserInRole("GERENCIAR_EQUIPE");
	}

	public boolean isGerenciarFuncao() {
		return externalContext.isUserInRole("GERENCIAR_FUNCAO");
	}

	public boolean isGerenciarCirculo() {
		return externalContext.isUserInRole("GERENCIAR_CIRCULO");
	}

	public boolean isGerenciarPalestra() {
		return externalContext.isUserInRole("GERENCIAR_PALESTRA");
	}

	public boolean isGerenciarFichas() {
		return externalContext.isUserInRole("GERENCIAR_FICHAS");
	}
	
	public boolean isGerenciarParoquia() {
		return externalContext.isUserInRole("GERENCIAR_PAROQUIA");
	}

	public boolean isGerenciarEvento() {
		return externalContext.isUserInRole("GERENCIAR_EVENTO");
	}
	
	public boolean isGerenciarSeguidor() {
		return externalContext.isUserInRole("GERENCIAR_SEGUIDOR");
	}
	
	public boolean isGerenciarCasal() {
		return externalContext.isUserInRole("GERENCIAR_CASAL");
	}
	
	public boolean isGerenciarRelatorioDirigente() {
		return externalContext.isUserInRole("GERENCIAR_RELATORIO_DIR");
	}
	
	public boolean isGerenciarPadre() {
		return externalContext.isUserInRole("GERENCIAR_PADRE");
	}
	
	public boolean isGerenciarVenda() {
		return externalContext.isUserInRole("GERENCIAR_VENDAS");
	}
	
	public boolean isSettings() {
		return isDeveloper() || isGerenciarSegueMe() || isGerenciarEquipe()
				|| isGerenciarFuncao() || isGerenciarPerfil() || isGerenciarCirculo()
				|| isGerenciarPalestra() || isGerenciarParoquia();
	}

}
