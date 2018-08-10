package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Casal;
import com.segue.repository.CasalRepository;

@FacesConverter(forClass = Casal.class)
public class CasalConverter implements Converter {

	@Inject
	private CasalRepository repository;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Casal retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Integer id = new Integer(value);
			retorno = repository.findById(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Casal casal = (Casal) value;
			return casal.getId() == null ? null : casal.getId().toString();
		}
		
		return "";
	}

}