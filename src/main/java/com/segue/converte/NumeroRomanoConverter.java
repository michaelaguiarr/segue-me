package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.NumeroRomano;
import com.segue.repository.NumeroRomanoRepository;

@FacesConverter(forClass = NumeroRomano.class)
public class NumeroRomanoConverter implements Converter {

	@Inject
	private NumeroRomanoRepository repoditory;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		NumeroRomano retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Long id = new Long(value);
			retorno = repoditory.porId(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			NumeroRomano retorno = (NumeroRomano) value;
			return retorno.getId() == null ? null : retorno.getId().toString();
		}
		
		return "";
	}

}