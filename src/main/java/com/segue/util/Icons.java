package com.segue.util;

import com.segue.model.Sexo;

public class Icons {

	/**
	 * Exibe icone para sexo
	 * 
	 * @return
	 */
	public static String getIconBySexo(Sexo sexo) {
		Integer icon;
		if (sexo == null) {
			icon = 0;
		} else {
			icon = sexo.getCod();
		}
		switch (icon) {
		case 1:
			return "fa fa-female";
		case 2:
			return "fa fa-male";
		default:
			return "fa fa-question";
		}
	}

}
