package br.org.ao.depress.assinador.cli.sub;

import br.org.ao.depress.assinador.core.exception.AssinadorException;
import br.org.ao.depress.assinador.core.model.factory.OperationOutcomeFactory;
import br.org.ao.depress.assinador.core.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.concurrent.Callable;

@Component
@Command(name = "validar", description = "Valida uma assinatura digital padrão FHIR")
@RequiredArgsConstructor
public class ValidarSubCommand implements Callable<Integer> {

    private final SignatureService signatureService;
    private final ObjectMapper objectMapper;

    @Option(names = {"-s", "--signature"}, description = "Caminho do arquivo JSON contendo o recurso Signature", required = true)
    private File signatureFile;

    @Option(names = {"-b", "--bundle"}, description = "Caminho do Bundle original (para conferência de hash)", required = true)
    private File bundleFile;

    @Override
    public Integer call() {
        try {
            String resultado = signatureService.validarAssinatura(signatureFile, bundleFile);
            System.out.println(resultado);
            return 0;
        } catch (AssinadorException e) {
            try {
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(OperationOutcomeFactory.fromException(e)));
            } catch (Exception ex) {
                System.err.println("ERRO: " + e.getDiagnostics());
            }
            return 1;
        } catch (Exception e) {
            System.err.println("ERRO INESPERADO: " + e.getMessage());
            return 1;
        }
    }
}