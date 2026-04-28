package br.org.ao.depress.assinador.core.service;

import br.org.ao.depress.assinador.core.exception.AssinadorException;
import br.org.ao.depress.assinador.core.model.dto.*;
import br.org.ao.depress.assinador.core.model.enums.SituacaoExcepcional;
import br.org.ao.depress.assinador.core.model.factory.OperationOutcomeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final ObjectMapper objectMapper;

    private static final String MOCK_CPF = "00000000000";
    private static final String MOCK_CERT_B64 = "MIIB...simulado...==";
    private static final String POLICY_URI =
            "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|0.1.2";
    private static final String RESOURCE_TYPE_KEY = "resourceType";

    public String executarAssinatura(File bundleFile, File provenanceFile, String pin)
            throws IOException, NoSuchAlgorithmException {

        validarPin(pin);
        validarBundle(bundleFile);
        validarProvenance(provenanceFile);

        return gerarMockSignature(bundleFile);
    }

    public String validarAssinatura(File signatureFile, File bundleFile) {
        validarArquivoSignature(signatureFile);
        validarArquivoBundle(bundleFile);
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(OperationOutcomeFactory.success());
    }

    private void validarPin(String pin) {
        if (pin == null || !pin.matches("\\d{4,8}")) {
            throw new AssinadorException(
                    SituacaoExcepcional.CRYPTO_PIN_INVALID,
                    "PIN inválido. Deve conter apenas entre 4 e 8 dígitos numéricos."
            );
        }
    }

    private void validarBundle(File bundleFile) {
        if (!bundleFile.exists()) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_BUNDLE_MALFORMED,
                    "Arquivo Bundle não encontrado: " + bundleFile.getPath()
            );
        }

        JsonNode bundle;
        try {
            bundle = objectMapper.readTree(bundleFile);
        } catch (Exception e) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_JSON_MALFORMED,
                    "Bundle não é um JSON válido: " + e.getMessage()
            );
        }

        if (!"Bundle".equals(getTextOrNull(bundle, RESOURCE_TYPE_KEY))) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_BUNDLE_MALFORMED,
                    "Bundle inválido: resourceType deve ser 'Bundle'."
            );
        }

        JsonNode entries = bundle.get("entry");
        if (entries == null || !entries.isArray() || entries.isEmpty()) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_BUNDLE_EMPTY,
                    "Bundle não contém nenhuma entrada para ser assinada."
            );
        }

        for (JsonNode entry : entries) {
            String fullUrl = getTextOrNull(entry, "fullUrl");
            if (fullUrl == null || !fullUrl.matches(
                    "urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                throw new AssinadorException(
                        SituacaoExcepcional.FORMAT_UUID_INVALID,
                        "Bundle inválido: fullUrl ausente ou fora do formato urn:uuid:<UUID>. Valor: " + fullUrl
                );
            }
        }
    }

    private void validarProvenance(File provenanceFile) {
        if (!provenanceFile.exists()) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_PROVENANCE_INVALID,
                    "Arquivo Provenance não encontrado: " + provenanceFile.getPath()
            );
        }

        JsonNode provenance;
        try {
            provenance = objectMapper.readTree(provenanceFile);
        } catch (Exception e) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_JSON_MALFORMED,
                    "Provenance não é um JSON válido: " + e.getMessage()
            );
        }

        if (!"Provenance".equals(getTextOrNull(provenance, RESOURCE_TYPE_KEY))) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_PROVENANCE_INVALID,
                    "Provenance inválido: resourceType deve ser 'Provenance'."
            );
        }

        String recorded = getTextOrNull(provenance, "recorded");
        if (recorded == null || recorded.isBlank()) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_PROVENANCE_INVALID,
                    "Provenance inválido: campo 'recorded' é obrigatório."
            );
        }

        validarJanelaDeTempo(recorded);
    }

    private void validarJanelaDeTempo(String recordedStr) {
        OffsetDateTime dataDocumento;
        try {
            dataDocumento = OffsetDateTime.parse(recordedStr);
        } catch (java.time.format.DateTimeParseException e) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_INVALID_TIMESTAMP,
                    "Formato de data inválido em 'recorded'. Use ISO-8601 (ex: 2026-03-30T14:30:00Z)."
            );
        }

        long diferencaEmMinutos = Math.abs(ChronoUnit.MINUTES.between(
                OffsetDateTime.now(ZoneOffset.UTC), dataDocumento));

        if (diferencaEmMinutos > 5) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_INVALID_TIMESTAMP,
                    "Timestamp fora da janela permitida. Diferença de " + diferencaEmMinutos
                            + " minutos (limite: 5 minutos)."
            );
        }
    }

    private void validarArquivoSignature(File signatureFile) {
        if (!signatureFile.exists()) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_JWS_MALFORMED,
                    "Arquivo Signature não encontrado: " + signatureFile.getPath()
            );
        }

        JsonNode signature;
        try {
            signature = objectMapper.readTree(signatureFile);
        } catch (Exception e) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_JSON_MALFORMED,
                    "Signature não é um JSON válido: " + e.getMessage()
            );
        }

        if (!"Signature".equals(getTextOrNull(signature, RESOURCE_TYPE_KEY))) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_JWS_MALFORMED,
                    "Signature inválido: resourceType deve ser 'Signature'."
            );
        }

        if (getTextOrNull(signature, "data") == null) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_JWS_MALFORMED,
                    "Signature inválido: campo 'data' é obrigatório."
            );
        }
    }

    private void validarArquivoBundle(File bundleFile) {
        if (!bundleFile.exists()) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_BUNDLE_MALFORMED,
                    "Arquivo Bundle não encontrado: " + bundleFile.getPath()
            );
        }
        try {
            objectMapper.readTree(bundleFile);
        } catch (Exception e) {
            throw new AssinadorException(
                    SituacaoExcepcional.FORMAT_JSON_MALFORMED,
                    "Bundle não é um JSON válido: " + e.getMessage()
            );
        }
    }

    String gerarMockSignature(File bundleFile) throws IOException, NoSuchAlgorithmException {
        Instant agora = Instant.now();
        long iat = agora.getEpochSecond();
        String when = OffsetDateTime.ofInstant(agora, ZoneOffset.UTC).toString();

        byte[] bundleBytes = Files.readAllBytes(bundleFile.toPath());
        byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(bundleBytes);
        String payloadB64 = base64url(hashBytes);

        ProtectedHeaderDTO protectedHeader = new ProtectedHeaderDTO(
                "RS256", List.of(MOCK_CERT_B64), new SigPIdDTO(POLICY_URI), iat);
        String protectedHeaderB64 = base64url(objectMapper.writeValueAsBytes(protectedHeader));

        JwsDTO jws = new JwsDTO(payloadB64, List.of(new JwsSignatureDTO(
                protectedHeaderB64,
                new JwsHeaderDTO(new RRefsDTO(List.of(), List.of())),
                base64url("mock-signature-bytes".getBytes(StandardCharsets.UTF_8))
        )));

        String dataB64 = Base64.getEncoder()
                .encodeToString(objectMapper.writeValueAsBytes(jws));

        SignatureDTO signature = new SignatureDTO(
                "Signature",
                List.of(new CodingDTO("urn:iso-astm:E1762-95:2013", "1.2.840.10065.1.12.1.5")),
                when,
                new WhoDTO(new IdentifierDTO("urn:brasil:cpf", MOCK_CPF)),
                "application/jose",
                "application/octet-stream",
                dataB64
        );

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(signature);
    }

    private String base64url(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && value.isString()) ? value.asString() : null;
    }
}