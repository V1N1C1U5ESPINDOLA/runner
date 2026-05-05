package br.org.ao.depress.assinador.core.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CodingSituacaoDTO(
        @JsonProperty("system") String system,
        @JsonProperty("code") String code,
        @JsonProperty("display") String display
) {}