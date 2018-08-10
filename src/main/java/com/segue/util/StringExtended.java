package com.segue.util;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StringExtended {

	private static final SimpleDateFormat PT_BR = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public static String toASCII(String string) {
		if (string != null) {
			string = Normalizer.normalize(string, Normalizer.Form.NFD);
			string = string.replaceAll("[^\\p{ASCII}]", "");
		}
		return string;
	}

	public static String toPT_BR(Calendar cal) {
		try {
			return PT_BR.format(cal.getTime());
		} catch (Exception e) {
			return "";
		}
	}
}
