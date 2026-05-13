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
        SpringApplication app = new SpringApplication(Application.class);

        boolean modoCliLocal = args.length > 0 && (args[0].equals("assinar") || args[0].equals("validar"));

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