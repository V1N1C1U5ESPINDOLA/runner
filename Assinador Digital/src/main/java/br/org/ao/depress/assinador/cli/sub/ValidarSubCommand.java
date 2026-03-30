package br.org.ao.depress.assinador.cli.sub;

import br.org.ao.depress.assinador.core.service.SignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Component
@Command(name = "validar", description = "Valida uma assinatura digital padrão FHIR")
@Slf4j
@RequiredArgsConstructor
public class ValidarSubCommand implements Callable<Integer> {

    private final SignatureService signatureService;

    @Option(names = {"-s", "--signature"}, description = "Caminho do arquivo JSON contendo o recurso Signature", required = true)
    private File signatureFile;

    @Option(names = {"-b", "--bundle"}, description = "Caminho do Bundle original (para conferência de hash)", required = true)
    private File bundleFile;

    @Override
    public Integer call() throws Exception {
        try {
            boolean isValid = signatureService.validarAssinatura(signatureFile, bundleFile);

            if (isValid) {
                log.info("Sucesso: A assinatura é válida e o documento está íntegro.");
                return 0;
            } else {
                log.info("Erro: Assinatura inválida ou documento adulterado.");
                return 1;
            }
        } catch (Exception e) {
            log.error("ERRO NA VALIDAÇÃO: {}", e.getMessage());
            return 1;
        }
    }
}