package br.org.ao.depress.assinador.core.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JwsSignatureDTO(
        @JsonProperty("protected") String protectedHeader,
        @JsonProperty("header") JwsHeaderDTO header,
        @JsonProperty("signature") String signature
) {}