package io.github.boomkalakasha.icarus.scaffold.core;

import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaffoldGeneratorContractTest {

    private final ScaffoldGenerator generator = new ScaffoldGenerator();

    @Test
    void generatesACompilableMultiModuleProjectContract() throws IOException {
        byte[] zip = generator.generate(new ScaffoldRequest(
                "order-service", "com.example", "com.example.order", 8088, "A safe service"));
        List<String> names = new ArrayList<>();
        String allText = readZip(zip, names);

        assertTrue(names.contains("pom.xml"));
        assertTrue(names.contains("domain/pom.xml"));
        assertTrue(names.contains("application/pom.xml"));
        assertTrue(names.contains("infrastructure/pom.xml"));
        assertTrue(names.contains("api/pom.xml"));
        assertTrue(names.contains("boot/pom.xml"));
        assertTrue(names.contains("AGENTS.md"));
        assertTrue(names.contains("README.md"));
        assertTrue(names.contains("README.zh-CN.md"));
        assertTrue(names.contains("LICENSE"));
        assertTrue(names.contains("SUPPORT.md"));
        assertTrue(names.contains("Dockerfile"));
        assertTrue(names.contains("compose.yaml"));
        assertTrue(names.contains("docker/HealthCheck.java"));
        assertTrue(names.stream().anyMatch(name -> name.startsWith(".github/workflows/")));
        assertTrue(names.stream().anyMatch(name -> name.contains("com/example/order")));
        assertTrue(allText.contains("order-service"));
        assertTrue(allText.contains("8088"));
        assertFalse(allText.contains("10."));
        assertFalse(allText.contains("Spire"));
        assertFalse(allText.contains("password"));
        assertFalse(allText.contains("secret"));
        assertTrue(allText.contains("Apache License"));
        assertTrue(allText.contains("/actuator/health"));
    }

    private static String readZip(byte[] zip, List<String> names) throws IOException {
        StringBuilder content = new StringBuilder();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                names.add(entry.getName());
                content.append(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return content.toString();
    }
}
