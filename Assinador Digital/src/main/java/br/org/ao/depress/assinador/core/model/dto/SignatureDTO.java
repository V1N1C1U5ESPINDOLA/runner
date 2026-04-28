package br.org.ao.depress.assinador.core.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SignatureDTO(
        @JsonProperty("resourceType") String resourceType,
        @JsonProperty("type") List<CodingDTO> type,
        @JsonProperty("when") String when,
        @JsonProperty("who") WhoDTO who,
        @JsonProperty("sigFormat") String sigFormat,
        @JsonProperty("targetFormat") String targetFormat,
        @JsonProperty("data") String data
) {}