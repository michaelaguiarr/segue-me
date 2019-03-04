package com.segue.util;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import com.segue.service.NegocioException;

public class ConverteParaMD5 {

	/**
	 * Converte senha para MD5
	 * 
	 * @param senhaTexto
	 * @return
	 */
	public static String toMD5(String senha) throws NegocioException {
		try {
			Md5PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();
			String encoded = md5PasswordEncoder.encodePassword(senha, null);
			return encoded;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new NegocioException("Senha inválida! Tente outra sequencia.");
		}
	}

}
