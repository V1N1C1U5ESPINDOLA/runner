package br.org.ao.depress.assinador.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.io.File;
import java.io.FileWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegracaoCliSteps {
    private static final File JAR = encontrarJar();

    private File bundleFile;
    private File provenanceFile;
    private File signatureFile;

    private int exitCode;
    private String stdout;

    @Dado("que o jar do assinador está disponível")
    public void jarDisponivel() {
        assertThat(JAR)
                .as("assinador.jar não encontrado em target/. Execute 'mvn package -DskipTests' primeiro.")
                .isNotNull()
                .exists();
    }

    @Dado("que existe um arquivo Bundle válido para integração")
    public void bundleValidoParaIntegracao() throws Exception {
        bundleFile = File.createTempFile("integ_bundle", ".json");
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

    @Dado("que o Bundle para integração não existe")
    public void bundleNaoExisteParaIntegracao() {
        bundleFile = new File("bundle_inexistente_integracao.json");
    }

    @Dado("que existe um arquivo Provenance válido para integração")
    public void provenanceValidoParaIntegracao() throws Exception {
        provenanceFile = File.createTempFile("integ_provenance", ".json");
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

    @Dado("que existe um arquivo Provenance com recorded de {int} minutos atrás para integração")
    public void provenanceForaDaJanelaParaIntegracao(int minutos) throws Exception {
        provenanceFile = File.createTempFile("integ_provenance_old", ".json");
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

    @Dado("que existe um arquivo Signature gerado pelo processo assinar")
    public void signatureGeradoPeloProcesso() throws Exception {
        if (bundleFile == null) bundleValidoParaIntegracao();
        if (provenanceFile == null) provenanceValidoParaIntegracao();

        ProcessResult resultado = executarProcesso(
                "assinar",
                "--bundle", bundleFile.getAbsolutePath(),
                "--provenance", provenanceFile.getAbsolutePath(),
                "--pin", "1234"
        );

        assertThat(resultado.exitCode)
                .as("Falha ao gerar Signature para o cenário de validação")
                .isZero();

        signatureFile = File.createTempFile("integ_signature", ".json");
        signatureFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(signatureFile)) {
            fw.write(resultado.stdout);
        }
    }

    @Dado("que o Signature para integração não existe")
    public void signatureNaoExisteParaIntegracao() {
        signatureFile = new File("signature_inexistente_integracao.json");
    }

    @Quando("o usuário executa o processo assinar com PIN {string}")
    public void executaProcessoAssinar(String pin) throws Exception {
        ProcessResult resultado = executarProcesso(
                "assinar",
                "--bundle", bundleFile.getAbsolutePath(),
                "--provenance", provenanceFile.getAbsolutePath(),
                "--pin", pin
        );
        exitCode = resultado.exitCode;
        stdout = resultado.stdout;
    }

    @Quando("o usuário executa o processo validar")
    public void executaProcessoValidar() throws Exception {
        ProcessResult resultado = executarProcesso(
                "validar",
                "--signature", signatureFile.getAbsolutePath(),
                "--bundle", bundleFile.getAbsolutePath()
        );
        exitCode = resultado.exitCode;
        stdout = resultado.stdout;
    }

    @Então("o processo deve encerrar com código {int}")
    public void processoEncerrouComCodigo(int codigoEsperado) {
        assertThat(exitCode).isEqualTo(codigoEsperado);
    }

    @Então("o stdout deve conter o campo resourceType igual a {string}")
    public void stdoutContemResourceType(String esperado) {
        assertThat(stdout).contains("resourceType");
        assertThat(stdout).contains(esperado);
    }

    @Então("o stdout deve conter o campo sigFormat igual a {string}")
    public void stdoutContemSigFormat(String esperado) {
        assertThat(stdout).contains("sigFormat");
        assertThat(stdout).contains(esperado);
    }

    @Então("o stdout deve conter o campo type.code igual a {string}")
    public void stdoutContemTypeCode(String esperado) {
        assertThat(stdout).contains(esperado);
    }

    @Então("o stdout deve conter o texto {string}")
    public void stdoutContemTexto(String esperado) {
        assertThat(stdout).contains(esperado);
    }

    private ProcessResult executarProcesso(String... args) throws Exception {
        List<String> comando = new ArrayList<>();
        comando.add("java");
        comando.add("-jar");
        if (JAR == null) throw new IllegalStateException("Não foi possível encontrar/gerar jar do assinador.");
        comando.add(JAR.getAbsolutePath());
        comando.addAll(List.of(args));

        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectErrorStream(false);
        Process processo = pb.start();

        String stdoutCapturado = new String(processo.getInputStream().readAllBytes());
        String stderrCapturado = new String(processo.getErrorStream().readAllBytes());
        processo.waitFor();

        System.out.println("=== STDOUT ===\n" + stdoutCapturado);
        System.out.println("=== STDERR ===\n" + stderrCapturado);
        System.out.println("=== EXIT CODE: " + processo.exitValue() + " ===");

        return new ProcessResult(processo.exitValue(), stdoutCapturado.trim());
    }

    private static File encontrarJar() {
        File target = new File("target");
        if (!target.exists()) return null;

        File[] jars = target.listFiles(f ->
                f.getName().endsWith(".jar") && !f.getName().endsWith("-plain.jar")
        );
        if (jars == null || jars.length == 0) return null;
        return jars[0];
    }

    private record ProcessResult(int exitCode, String stdout) {}
}