package br.org.ao.depress.assinador.core.model.dto;

import java.util.List;

public record ProtectedHeaderDTO(
        String alg,
        List<String> x5c,
        SigPIdDTO sigPId,
        long iat
) {}