package io.github.boomkalakasha.icarus.scaffold.cli;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

/**
 * ZIP-only CLI. The generated archive is written to stdout so shell redirection
 * remains the caller's explicit persistence decision.
 */
@Command(name = "icarus-scaffold",
        mixinStandardHelpOptions = true,
        description = "Generate a Spring Boot project ZIP on stdout.")
public final class ScaffoldCli implements Callable<Integer> {

    @Option(names = "--artifact", defaultValue = "generated-service", description = "Maven artifact name")
    private String artifact;

    @Option(names = "--group", defaultValue = "com.example", description = "Maven group and Java package prefix")
    private String group;

    @Option(names = "--package", defaultValue = "com.example.generated", description = "Java package name")
    private String packageName;

    @Option(names = "--port", defaultValue = "8080", description = "HTTP port from 1024 to 65535")
    private int port;

    @Option(names = "--description", defaultValue = "A generated Spring service", description = "Short project description")
    private String description;

    private final ScaffoldGenerator generator;
    private final OutputStream output;
    private final PrintWriter errors;

    public ScaffoldCli() {
        this(new ScaffoldGenerator(), System.out, new PrintWriter(System.err, true));
    }

    public ScaffoldCli(ScaffoldGenerator generator, OutputStream output, PrintWriter errors) {
        this.generator = generator;
        this.output = output;
        this.errors = errors;
    }

    @Override
    public Integer call() {
        try {
            byte[] zip = generator.generate(new ScaffoldRequest(artifact, group, packageName, port, description));
            output.write(zip);
            output.flush();
            return 0;
        } catch (IllegalArgumentException exception) {
            errors.println("Invalid scaffold request: " + exception.getMessage());
            errors.flush();
            return 2;
        } catch (IOException exception) {
            errors.println("Could not write generated ZIP.");
            errors.flush();
            return 1;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ScaffoldCli()).execute(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
