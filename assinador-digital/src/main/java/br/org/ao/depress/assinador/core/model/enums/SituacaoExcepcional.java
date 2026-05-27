package br.org.ao.depress.assinador.core.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Códigos de situação excepcional do CodeSystem
 * {@code situacao-excepcional-assinatura} da SES-GO.
 *
 * <p>Cada constante representa uma situação prevista na especificação FHIR de segurança
 * da SES-GO e carrega os três atributos necessários para montar o {@code issue} de um
 * {@code OperationOutcome}: o código do CodeSystem ({@code code}), a severidade FHIR
 * ({@code severity}) e o texto de exibição ({@code display}).
 *
 * <p>Utilizado por {@link br.org.ao.depress.assinador.core.exception.AssinadorException}
 * para tipificar erros e por
 * {@link br.org.ao.depress.assinador.core.model.factory.OperationOutcomeFactory}
 * para construir as respostas estruturadas.
 *
 * @see <a href="https://fhir.saude.go.gov.br/r4/seguranca">Guia de Implementação SES-GO</a>
 */
@RequiredArgsConstructor
@Getter
public enum SituacaoExcepcional {
    FORMAT_JSON_MALFORMED("FORMAT.JSON-MALFORMED", "error", "JSON Malformado"),
    FORMAT_BUNDLE_MALFORMED("FORMAT.BUNDLE-MALFORMED", "error", "Bundle FHIR Malformado"),
    FORMAT_BUNDLE_EMPTY("FORMAT.BUNDLE-EMPTY", "error", "Bundle Vazio"),
    FORMAT_UUID_INVALID("FORMAT.UUID-INVALID", "error", "UUID Inválido"),
    FORMAT_PROVENANCE_INVALID("FORMAT.PROVENANCE-INVALID", "error", "Provenance Inválido"),
    FORMAT_INVALID_TIMESTAMP("FORMAT.INVALID-TIMESTAMP", "error", "Timestamp Inválido"),
    FORMAT_JWS_MALFORMED("FORMAT.JWS-MALFORMED", "error", "JWS Malformado"),

    CRYPTO_PIN_INVALID("CRYPTO.PIN-INVALID", "error", "PIN Inválido"),

    VALIDATION_SUCCESS("VALIDATION.SUCCESS", "information", "Validação Bem-sucedida");

    private final String code;
    private final String severity;
    private final String display;
}