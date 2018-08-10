package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.SegueMe;
import com.segue.repository.SegueMeRepository;

@FacesConverter(forClass = SegueMe.class)
public class SegueMeConverter implements Converter {

	@Inject
	private SegueMeRepository ejcs;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		SegueMe retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Long id = new Long(value);
			retorno = ejcs.porId(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			SegueMe retorno = (SegueMe) value;
			return retorno.getId() == null ? null : retorno.getId().toString();
		}
		
		return "";
	}

}