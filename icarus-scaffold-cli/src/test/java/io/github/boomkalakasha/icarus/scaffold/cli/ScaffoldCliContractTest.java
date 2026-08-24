package io.github.boomkalakasha.icarus.scaffold.cli;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipInputStream;

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
}
