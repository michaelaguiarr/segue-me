package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Perfil;
import com.segue.repository.PerfilRepository;

@FacesConverter(forClass = Perfil.class)
public class PerfilConverter implements Converter {

	@Inject
	private PerfilRepository perfilRepository;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Perfil retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Long id = new Long(value);
			retorno = perfilRepository.porId(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Perfil perfil = (Perfil) value;
			return perfil.getId()== null ? null : perfil.getId().toString();
		}
		
		return "";
	}

}