package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Seguidor;
import com.segue.repository.SeguidorRepository;

@FacesConverter(forClass = Seguidor.class)
public class SeguidorConverter implements Converter {

	@Inject
	private SeguidorRepository repository;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Seguidor retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Integer id = new Integer(value);
			retorno = repository.findById(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Seguidor seguidor = (Seguidor) value;
			return seguidor.getId() == null ? null : seguidor.getId().toString();
		}
		
		return "";
	}

}