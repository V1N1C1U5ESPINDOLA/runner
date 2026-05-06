package br.org.ao.depress.assinador.steps;

import br.org.ao.depress.assinador.core.service.SignatureService;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class AssinarSteps {

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private ObjectMapper objectMapper;

    private File bundleFile;
    private File provenanceFile;
    private Exception excecaoCapturada;
    private JsonNode signatureNode;
    private JsonNode jwsNode;
    private File signatureFile;
    private String resultadoValidacao;

    @Dado("que existe um arquivo Bundle válido")
    public void existeArquivoBundleValido() throws Exception {
        bundleFile = File.createTempFile("bundle", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("""
                {
                  "resourceType": "Bundle",
                  "type": "collection",
                  "entry": [
                    {
                      "fullUrl": "urn:uuid:3fa85f64-5717-4562-b3fc-2c963f66afa6",
                      "resource": { "resourceType": "Patient", "id": "exemplo" }
                    }
                  ]
                }
                """);
        }
    }

    @Dado("que existe um arquivo Provenance com recorded dentro da janela de 5 minutos")
    public void existeArquivoProvenanceValido() throws Exception {
        provenanceFile = File.createTempFile("provenance", ".json");
        provenanceFile.deleteOnExit();

        String recorded = OffsetDateTime.now(ZoneOffset.UTC).toString();
        try (FileWriter fw = new FileWriter(provenanceFile)) {
            fw.write("""
                {
                  "resourceType": "Provenance",
                  "recorded": "%s"
                }
                """.formatted(recorded));
        }
    }

    @Dado("que existe um arquivo Bundle com resourceType {string}")
    public void existeArquivoBundleComResourceType(String type) throws Exception {
        bundleFile = File.createTempFile("bundle_invalid_type", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("""
                {
                  "resourceType": "%s",
                  "type": "collection",
                  "entry": [
                    {
                      "fullUrl": "urn:uuid:3fa85f64-5717-4562-b3fc-2c963f66afa6",
                      "resource": { "resourceType": "Patient", "id": "exemplo" }
                    }
                  ]
                }
                """.formatted(type));
        }
    }

    @Dado("que existe um arquivo Bundle vazio")
    public void existeArquivoBundleVazio() throws Exception {
        bundleFile = File.createTempFile("bundle_empty", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("""
                {
                  "resourceType": "Bundle",
                  "type": "collection",
                  "entry": []
                }
                """);
        }
    }

    @Dado("que existe um arquivo Bundle com fullUrl inválido")
    public void existeArquivoBundleFullUrlInvalido() throws Exception {
        bundleFile = File.createTempFile("bundle_bad_url", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("""
                {
                  "resourceType": "Bundle",
                  "type": "collection",
                  "entry": [
                    {
                      "fullUrl": "http://exemplo.com/paciente",
                      "resource": { "resourceType": "Patient", "id": "exemplo" }
                    }
                  ]
                }
                """);
        }
    }

    @Dado("que existe um arquivo Provenance com resourceType {string}")
    public void existeArquivoProvenanceComResourceType(String type) throws Exception {
        provenanceFile = File.createTempFile("provenance_invalid_type", ".json");
        provenanceFile.deleteOnExit();
        String recorded = OffsetDateTime.now(ZoneOffset.UTC).toString();
        try (FileWriter fw = new FileWriter(provenanceFile)) {
            fw.write("""
                {
                  "resourceType": "%s",
                  "recorded": "%s"
                }
                """.formatted(type, recorded));
        }
    }

    @Dado("que existe um arquivo Provenance sem o campo recorded")
    public void existeArquivoProvenanceSemRecorded() throws Exception {
        provenanceFile = File.createTempFile("provenance_no_recorded", ".json");
        provenanceFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(provenanceFile)) {
            fw.write("""
                {
                  "resourceType": "Provenance"
                }
                """);
        }
    }

    @Dado("que existe um arquivo Provenance com recorded inválido {string}")
    public void existeArquivoProvenanceComRecordedInvalido(String recorded) throws Exception {
        provenanceFile = File.createTempFile("provenance_bad_recorded", ".json");
        provenanceFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(provenanceFile)) {
            fw.write("""
                {
                  "resourceType": "Provenance",
                  "recorded": "%s"
                }
                """.formatted(recorded));
        }
    }

    @Dado("que existe um arquivo Provenance com recorded de {int} minutos atrás")
    public void existeArquivoProvenanceForaDaJanela(int minutos) throws Exception {
        provenanceFile = File.createTempFile("provenance_old", ".json");
        provenanceFile.deleteOnExit();
        String recorded = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(minutos).toString();
        try (FileWriter fw = new FileWriter(provenanceFile)) {
            fw.write("""
                {
                  "resourceType": "Provenance",
                  "recorded": "%s"
                }
                """.formatted(recorded));
        }
    }

    @Dado("que existe um arquivo Signature válido")
    public void existeArquivoSignatureValido() throws Exception {
        existeArquivoProvenanceValido();
        String mockSignatureJson = signatureService.executarAssinatura(bundleFile, provenanceFile, "1234");

        signatureFile = File.createTempFile("signature_valid", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write(mockSignatureJson);
        }
    }

    @Quando("o usuário executa o comando assinar com PIN {string}")
    public void usuarioExecutaAssinar(String pin) {
        try {
            if ("null".equalsIgnoreCase(pin)) {
                pin = null;
            }

            String resultado = signatureService.executarAssinatura(bundleFile, provenanceFile, pin);
            signatureNode = objectMapper.readTree(resultado);

            String dataB64 = signatureNode.get("data").asString();
            byte[] jwsBytes = Base64.getDecoder().decode(dataB64);
            jwsNode = objectMapper.readTree(new String(jwsBytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o usuário executa o comando validar assinatura")
    public void usuarioExecutaValidarAssinatura() {
        try {
            resultadoValidacao = signatureService.validarAssinatura(signatureFile, bundleFile);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("a resposta deve ter resourceType igual a {string}")
    public void verificaResourceType(String esperado) {
        assertThat(signatureNode.get("resourceType").asString()).isEqualTo(esperado);
    }

    @Então("o campo type deve conter code igual a {string}")
    public void verificaTypeCode(String esperado) {
        JsonNode code = signatureNode.get("type").get(0).get("code");
        assertThat(code.asString()).isEqualTo(esperado);
    }

    @Então("o campo who deve conter identifier com system {string}")
    public void verificaWhoIdentifierSystem(String esperado) {
        JsonNode system = signatureNode.get("who").get("identifier").get("system");
        assertThat(system.asString()).isEqualTo(esperado);
    }

    @Então("o campo who não deve conter o campo display")
    public void verificaWhoSemDisplay() {
        assertThat(signatureNode.get("who").has("display")).isFalse();
    }

    @Então("o campo sigFormat deve ser {string}")
    public void verificaSigFormat(String esperado) {
        assertThat(signatureNode.get("sigFormat").asString()).isEqualTo(esperado);
    }

    @Então("o campo targetFormat deve ser {string}")
    public void verificaTargetFormat(String esperado) {
        assertThat(signatureNode.get("targetFormat").asString()).isEqualTo(esperado);
    }

    @Então("o campo data deve ser decodificável em base64")
    public void verificaDataDecodificavel() {
        assertThat(jwsNode).isNotNull();
    }

    @Então("o JWS decodificado deve conter o campo payload")
    public void verificaJwsPayload() {
        assertThat(jwsNode.has("payload")).isTrue();
    }

    @Então("o JWS decodificado deve conter signatures com pelo menos uma entrada")
    public void verificaJwsSignatures() {
        assertThat(jwsNode.get("signatures").size()).isGreaterThanOrEqualTo(1);
    }

    @Então("cada signature deve conter os campos protected, header e signature")
    public void verificaJwsSignatureFields() {
        JsonNode sig = jwsNode.get("signatures").get(0);
        assertThat(sig.has("protected")).isTrue();
        assertThat(sig.has("header")).isTrue();
        assertThat(sig.has("signature")).isTrue();
    }

    @Então("o campo when do Signature deve corresponder ao iat do protected header")
    public void verificaWhenIatConsistentes() {
        String when = signatureNode.get("when").asString();
        String protectedB64 = jwsNode.get("signatures").get(0).get("protected").asString();
        byte[] protectedBytes = Base64.getUrlDecoder().decode(protectedB64);
        JsonNode protectedNode = objectMapper.readTree(new String(protectedBytes, StandardCharsets.UTF_8));

        long iat = protectedNode.get("iat").asLong();
        Instant iatInstant = Instant.ofEpochSecond(iat);
        Instant whenInstant = OffsetDateTime.parse(when).toInstant();

        assertThat(Math.abs(iatInstant.getEpochSecond() - whenInstant.getEpochSecond()))
                .isLessThanOrEqualTo(1L);
    }

    @Então("deve ser lançada uma exceção com mensagem contendo {string}")
    public void verificaExcecao(String mensagemEsperada) {
        assertThat(excecaoCapturada)
                .isNotNull()
                .hasMessageContaining(mensagemEsperada);
    }

    @Então("o resultado da validação deve conter {string}")
    public void resultadoDaValidacaoDeveConter(String textoEsperado) {
        assertThat(resultadoValidacao).contains(textoEsperado);
    }

    @Dado("que o arquivo Bundle não existe")
    public void arquivoBundleNaoExiste() {
        bundleFile = new File("caminho_ficticio_bundle_inexistente.json");
    }

    @Dado("que existe um arquivo Bundle com JSON inválido")
    public void existeArquivoBundleComJsonInvalido() throws Exception {
        bundleFile = File.createTempFile("bundle_invalid_json", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("{ resourceType: Bundle, incompleto...");
        }
    }

    @Dado("que existe um arquivo Bundle sem a propriedade entry")
    public void existeArquivoBundleSemPropertyEntry() throws Exception {
        bundleFile = File.createTempFile("bundle_no_entry_prop", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("""
                {
                  "resourceType": "Bundle",
                  "type": "collection"
                }
                """);
        }
    }

    @Dado("que existe um arquivo Bundle com a propriedade entry como objeto")
    public void existeArquivoBundleComEntryComoObjeto() throws Exception {
        bundleFile = File.createTempFile("bundle_entry_obj", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("""
                {
                  "resourceType": "Bundle",
                  "type": "collection",
                  "entry": {
                    "fullUrl": "urn:uuid:3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "resource": { "resourceType": "Patient", "id": "exemplo" }
                  }
                }
                """);
        }
    }

    @Dado("que existe um arquivo Bundle com uma entry sem fullUrl")
    public void existeArquivoBundleComEntrySemFullUrl() throws Exception {
        bundleFile = File.createTempFile("bundle_no_fullurl", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("""
                {
                  "resourceType": "Bundle",
                  "type": "collection",
                  "entry": [
                    {
                      "resource": { "resourceType": "Patient", "id": "exemplo" }
                    }
                  ]
                }
                """);
        }
    }

    @Dado("que o arquivo Provenance não existe")
    public void arquivoProvenanceNaoExiste() {
        provenanceFile = new File("caminho_ficticio_provenance_inexistente.json");
    }

    @Dado("que existe um arquivo Provenance com JSON inválido")
    public void existeArquivoProvenanceComJsonInvalido() throws Exception {
        provenanceFile = File.createTempFile("provenance_invalid_json", ".json");
        provenanceFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(provenanceFile)) {
            fw.write("{ resourceType: Provenance, quebrado...");
        }
    }

    @Dado("que existe um arquivo Provenance com o campo recorded em branco")
    public void existeArquivoProvenanceComRecordedEmBranco() throws Exception {
        provenanceFile = File.createTempFile("provenance_blank_recorded", ".json");
        provenanceFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(provenanceFile)) {
            fw.write("""
                {
                  "resourceType": "Provenance",
                  "recorded": "   "
                }
                """);
        }
    }

    @Dado("que o arquivo Signature não existe")
    public void arquivoSignatureNaoExiste() {
        signatureFile = new File("caminho_ficticio_signature_inexistente.json");
    }

    @Dado("que existe um arquivo Signature com JSON inválido")
    public void existeArquivoSignatureComJsonInvalido() throws Exception {
        signatureFile = File.createTempFile("signature_invalid_json", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write("{ resourceType: Signature, data: ... quebrado");
        }
    }

    @Dado("que existe um arquivo Signature com resourceType {string}")
    public void existeArquivoSignatureComResourceType(String type) throws Exception {
        signatureFile = File.createTempFile("signature_invalid_type", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write("""
                {
                  "resourceType": "%s",
                  "data": "base64string..."
                }
                """.formatted(type));
        }
    }

    @Dado("que existe um arquivo Signature sem o campo data")
    public void existeArquivoSignatureSemCampoData() throws Exception {
        signatureFile = File.createTempFile("signature_no_data", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write("""
                {
                  "resourceType": "Signature"
                }
                """);
        }
    }

    @Dado("que existe um arquivo Signature com campo data numérico")
    public void existeArquivoSignatureComDataNumerico() throws Exception {
        signatureFile = File.createTempFile("signature_numeric_data", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write("""
                {
                  "resourceType": "Signature",
                  "data": 123
                }
                """);
        }
    }
}