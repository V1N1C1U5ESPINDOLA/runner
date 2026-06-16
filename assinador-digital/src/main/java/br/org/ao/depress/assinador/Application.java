package br.org.ao.depress.assinador;

import br.org.ao.depress.assinador.cli.AssinadorCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.spring.PicocliSpringFactory;

import java.util.Arrays;

@SpringBootApplication
public class Application implements CommandLineRunner, ExitCodeGenerator {

    private final AssinadorCommand assinadorCommand;
    private final IFactory factory;
    private int exitCode;

    public Application(AssinadorCommand assinadorCommand, ApplicationContext context) {
        this.assinadorCommand = assinadorCommand;
        this.factory = new PicocliSpringFactory(context);
    }

    public static void main(String[] args) {
        int javaVersion = Runtime.version().feature();
        if (javaVersion < 21) {
            System.err.printf(
                    "ERRO: Java 21 ou superior é necessário. Versão detectada: %d.%n" +
                    "Baixe o JDK em: https://adoptium.net/%n", javaVersion);
            System.exit(1);
        }

        SpringApplication app = new SpringApplication(Application.class);

        boolean modoCliLocal = Arrays.stream(args)
                .anyMatch(arg -> arg.equals("assinar") || arg.equals("validar"));

        if (modoCliLocal) {
            app.setAdditionalProfiles("cli");
            app.setWebApplicationType(WebApplicationType.NONE);
            System.exit(SpringApplication.exit(app.run(args)));
        }

        app.run(args);
    }

    @Override
    public void run(String... args) {
        if (args.length > 0) {
            exitCode = new CommandLine(assinadorCommand, factory).execute(args);
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}