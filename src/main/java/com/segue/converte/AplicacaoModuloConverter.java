package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.AplicacaoModulo;
import com.segue.repository.AplicacaoModuloRepository;

@FacesConverter("aplicacaoModuloConverter")
public class AplicacaoModuloConverter implements Converter {

	@Inject
	private AplicacaoModuloRepository aplicacaoModuloRepository;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		AplicacaoModulo retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Integer id = new Integer(value);
			retorno = aplicacaoModuloRepository.findById(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			AplicacaoModulo aplicacaoModulo = (AplicacaoModulo) value;
			return aplicacaoModulo.getCod()== null ? null : aplicacaoModulo.getCod().toString();
		}
		
		return "";
	}

}