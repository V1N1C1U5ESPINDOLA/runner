package br.org.ao.depress.assinador.core.model.dto;

public record SignRequestDTO(
        String bundlePath,
        String provenancePath,
        String pin
) {}