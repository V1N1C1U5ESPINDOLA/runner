package br.org.ao.depress.assinador.core.model.dto;

public record ValidateRequestDTO(
        String signaturePath,
        String bundlePath
) {}