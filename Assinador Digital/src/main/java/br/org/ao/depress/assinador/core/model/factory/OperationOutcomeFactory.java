package br.org.ao.depress.assinador.core.model.factory;

import br.org.ao.depress.assinador.core.exception.AssinadorException;
import br.org.ao.depress.assinador.core.model.dto.*;
import br.org.ao.depress.assinador.core.model.enums.SituacaoExcepcional;

import java.util.List;

public class OperationOutcomeFactory {
    private OperationOutcomeFactory() {
    }

    private static final String CODESYSTEM_URI =
            "https://fhir.saude.go.gov.br/r4/seguranca/CodeSystem/situacao-excepcional-assinatura";

    public static OperationOutcomeDTO fromException(AssinadorException ex) {
        return build(ex.getSituacao(), ex.getDiagnostics());
    }

    public static OperationOutcomeDTO success() {
        return build(SituacaoExcepcional.VALIDATION_SUCCESS, "Assinatura digital validada com sucesso");
    }

    private static OperationOutcomeDTO build(SituacaoExcepcional situacao, String diagnostics) {
        CodingSituacaoDTO coding = new CodingSituacaoDTO(
                CODESYSTEM_URI,
                situacao.getCode(),
                situacao.getDisplay()
        );

        DetailsDTO details = new DetailsDTO(
                List.of(coding),
                situacao.getDisplay()
        );

        String issueCode = switch (situacao.getSeverity()) {
            case "error" -> "invalid";
            case "fatal" -> "exception";
            case "warning" -> "informational";
            default -> "informational";
        };

        IssueDTO issue = new IssueDTO(
                situacao.getSeverity(),
                issueCode,
                details,
                diagnostics
        );

        return new OperationOutcomeDTO("OperationOutcome", List.of(issue));
    }
}