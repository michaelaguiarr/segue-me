package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Paroquia;
import com.segue.repository.ParoquiaRepository;

@FacesConverter(forClass = Paroquia.class)
public class ParoquiaConverter implements Converter {

	@Inject
	private ParoquiaRepository paroquias;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Paroquia retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Long id = new Long(value);
			retorno = paroquias.porId(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Paroquia retorno = (Paroquia) value;
			return retorno.getId() == null ? null : retorno.getId().toString();
		}
		
		return "";
	}

}