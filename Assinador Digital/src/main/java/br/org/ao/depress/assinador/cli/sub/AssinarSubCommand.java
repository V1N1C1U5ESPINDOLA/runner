package br.org.ao.depress.assinador.cli.sub;

import br.org.ao.depress.assinador.core.service.SignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.*;

import java.io.File;
import java.util.concurrent.Callable;

@Component
@Command(name = "assinar", description = "Gera uma assinatura simulada padrão FHIR")
@Slf4j
@RequiredArgsConstructor
public class AssinarSubCommand implements Callable<Integer> {

    private final SignatureService signatureService;

    @Option(names = {"-p", "--pin"}, description = "PIN do dispositivo PKCS#11", required = true)
    private String pin;

    @Option(names = {"-b", "--bundle"}, description = "Caminho do arquivo JSON Bundle", required = true)
    private File bundleFile;

    @Option(names = {"-v", "--provenance"}, description = "Caminho do arquivo JSON Provenance", required = true)
    private File provenanceFile;

    @Override
    public Integer call() {
        try {
            String resultado = signatureService.executarAssinatura(bundleFile, provenanceFile, pin);

            log.info("--- ASSINATURA GERADA COM SUCESSO ---");
            log.info(resultado);
            return 0;
        } catch (Exception e) {
            log.error("ERRO NA OPERAÇÃO: {}", e.getMessage());
            return 1;
        }
    }
}
