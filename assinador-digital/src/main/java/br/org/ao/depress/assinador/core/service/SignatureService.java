package br.org.ao.depress.assinador.core.service;

import br.org.ao.depress.assinador.core.exception.AssinadorException;
import br.org.ao.depress.assinador.core.model.dto.*;
import br.org.ao.depress.assinador.core.model.enums.SituacaoExcepcional;
import br.org.ao.depress.assinador.core.model.factory.OperationOutcomeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Serviço responsável pela simulação de operações de assinatura digital no padrão FHIR da SES-GO.
 *
 * <p>Implementa dois fluxos principais:
 * <ul>
 *   <li><b>Criação:</b> valida os parâmetros de entrada e retorna um recurso {@code Signature} FHIR
 *       simulado, com estrutura conforme o perfil
 *       <a href="https://fhir.saude.go.gov.br/r4/seguranca">Assinatura digital avançada da SES-GO</a>.</li>
 *   <li><b>Validação:</b> verifica a estrutura mínima dos arquivos recebidos e retorna um
 *       {@code OperationOutcome} FHIR indicando sucesso ou o erro encontrado.</li>
 * </ul>
 *
 * <p><b>Sobre a simulação:</b> a assinatura criptográfica real está fora do escopo deste componente.
 * O campo {@code data} do {@code Signature} retornado contém um JWS JSON Serialization (RFC 7515 §3.2)
 * estruturalmente correto, mas com valores simulados. O CPF do signatário é extraído do certificado
 * no dispositivo PKCS#11 via {@link Pkcs11Service} quando disponível; caso contrário, um valor fixo
 * simulado é usado como fallback.
 *
 * <p>Erros de validação são lançados como {@link AssinadorException} com o código correspondente
 * do CodeSystem {@code situacao-excepcional-assinatura} da SES-GO, permitindo tratamento uniforme
 * tanto no modo CLI quanto no modo servidor HTTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignatureService {

    private final ObjectMapper objectMapper;
    private final Pkcs11Service pkcs11Service;

    private static final String MOCK_CPF = "00000000000";
    private static final String MOCK_CERT_B64 = "MIIB...simulado...==";
    private static final String POLICY_URI =
            "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|0.1.2";
    private static final String RESOURCE_TYPE_KEY = "resourceType";

    /**
     * Valida os parâmetros de entrada e retorna uma assinatura digital simulada no formato FHIR.
     *
     * <p>A ordem de validação é:
     * <ol>
     *   <li>PIN — formato numérico, 4 a 8 dígitos</li>
     *   <li>Bundle — existência, JSON válido, {@code resourceType: "Bundle"}, ao menos uma
     *       {@code entry}, e {@code fullUrl} de cada entrada no formato {@code urn:uuid:<UUID>}</li>
     *   <li>Provenance — existência, JSON válido, {@code resourceType: "Provenance"}, campo
     *       {@code recorded} presente em ISO-8601 e dentro da janela de ±5 minutos</li>
     * </ol>
     *
     * <p>O CPF do signatário no {@code Signature} retornado é extraído do certificado no dispositivo
     * PKCS#11 quando disponível. Se o dispositivo não estiver acessível, o valor fixo
     * {@code "00000000000"} é usado e um aviso é registrado em stderr.
     *
     * @param bundleFile     arquivo JSON contendo o recurso FHIR {@code Bundle} a ser assinado
     * @param provenanceFile arquivo JSON contendo o recurso FHIR {@code Provenance} do documento
     * @param pin            PIN numérico do dispositivo PKCS#11 (4 a 8 dígitos)
     * @return {@link SignatureDTO} com a assinatura simulada em conformidade com o perfil SES-GO
     * @throws AssinadorException       se qualquer parâmetro for inválido, com o código de erro correspondente
     * @throws IOException              se ocorrer erro de leitura dos arquivos
     * @throws NoSuchAlgorithmException se o algoritmo SHA-256 não estiver disponível na JVM
     */
    public SignatureDTO executarAssinatura(File bundleFile, File provenanceFile, String pin)
            throws IOException, NoSuchAlgorithmException {
        validarPin(pin);
        validarBundle(bundleFile);
        validarProvenance(provenanceFile);

        return gerarMockSignature(bundleFile);
    }

    /**
     * Valida a estrutura dos arquivos recebidos e retorna o resultado da validação simulada.
     *
     * <p>A validação é estrutural — verifica se o {@code Signature} é um JSON válido com
     * {@code resourceType: "Signature"} e campo {@code data} presente, e se o Bundle existe
     * e é um JSON válido. Não há verificação criptográfica da assinatura.
     *
     * <p>Para entradas bem formadas, sempre retorna {@code VALIDATION.SUCCESS}. Entradas
     * inválidas lançam {@link AssinadorException} com o código de erro correspondente.
     *
     * @param signatureFile arquivo JSON contendo o recurso FHIR {@code Signature} a ser validado
     * @param bundleFile    arquivo JSON contendo o recurso FHIR {@code Bundle} original
     * @return {@link OperationOutcomeDTO} com {@code severity: "information"} e
     *         código {@code VALIDATION.SUCCESS} em caso de sucesso
     * @throws AssinadorException se qualquer arquivo estiver ausente ou malformado
     */
    public OperationOutcomeDTO validarAssinatura(File signatureFile, File bundleFile) {
        validarArquivoSignature(signatureFile);
        validarArquivoBundle(bundleFile);
        return OperationOutcomeFactory.success();
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

    /**
     * Gera um recurso FHIR {@code Signature} simulado em conformidade com o perfil
     * Assinatura digital avançada da SES-GO.
     *
     * <p>O campo {@code data} contém um JWS JSON Serialization (RFC 7515 §3.2) codificado
     * em base64 padrão. Internamente, o JWS é composto por:
     * <ul>
     *   <li>{@code payload} — hash SHA-256 real do conteúdo do Bundle, em base64url</li>
     *   <li>{@code signatures[0].protected} — protected header com {@code alg}, {@code x5c},
     *       {@code sigPId} (política SES-GO) e {@code iat}, em base64url</li>
     *   <li>{@code signatures[0].header} — cabeçalho não protegido com {@code rRefs} vazios</li>
     *   <li>{@code signatures[0].signature} — valor simulado fixo, em base64url</li>
     * </ul>
     *
     * <p>O campo {@code when} do {@code Signature} e o campo {@code iat} do protected header
     * são gerados a partir do mesmo {@link Instant}, garantindo consistência entre os dois.
     *
     * @param bundleFile arquivo Bundle cujo conteúdo será usado para calcular o {@code payload}
     * @return {@link SignatureDTO} pronto para serialização
     * @throws IOException              se ocorrer erro de leitura do Bundle
     * @throws NoSuchAlgorithmException se SHA-256 não estiver disponível
     */
    SignatureDTO gerarMockSignature(File bundleFile) throws IOException, NoSuchAlgorithmException {
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

        String cpf = pkcs11Service.extrairCpf().orElseGet(() -> {
            log.warn("PKCS#11 indisponível — usando CPF simulado no campo who.identifier.value");
            return MOCK_CPF;
        });

        return new SignatureDTO(
                "Signature",
                List.of(new CodingDTO("urn:iso-astm:E1762-95:2013", "1.2.840.10065.1.12.1.5")),
                when,
                new WhoDTO(new IdentifierDTO("urn:brasil:cpf", cpf)),
                "application/jose",
                "application/octet-stream",
                dataB64
        );
    }

    private String base64url(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && value.isString()) ? value.asString() : null;
    }
}