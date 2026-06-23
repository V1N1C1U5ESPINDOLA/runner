package br.org.ao.depress.assinador.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@AutoConfigureMockMvc
public class HttpSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private File bundleFile;
    private File provenanceFile;
    private File signatureFile;
    private ResultActions resultActions;

    @Dado("que existe um arquivo Bundle válido para HTTP")
    public void bundleValidoParaHttp() throws Exception {
        bundleFile = File.createTempFile("http_bundle", ".json");
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

    @Dado("que existe um arquivo Provenance válido para HTTP")
    public void provenanceValidoParaHttp() throws Exception {
        provenanceFile = File.createTempFile("http_provenance", ".json");
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

    @Dado("que o Bundle para HTTP não existe")
    public void bundleNaoExisteParaHttp() {
        bundleFile = new File("bundle_inexistente_http.json");
    }

    @Dado("que existe um arquivo Signature gerado via HTTP")
    public void signatureGeradoViaHttp() throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("bundlePath", bundleFile.getAbsolutePath());
            put("provenancePath", provenanceFile.getAbsolutePath());
            put("pin", "1234");
        }});

        String responseJson = mockMvc.perform(post("/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        signatureFile = File.createTempFile("http_signature", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write(responseJson);
        }
    }

    @Dado("que o Signature para HTTP não existe")
    public void signatureNaoExisteParaHttp() {
        signatureFile = new File("signature_inexistente_http.json");
    }

    @Dado("que existe um arquivo Provenance com recorded de {int} minutos atrás para HTTP")
    public void provenanceForaDaJanelaParaHttp(int minutos) throws Exception {
        provenanceFile = File.createTempFile("http_provenance_old", ".json");
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

    @Dado("que existe um arquivo Bundle com UUID inválido para HTTP")
    public void bundleUuidInvalidoParaHttp() throws Exception {
        bundleFile = File.createTempFile("http_bundle_uuid", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("""
                {
                  "resourceType": "Bundle",
                  "type": "collection",
                  "entry": [
                    {
                      "fullUrl": "nao-e-um-uuid-valido",
                      "resource": { "resourceType": "Patient" }
                    }
                  ]
                }
                """);
        }
    }

    @Dado("que existe um arquivo Provenance com JSON inválido para HTTP")
    public void provenanceJsonInvalidoParaHttp() throws Exception {
        provenanceFile = File.createTempFile("http_provenance_invalido", ".json");
        provenanceFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(provenanceFile)) {
            fw.write("{ isso não é json válido }");
        }
    }

    @Dado("que existe um arquivo Signature com resourceType {string} para HTTP")
    public void signatureComResourceTypeParaHttp(String resourceType) throws Exception {
        signatureFile = File.createTempFile("http_signature_rt", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write("""
                {
                  "resourceType": "%s",
                  "data": "dGVzdGU="
                }
                """.formatted(resourceType));
        }
    }

    @Dado("que existe um arquivo Signature sem o campo data para HTTP")
    public void signatureSemCampoDataParaHttp() throws Exception {
        signatureFile = File.createTempFile("http_signature_semdata", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write("""
                {
                  "resourceType": "Signature"
                }
                """);
        }
    }

    @Dado("que existe um arquivo Signature com JSON inválido para HTTP")
    public void signatureJsonInvalidoParaHttp() throws Exception {
        signatureFile = File.createTempFile("http_signature_invalido", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write("{ isso não é json válido }");
        }
    }

    @Dado("que existe um arquivo Bundle com JSON inválido para HTTP")
    public void bundleJsonInvalidoParaHttp() throws Exception {
        bundleFile = File.createTempFile("http_bundle_invalido", ".json");
        bundleFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(bundleFile)) {
            fw.write("{ isso não é json válido }");
        }
    }

    @Quando("o cliente envia POST para {string} com PIN {string}")
    public void clienteEnviaPostSign(String endpoint, String pin) throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("bundlePath", bundleFile.getAbsolutePath());
            put("provenancePath", provenanceFile.getAbsolutePath());
            put("pin", pin);
        }});

        resultActions = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Quando("o cliente envia POST para {string}")
    public void clienteEnviaPostValidate(String endpoint) throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("signaturePath", signatureFile.getAbsolutePath());
            put("bundlePath", bundleFile.getAbsolutePath());
        }});

        resultActions = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Então("a resposta HTTP deve ter status {int}")
    public void respostaHttpDeveTermStatus(int statusEsperado) throws Exception {
        resultActions.andExpect(status().is(statusEsperado));
    }

    @Então("o corpo da resposta deve ter resourceType igual a {string}")
    public void corpoDeveTermResourceType(String esperado) throws Exception {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        assertThat(node.get("resourceType").asString()).isEqualTo(esperado);
    }

    @Então("o corpo da resposta deve ter sigFormat igual a {string}")
    public void corpoDeveTermSigFormat(String esperado) throws Exception {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        assertThat(node.get("sigFormat").asString()).isEqualTo(esperado);
    }

    @Então("o corpo da resposta deve conter o código {string}")
    public void corpoDeveConterCodigo(String codigoEsperado) throws Exception {
        String json = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(json).contains(codigoEsperado);
    }
}