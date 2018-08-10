package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Padre;
import com.segue.repository.PadreRepository;

@FacesConverter(forClass = Padre.class)
public class PadreConverter implements Converter {

	@Inject
	private PadreRepository repository;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Padre retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Integer id = new Integer(value);
			retorno = repository.findById(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Padre seguidor = (Padre) value;
			return seguidor.getId() == null ? null : seguidor.getId().toString();
		}
		
		return "";
	}

}