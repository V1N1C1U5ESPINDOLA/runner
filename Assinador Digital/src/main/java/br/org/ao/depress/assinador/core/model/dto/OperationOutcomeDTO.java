package br.org.ao.depress.assinador.core.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OperationOutcomeDTO(
        @JsonProperty("resourceType") String resourceType,
        @JsonProperty("issue") List<IssueDTO> issue
) {}