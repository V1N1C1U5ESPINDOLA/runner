package br.org.ao.depress.assinador.core.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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