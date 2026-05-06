package br.org.ao.depress.assinador.cli;

import br.org.ao.depress.assinador.cli.sub.*;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Component
@Command(name = "assinador",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Simulador de Assinatura Digital FHIR (SES-GO)",
        subcommands = {
                AssinarSubCommand.class,
                ValidarSubCommand.class
        })
public class AssinadorCommand implements Callable<Integer> {
    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }
}

