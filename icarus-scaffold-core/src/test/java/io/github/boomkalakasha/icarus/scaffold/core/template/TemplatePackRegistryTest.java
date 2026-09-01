package io.github.boomkalakasha.icarus.scaffold.core.template;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplatePackRegistryTest {

    @Test
    void defaultsContainsOnlyTheBundledDefaultPack() {
        TemplatePackRegistry registry = TemplatePackRegistry.defaults();

        assertEquals(List.of("default"), registry.ids().stream().toList());
        assertEquals("default", registry.require("default").id());
    }

    @Test
    void duplicatePackIdsAreRejected() {
        TemplatePack first = pack("custom");
        TemplatePack second = pack("custom");

        assertThrows(IllegalArgumentException.class,
                () -> new TemplatePackRegistry(List.of(first, second)));
    }

    @Test
    void unknownPackIdsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> TemplatePackRegistry.defaults().require("private"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../escape.ftl", "/absolute.ftl", "C:/escape.ftl", "https://example.test/template.ftl",
            "file:template.ftl", "nested\\escape.ftl", "nested//escape.ftl", "nested/../escape.ftl"
    })
    void unsafeTemplateManifestPathsAreRejected(String path) {
        assertThrows(IllegalArgumentException.class,
                () -> new TemplateDefinition(path, "generated.txt"));
        assertThrows(IllegalArgumentException.class,
                () -> new TemplateDefinition("README.md.ftl", path));
    }

    @Test
    void packTemplateOutputPathsAreUnique() {
        TemplatePack duplicateOutputs = named("custom", List.of(
                new TemplateDefinition("README.md.ftl", "same.txt"),
                new TemplateDefinition("README.zh-CN.md.ftl", "same.txt")));

        assertThrows(IllegalArgumentException.class,
                () -> new TemplatePackRegistry(List.of(duplicateOutputs)));
    }

    @Test
    void trustedClasspathPackCanRenderBundledTemplateWithoutFilesystemInput() throws IOException {
        TemplatePack custom = named("custom", List.of(
                new TemplateDefinition("README.md.ftl", "custom/README.md")));
        ScaffoldGenerator generator = new ScaffoldGenerator(
                new TemplatePackRegistry(List.of(new DefaultTemplatePack(), custom)));

        Map<String, byte[]> files = readZip(generator.generate(new ScaffoldRequest(
                "orders", "com.example", "com.example.orders", 8080, "safe", null, null, null, "custom")));

        assertTrue(files.containsKey("custom/README.md"));
        assertTrue(new String(files.get("custom/README.md"), StandardCharsets.UTF_8).contains("orders"));
    }

    @Test
    void serviceLoaderPackCanRenderItsOwnClasspathTemplate() throws IOException {
        TemplatePackRegistry registry = TemplatePackRegistry.fromClasspath();
        assertTrue(registry.contains("classpath-test"));
        ScaffoldGenerator generator = new ScaffoldGenerator(registry);

        Map<String, byte[]> files = readZip(generator.generate(new ScaffoldRequest(
                "orders", "com.example", "com.example.orders", 8080, "safe",
                null, null, null, "classpath-test")));

        assertEquals("orders\n", new String(files.get("classpath-pack.txt"), java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    void generatedOutputIsDeterministicForTheSameDefaultRequest() {
        ScaffoldRequest request = ScaffoldRequest.defaults();

        assertArrayEquals(new ScaffoldGenerator().generate(request), new ScaffoldGenerator().generate(request));
    }

    private static TemplatePack pack(String id) {
        return named(id, List.of(new TemplateDefinition("README.md.ftl", id + ".md")));
    }

    private static TemplatePack named(String id, List<TemplateDefinition> templates) {
        return new TemplatePack() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public List<TemplateDefinition> templates() {
                return templates;
            }
        };
    }

    private static Map<String, byte[]> readZip(byte[] zip) throws IOException {
        java.util.LinkedHashMap<String, byte[]> files = new java.util.LinkedHashMap<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (var entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                files.put(entry.getName(), input.readAllBytes());
            }
        }
        return files;
    }
}
