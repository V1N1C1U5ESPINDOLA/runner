package br.org.ao.depress.assinador.core.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DetailsDTO(
        @JsonProperty("coding") List<CodingSituacaoDTO> coding,
        @JsonProperty("text") String text
) {}
