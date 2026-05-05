package br.org.ao.depress.assinador.core.model.dto;

import java.util.List;

public record RRefsDTO(
        List<Object> ocspRefs,
        List<Object> crlRefs
) {}