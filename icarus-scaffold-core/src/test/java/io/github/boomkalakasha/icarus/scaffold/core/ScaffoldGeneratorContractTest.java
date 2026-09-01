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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScaffoldGeneratorContractTest {

    private final ScaffoldGenerator generator = new ScaffoldGenerator();

    @Test
    void omittedTemplatePackKeepsTheDefaultBytesCompatibleWithExplicitDefault() {
        ScaffoldRequest legacy = new ScaffoldRequest(
                "order-service", "com.example", "com.example.order", 8088, "A safe service");
        ScaffoldRequest explicit = new ScaffoldRequest(
                "order-service", "com.example", "com.example.order", 8088, "A safe service",
                null, null, null, "default", "modular");

        assertArrayEquals(generator.generate(legacy), generator.generate(explicit));
        assertEquals("default", legacy.templatePack());
        assertEquals("modular", legacy.profile());
    }

    @Test
    void simpleProfileGeneratesOneModuleWithTheExistingGreetingSlice() throws IOException {
        Map<String, byte[]> files = readZip(generator.generate(new ScaffoldRequest(
                "order-service", "com.example", "com.example.order", 8088, "A safe service",
                null, null, null, "default", "simple")));

        assertTrue(files.containsKey("pom.xml"));
        assertTrue(files.containsKey("src/main/java/com/example/order/domain/Greeting.java"));
        assertTrue(files.containsKey("src/main/java/com/example/order/application/GreetingService.java"));
        assertTrue(files.containsKey("src/main/java/com/example/order/infrastructure/InMemoryGreetingRepository.java"));
        assertTrue(files.containsKey("src/main/java/com/example/order/api/GreetingController.java"));
        assertTrue(files.containsKey("src/main/java/com/example/order/boot/GeneratedApplication.java"));
        assertTrue(files.containsKey("src/test/java/com/example/order/boot/GeneratedApplicationTest.java"));
        assertFalse(files.containsKey("domain/pom.xml"));
        assertFalse(files.containsKey("application/pom.xml"));
        assertFalse(files.containsKey("boot/pom.xml"));
        assertTrue(new String(files.get("README.md"), StandardCharsets.UTF_8).contains("simple profile"));
    }

    @Test
    void modularProfileKeepsTheExistingFiveModuleShape() throws IOException {
        Map<String, byte[]> files = readZip(generator.generate(new ScaffoldRequest(
                "order-service", "com.example", "com.example.order", 8088, "A safe service",
                null, null, null, "default", "modular")));

        assertTrue(files.containsKey("domain/pom.xml"));
        assertTrue(files.containsKey("application/pom.xml"));
        assertTrue(files.containsKey("infrastructure/pom.xml"));
        assertTrue(files.containsKey("api/pom.xml"));
        assertTrue(files.containsKey("boot/pom.xml"));
        assertFalse(files.containsKey("src/main/java/com/example/order/domain/Greeting.java"));
    }

    @Test
    void generatedRootGuideExplainsWhenModuleLevelAiGuidanceIsNeeded() throws IOException {
        for (String profile : List.of("simple", "modular")) {
            Map<String, byte[]> files = readZip(generator.generate(new ScaffoldRequest(
                    "order-service", "com.example", "com.example.order", 8088, "A safe service",
                    null, null, null, "default", profile)));

            String guide = new String(files.get("AGENTS.md"), StandardCharsets.UTF_8);
            assertTrue(guide.contains("AI guidance coverage"));
            assertTrue(guide.contains("Do not add a module guide just because a folder exists"));
            assertTrue(guide.contains("distinct command"));
            assertTrue(guide.contains("data/security boundary"));
        }
    }

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
