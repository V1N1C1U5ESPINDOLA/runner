package br.org.ao.depress.assinador.cli;

import br.org.ao.depress.assinador.cli.sub.AssinarSubCommand;
import br.org.ao.depress.assinador.cli.sub.ValidarSubCommand;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

@Component
@Command(
        name = "assinador",
        mixinStandardHelpOptions = true,
        versionProvider = AssinadorCommand.VersionProvider.class,
        description = "Simulador de Assinatura Digital FHIR (SES-GO)",
        subcommands = {
                AssinarSubCommand.class,
                ValidarSubCommand.class
        }
)
public class AssinadorCommand implements Callable<Integer> {

    @Option(
            names = {"--verbose"},
            scope = ScopeType.INHERIT,
            description = "Habilita logs de diagnóstico no stderr"
    )
    private boolean verbose;

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    /**
     * Eleva o nível de log para INFO/DEBUG quando --verbose está ativo.
     * Chamado no início de cada subcomando para aplicar o modo antes da execução.
     */
    public void aplicarModoVerbose() {
        if (!verbose) return;
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);
        context.getLogger("br.org.ao.depress").setLevel(Level.DEBUG);
    }

    static class VersionProvider implements IVersionProvider {
        @Override
        public String[] getVersion() {
            var props = new Properties();
            try (var is = VersionProvider.class.getResourceAsStream("/version.properties")) {
                if (is != null) props.load(is);
            } catch (IOException ignored) {}
            return new String[]{"assinador " + props.getProperty("app.version", "dev")};
        }
    }
}
