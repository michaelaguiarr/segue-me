package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.PalestranteConvidado;
import com.segue.repository.PalestranteConvidadoRepository;

@FacesConverter(forClass = PalestranteConvidado.class)
public class PalestranteConvidadoConverter implements Converter {

	@Inject
	private PalestranteConvidadoRepository repository;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		PalestranteConvidado retorno = null;
		
		if (StringUtils.isNotEmpty(value)) {
			Integer id = new Integer(value);
			retorno = repository.findById(id);
		}
		
		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			PalestranteConvidado seguidor = (PalestranteConvidado) value;
			return seguidor.getId() == null ? null : seguidor.getId().toString();
		}
		
		return "";
	}

}