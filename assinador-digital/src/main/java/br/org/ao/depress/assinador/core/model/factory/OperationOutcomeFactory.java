package br.org.ao.depress.assinador.core.model.factory;

import br.org.ao.depress.assinador.core.exception.AssinadorException;
import br.org.ao.depress.assinador.core.model.dto.*;
import br.org.ao.depress.assinador.core.model.enums.SituacaoExcepcional;

import java.util.List;

/**
 * Fábrica para construção de recursos FHIR {@code OperationOutcome} conformes ao perfil
 * da SES-GO, usando o CodeSystem
 * {@code situacao-excepcional-assinatura}.
 *
 * <p>Centraliza a montagem do {@code OperationOutcome} para garantir que todos os campos
 * obrigatórios — {@code resourceType}, {@code issue.severity}, {@code issue.code},
 * {@code issue.details.coding} e {@code issue.diagnostics} — estejam sempre presentes
 * e consistentes, tanto para respostas de erro quanto de sucesso.
 *
 * <p>O mapeamento de {@code severity} para {@code issue.code} (tipo FHIR) segue a convenção:
 * {@code error} → {@code invalid}, {@code fatal} → {@code exception},
 * demais → {@code informational}.
 */
public class OperationOutcomeFactory {
    private OperationOutcomeFactory() {
    }

    private static final String CODESYSTEM_URI =
            "https://fhir.saude.go.gov.br/r4/seguranca/CodeSystem/situacao-excepcional-assinatura";

    /**
     * Cria um {@code OperationOutcome} de erro a partir de uma {@link AssinadorException}.
     *
     * @param ex exceção contendo o código de situação excepcional e a mensagem de diagnóstico
     * @return {@link OperationOutcomeDTO} com {@code severity: "error"} e o código correspondente
     */
    public static OperationOutcomeDTO fromException(AssinadorException ex) {
        return build(ex.getSituacao(), ex.getDiagnostics());
    }

    /**
     * Cria um {@code OperationOutcome} de erro para falhas inesperadas do sistema
     * (ex.: I/O, algoritmo indisponível) que não correspondem a erros de validação do usuário.
     *
     * <p>Não usa o CodeSystem da SES-GO pois essas situações não são previstas no perfil
     * de segurança — são erros de infraestrutura, não de negócio.
     *
     * @param diagnostics mensagem descritiva da causa do erro
     * @return {@link OperationOutcomeDTO} com {@code severity: "error"} e {@code code: "exception"}
     */
    public static OperationOutcomeDTO systemError(String diagnostics) {
        DetailsDTO details = new DetailsDTO(List.of(), "Erro interno do sistema");
        IssueDTO issue = new IssueDTO("error", "exception", details, diagnostics);
        return new OperationOutcomeDTO("OperationOutcome", List.of(issue));
    }

    /**
     * Cria um {@code OperationOutcome} de sucesso para o fluxo de validação de assinatura.
     *
     * @return {@link OperationOutcomeDTO} com {@code severity: "information"} e
     *         código {@code VALIDATION.SUCCESS}
     */
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