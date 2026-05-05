package br.org.ao.depress.assinador.core.exception;

import br.org.ao.depress.assinador.core.model.enums.SituacaoExcepcional;
import lombok.Getter;

@Getter
public class AssinadorException extends RuntimeException {

    private final SituacaoExcepcional situacao;
    private final String diagnostics;

    public AssinadorException(SituacaoExcepcional situacao, String diagnostics) {
        super(diagnostics);
        this.situacao = situacao;
        this.diagnostics = diagnostics;
    }
}