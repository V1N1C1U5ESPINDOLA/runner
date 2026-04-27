package br.org.ao.depress.assinador.core.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IssueDTO(
        @JsonProperty("severity") String severity,
        @JsonProperty("code") String code,
        @JsonProperty("details") DetailsDTO details,
        @JsonProperty("diagnostics") String diagnostics
) {}