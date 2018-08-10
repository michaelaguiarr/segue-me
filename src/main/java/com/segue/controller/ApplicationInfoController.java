package com.segue.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class ApplicationInfoController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Versão da aplicação no pom.xml
	 * @return
	 * @throws IOException
	 */
	public String getVersionApplication() throws IOException{
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream("/application.properties"));
		
		return "Versão " + props.getProperty("versao");
	}

}
