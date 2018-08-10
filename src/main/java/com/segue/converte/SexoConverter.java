package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Sexo;
import com.segue.repository.SexoRepository;

@FacesConverter(forClass = Sexo.class)
public class SexoConverter implements Converter {

	@Inject
	private SexoRepository repoditory;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Sexo retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Integer id = new Integer(value);
			retorno = repoditory.porId(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Sexo retorno = (Sexo) value;
			return retorno.getCod() == null ? null : retorno.getCod().toString();
		}
		
		return "";
	}

}