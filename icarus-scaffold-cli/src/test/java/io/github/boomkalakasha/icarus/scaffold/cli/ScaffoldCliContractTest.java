package io.github.boomkalakasha.icarus.scaffold.cli;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaffoldCliContractTest {

    @Test
    void writesOnlyZipBytesToTheConfiguredOutput() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int exitCode = new CommandLine(new ScaffoldCli(
                new ScaffoldGenerator(), output, new PrintWriter(errors, true)))
                .execute("--artifact", "cli-service", "--group", "com.example",
                        "--package", "com.example.cli", "--port", "8090");

        assertEquals(0, exitCode);
        assertEquals(0, errors.size());
        assertTrue(output.size() > 100);
        Set<String> names = new HashSet<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                names.add(entry.getName());
            }
        }
        assertTrue(names.contains("pom.xml"));
        assertTrue(names.contains("boot/pom.xml"));
        assertFalse(names.contains("LICENSE"));
    }

    @Test
    void mapsAnExplicitLicenseDeclarationIntoTheGeneratedZip() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int exitCode = new CommandLine(new ScaffoldCli(
                new ScaffoldGenerator(), output, new PrintWriter(errors, true)))
                .execute("--artifact", "licensed-service", "--group", "com.example",
                        "--package", "com.example.licensed", "--port", "8090",
                        "--license", "MIT", "--copyright-holder", "Example Authors",
                        "--copyright-year", "2026");

        assertEquals(0, exitCode);
        assertEquals(0, errors.size());
        String license = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if ("LICENSE".equals(entry.getName())) {
                    license = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
        assertTrue(license != null && license.contains("Copyright (c) 2026 Example Authors"));
    }

    @Test
    void rejectsFilesystemAndOverwriteOptions() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new ScaffoldCli(
                new ScaffoldGenerator(), output, new PrintWriter(errors, true)))
                .setErr(new PrintWriter(errors, true));
        int exitCode = commandLine.execute("--outputPath", "out.zip", "--overwrite");

        assertTrue(exitCode != 0);
        assertEquals(0, output.size());
        assertFalse(errors.toString().isBlank());
    }

    @Test
    void writesTheSameBytesToOneCwdFilenameWhenOutputIsRequested() throws Exception {
        Path outputFile = Path.of("cli-contract-output.zip").toAbsolutePath();
        Files.deleteIfExists(outputFile);
        try {
            ByteArrayOutputStream defaultOutput = new ByteArrayOutputStream();
            int defaultExitCode = new CommandLine(new ScaffoldCli(
                    new ScaffoldGenerator(), defaultOutput, new PrintWriter(new ByteArrayOutputStream(), true)))
                    .execute("--artifact", "cli-service", "--group", "com.example",
                            "--package", "com.example.cli", "--port", "8090");

            ByteArrayOutputStream redirectedOutput = new ByteArrayOutputStream();
            ByteArrayOutputStream errors = new ByteArrayOutputStream();
            int outputExitCode = new CommandLine(new ScaffoldCli(
                    new ScaffoldGenerator(), redirectedOutput, new PrintWriter(errors, true)))
                    .execute("--artifact", "cli-service", "--group", "com.example",
                            "--package", "com.example.cli", "--port", "8090",
                            "--output", outputFile.getFileName().toString());

            assertEquals(0, defaultExitCode);
            assertEquals(0, outputExitCode);
            assertEquals(0, redirectedOutput.size());
            assertEquals(0, errors.size());
            assertTrue(Files.isRegularFile(outputFile));
            assertArrayEquals(defaultOutput.toByteArray(), Files.readAllBytes(outputFile));
        } finally {
            Files.deleteIfExists(outputFile);
        }
    }

    @Test
    void rejectsUnsafeOutputNamesAndDoesNotWriteStdout() throws Exception {
        for (String requestedName : new String[]{
                "nested/output.zip", "../escape.zip", "/absolute.zip", "output.tar", "output"}) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream errors = new ByteArrayOutputStream();
            int exitCode = new CommandLine(new ScaffoldCli(
                    new ScaffoldGenerator(), output, new PrintWriter(errors, true)))
                    .execute("--output", requestedName);

            assertTrue(exitCode != 0, requestedName);
            assertEquals(0, output.size(), requestedName);
            assertFalse(errors.toString().isBlank(), requestedName);
        }
    }

    @Test
    void refusesToOverwriteAnExistingOutputFile() throws Exception {
        Path outputFile = Path.of("cli-contract-existing.zip").toAbsolutePath();
        byte[] sentinel = "do not overwrite".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(outputFile, sentinel);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream errors = new ByteArrayOutputStream();
            int exitCode = new CommandLine(new ScaffoldCli(
                    new ScaffoldGenerator(), output, new PrintWriter(errors, true)))
                    .execute("--output", outputFile.getFileName().toString());

            assertTrue(exitCode != 0);
            assertEquals(0, output.size());
            assertArrayEquals(sentinel, Files.readAllBytes(outputFile));
        } finally {
            Files.deleteIfExists(outputFile);
        }
    }
}
