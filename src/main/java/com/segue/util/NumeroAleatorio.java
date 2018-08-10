package com.segue.util;

public class NumeroAleatorio {

	/**
	 * Gera numero aleatorio
	 * @return
	 */
	public static Integer gerarNumeroAleatorio() {
		int r = (int) (Math.random() * 10000000 + 2018);
		return r;
	}

}
