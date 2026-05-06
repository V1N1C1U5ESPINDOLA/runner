package br.org.ao.depress.assinador.cli.sub;

import br.org.ao.depress.assinador.core.exception.AssinadorException;
import br.org.ao.depress.assinador.core.model.factory.OperationOutcomeFactory;
import br.org.ao.depress.assinador.core.service.SignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.*;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.concurrent.Callable;

@Component
@Command(name = "assinar", description = "Gera uma assinatura simulada padrão FHIR")
@Slf4j
@RequiredArgsConstructor
public class AssinarSubCommand implements Callable<Integer> {

    private final SignatureService signatureService;
    private final ObjectMapper objectMapper;

    @Option(names = {"-b", "--bundle"}, description = "Caminho do arquivo JSON Bundle", required = true)
    private File bundleFile;

    @Option(names = {"-v", "--provenance"}, description = "Caminho do arquivo JSON Provenance", required = true)
    private File provenanceFile;

    @Option(names = {"-p", "--pin"}, description = "PIN do dispositivo PKCS#11", required = true)
    private String pin;

    @Override
    public Integer call() {
        try {
            String resultado = signatureService.executarAssinatura(bundleFile, provenanceFile, pin);
            log.info("--- ASSINATURA GERADA COM SUCESSO ---");
            System.out.println(resultado);
            return 0;
        } catch (AssinadorException e) {
            try {
                String outcome = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(OperationOutcomeFactory.fromException(e));
                System.err.println("ERRO NA OPERAÇÃO:\n" + outcome);
            } catch (Exception ex) {
                System.err.println("ERRO NA OPERAÇÃO: " + e.getDiagnostics());
            }
            return 1;
        } catch (Exception e) {
            System.err.println("ERRO INESPERADO: " + e.getMessage());
            return 1;
        }
    }
}
