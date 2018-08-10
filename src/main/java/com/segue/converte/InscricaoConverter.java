package com.segue.converte;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.segue.model.Inscricao;
import com.segue.repository.InscricaoRepository;

@FacesConverter(forClass = Inscricao.class)
public class InscricaoConverter implements Converter {

	@Inject
	private InscricaoRepository repository;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Inscricao retorno = null;

		if (StringUtils.isNotEmpty(value)) {
			Long id = new Long(value);
			retorno = repository.porId(id);
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Inscricao retorno = (Inscricao) value;
			return retorno.getId() == null ? null : retorno.getId().toString();
		}

		return "";
	}

}