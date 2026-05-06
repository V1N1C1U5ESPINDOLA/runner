package br.org.ao.depress.assinador.core.model.dto;

import java.util.List;

public record JwsDTO(
        String payload,
        List<JwsSignatureDTO> signatures
) {}
