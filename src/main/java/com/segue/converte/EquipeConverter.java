package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Equipe;
import com.segue.repository.EquipeRepository;

@FacesConverter(forClass = Equipe.class)
public class EquipeConverter implements Converter {

	@Inject
	private EquipeRepository repoditory;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Equipe retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Long id = new Long(value);
			retorno = repoditory.porId(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Equipe retorno = (Equipe) value;
			return retorno.getId() == null ? null : retorno.getId().toString();
		}
		
		return "";
	}

}