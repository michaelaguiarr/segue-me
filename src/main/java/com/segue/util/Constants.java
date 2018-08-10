package com.segue.util;

import javax.faces.context.FacesContext;

public class Constants {

	public static final String CONTEXT = FacesContext.getCurrentInstance().getExternalContext()
			.getRequestContextPath();
	
	
	

}
