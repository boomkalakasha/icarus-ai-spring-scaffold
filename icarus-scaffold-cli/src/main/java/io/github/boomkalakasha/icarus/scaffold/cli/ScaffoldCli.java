package io.github.boomkalakasha.icarus.scaffold.cli;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * ZIP-only CLI. The generated archive is written to stdout so shell redirection
 * remains the caller's explicit persistence decision.
 */
@Command(name = "icarus-scaffold",
        mixinStandardHelpOptions = true,
        description = "Generate a Spring Boot project ZIP on stdout or one new cwd ZIP file.")
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

    @Option(names = "--license", description = "Optional generated-project license: Apache-2.0 or MIT")
    private String license;

    @Option(names = "--copyright-holder", description = "Generated-project copyright holder; requires --license and --copyright-year")
    private String copyrightHolder;

    @Option(names = "--copyright-year", description = "Generated-project copyright year; requires --license and --copyright-holder")
    private Integer copyrightYear;

    @Option(names = "--output", paramLabel = "<filename.zip>",
            description = "Write to one new .zip filename directly under the current working directory")
    private String outputFileName;

    private final ScaffoldGenerator generator;
    private final OutputStream output;
    private final PrintWriter errors;
    private final Path workingDirectory;

    public ScaffoldCli() {
        this(new ScaffoldGenerator(), System.out, new PrintWriter(System.err, true),
                Path.of("").toAbsolutePath().normalize());
    }

    public ScaffoldCli(ScaffoldGenerator generator, OutputStream output, PrintWriter errors) {
        this(generator, output, errors, Path.of("").toAbsolutePath().normalize());
    }

    ScaffoldCli(ScaffoldGenerator generator, OutputStream output, PrintWriter errors,
                Path workingDirectory) {
        this.generator = generator;
        this.output = output;
        this.errors = errors;
        this.workingDirectory = Objects.requireNonNull(workingDirectory, "workingDirectory")
                .toAbsolutePath().normalize();
    }

    @Override
    public Integer call() {
        final Path requestedOutput;
        if (outputFileName == null) {
            requestedOutput = null;
        } else {
            try {
                requestedOutput = SafeOutputFile.resolve(workingDirectory, outputFileName);
            } catch (IllegalArgumentException exception) {
                errors.println("Invalid output filename: " + exception.getMessage());
                errors.flush();
                return 2;
            }
        }

        try {
            byte[] zip = generator.generate(new ScaffoldRequest(
                    artifact, group, packageName, port, description,
                    license, copyrightHolder, copyrightYear));
            if (requestedOutput == null) {
                output.write(zip);
                output.flush();
            } else {
                SafeOutputFile.writeNew(requestedOutput, zip);
            }
            return 0;
        } catch (IllegalArgumentException exception) {
            errors.println("Invalid scaffold request: " + exception.getMessage());
            errors.flush();
            return 2;
        } catch (IOException exception) {
            errors.println("Could not write generated ZIP: " + exception.getMessage());
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
