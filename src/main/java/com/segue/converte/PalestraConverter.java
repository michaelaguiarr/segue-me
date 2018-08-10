package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Palestra;
import com.segue.repository.PalestraRepository;

@FacesConverter(forClass = Palestra.class)
public class PalestraConverter implements Converter {

	@Inject
	private PalestraRepository repository;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Palestra retorno = null;

		if (StringUtils.isNotEmpty(value)) {
			Long id = new Long(value);
			retorno = repository.porId(id);
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Palestra retorno = (Palestra) value;
			return retorno.getId() == null ? null : retorno.getId().toString();
		}

		return "";
	}

}