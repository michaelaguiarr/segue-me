package com.segue.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Date;

public class CalcularIdade implements Serializable {

	private static final long serialVersionUID = 1L;
	

	public static String contaDias(Date dataInicialBR, Date dataFinalBR) throws ParseException {
		//DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		//df.setLenient(false);
		//Date dataInicio = df.parse(dataInicialBR);
		//Date dataFim = df.parse(dataFinalBR);
		long dt = (dataFinalBR.getTime() - dataInicialBR.getTime()) + 3600000;
		Long diasCorridosAnoLong = (dt / 86400000L);
		Integer diasDecorridosInt = Integer.valueOf(diasCorridosAnoLong.toString());
		String diasDecorridos = String.valueOf(diasDecorridosInt); // Sem
																	// numeros
																	// formatados;
		return diasDecorridos;
	}

	// agora para calcular a idade
	public static BigDecimal calculaIdade(Date dataDoMeuNascimento) throws ParseException {
		BigDecimal qtdDias = new BigDecimal(contaDias(dataDoMeuNascimento, new Date()));
		BigDecimal ano = new BigDecimal(365.25);
		BigDecimal idade = qtdDias.divide(ano, 0, RoundingMode.DOWN);
		return idade;
	}
	
	
	public static Integer calculaIdadeSegueMe(Date dataDoMeuNascimento, Date dataDoSegueMe) throws ParseException {
		BigDecimal qtdDias = new BigDecimal(contaDias(dataDoMeuNascimento, dataDoSegueMe));
		BigDecimal ano = new BigDecimal(365.25);
		BigDecimal idade = qtdDias.divide(ano, 0, RoundingMode.DOWN);
		return  idade.intValue();
	}
	
	
	
}
