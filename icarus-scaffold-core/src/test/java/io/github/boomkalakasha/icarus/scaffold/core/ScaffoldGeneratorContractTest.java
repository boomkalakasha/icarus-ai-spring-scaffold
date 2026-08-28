package io.github.boomkalakasha.icarus.scaffold.core;

import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        assertTrue(names.contains("boot/src/main/resources/application.yml"));
        assertTrue(names.contains("AGENTS.md"));
        assertTrue(names.contains("README.md"));
        assertTrue(names.contains("README.zh-CN.md"));
        assertFalse(names.contains("LICENSE"));
        assertTrue(names.contains("SUPPORT.md"));
        assertTrue(names.contains("Dockerfile"));
        assertTrue(names.contains("compose.yaml"));
        assertTrue(names.contains("docker/HealthCheck.java"));
        assertTrue(names.stream().anyMatch(name -> name.startsWith(".github/workflows/")));
        assertTrue(names.stream().anyMatch(name -> name.contains("com/example/order")));
        assertTrue(allText.contains("order-service"));
        assertTrue(allText.contains("8088"));
        assertTrue(allText.contains("server:\n  port: 8088"));
        assertFalse(allText.contains("10."));
        assertFalse(allText.contains("Spire"));
        assertFalse(allText.contains("password"));
        assertFalse(allText.contains("secret"));
        assertTrue(allText.contains("No project license was declared"));
        assertTrue(allText.contains("/actuator/health"));
    }

    @Test
    void emitsOnlyTheExplicitlySelectedLicenseAndAttribution() throws IOException {
        Map<String, byte[]> apacheFiles = readZip(generator.generate(new ScaffoldRequest(
                "apache-service", "com.example", "com.example.apache", 8088, "Apache sample",
                "Apache-2.0", "Example Labs", 2026)));
        String apache = new String(apacheFiles.get("LICENSE"), StandardCharsets.UTF_8);
        assertTrue(apache.contains("Apache License"));
        assertTrue(apache.contains("Copyright 2026 Example Labs"));

        Map<String, byte[]> mitFiles = readZip(generator.generate(new ScaffoldRequest(
                "mit-service", "com.example", "com.example.mit", 8089, "MIT sample",
                "MIT", "Example Authors", 2025)));
        String mit = new String(mitFiles.get("LICENSE"), StandardCharsets.UTF_8);
        assertTrue(mit.contains("MIT License"));
        assertTrue(mit.contains("Copyright (c) 2025 Example Authors"));
        assertFalse(mit.contains("Icarus AI Spring Scaffold contributors"));
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

    private static Map<String, byte[]> readZip(byte[] zip) throws IOException {
        Map<String, byte[]> files = new HashMap<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                files.put(entry.getName(), input.readAllBytes());
            }
        }
        return files;
    }
}
