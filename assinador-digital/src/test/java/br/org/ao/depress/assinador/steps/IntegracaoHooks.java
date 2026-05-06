package br.org.ao.depress.assinador.steps;

import io.cucumber.java.BeforeAll;

import java.io.File;
import java.util.Objects;

public class IntegracaoHooks {
    @BeforeAll
    public static void garantirJarCompilado() throws Exception {
        File target = new File("target");
        boolean jarExiste = target.exists() && target.listFiles(f ->
                f.getName().endsWith(".jar") && !f.getName().endsWith("-plain.jar")
        ) != null && Objects.requireNonNull(target.listFiles(f ->
                f.getName().endsWith(".jar") && !f.getName().endsWith("-plain.jar")
        )).length > 0;

        if (jarExiste) return;

        System.out.println("[IntegracaoHooks] jar não encontrado — executando mvn package...");

        String mvnw = System.getProperty("os.name").toLowerCase().contains("win")
                ? "mvnw.cmd"
                : "./mvnw";

        ProcessBuilder pb = new ProcessBuilder(mvnw, "package", "-DskipTests")
                .inheritIO();
        int exitCode = pb.start().waitFor();

        if (exitCode != 0) {
            throw new IllegalStateException(
                    "Falha ao compilar o jar automaticamente (exit code " + exitCode + "). " +
                            "Execute 'mvn package -DskipTests' manualmente antes de rodar os testes."
            );
        }
    }
}