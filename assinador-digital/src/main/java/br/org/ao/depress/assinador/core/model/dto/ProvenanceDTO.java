package br.org.ao.depress.assinador.core.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProvenanceDTO(
        String recorded
) {}
