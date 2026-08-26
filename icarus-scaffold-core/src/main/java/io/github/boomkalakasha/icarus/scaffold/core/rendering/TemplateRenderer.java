package io.github.boomkalakasha.icarus.scaffold.core.rendering;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import io.github.boomkalakasha.icarus.scaffold.core.security.ZipSafety;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Renders the fixed, bundled template manifest into a temporary root. */
public final class TemplateRenderer {

    private static final Version FREEMARKER_VERSION = Configuration.VERSION_2_3_34;
    private static final List<String> TEMPLATE_PATHS = List.of(
            "AGENTS.md.ftl",
            "README.md.ftl",
            "README.zh-CN.md.ftl",
            ".gitignore.ftl",
            "Dockerfile.ftl",
            "compose.yaml.ftl",
            "pom.xml.ftl",
            ".github/workflows/ci.yml.ftl",
            ".github/workflows/codeql.yml.ftl",
            ".github/dependabot.yml.ftl",
            ".github/ISSUE_TEMPLATE/bug_report.md.ftl",
            ".github/ISSUE_TEMPLATE/feature_request.md.ftl",
            ".github/PULL_REQUEST_TEMPLATE.md.ftl",
            "SECURITY.md.ftl",
            "SUPPORT.md.ftl",
            "LICENSE.ftl",
            "docker/HealthCheck.java.ftl",
            "domain/pom.xml.ftl",
            "domain/src/main/java/__packagePath__/domain/Greeting.java.ftl",
            "domain/src/test/java/__packagePath__/domain/GreetingTest.java.ftl",
            "application/pom.xml.ftl",
            "application/src/main/java/__packagePath__/application/GreetingUseCase.java.ftl",
            "application/src/main/java/__packagePath__/application/GreetingService.java.ftl",
            "application/src/test/java/__packagePath__/application/GreetingServiceTest.java.ftl",
            "infrastructure/pom.xml.ftl",
            "infrastructure/src/main/java/__packagePath__/infrastructure/InMemoryGreetingRepository.java.ftl",
            "infrastructure/src/test/java/__packagePath__/infrastructure/InMemoryGreetingRepositoryTest.java.ftl",
            "api/pom.xml.ftl",
            "api/src/main/java/__packagePath__/api/GreetingController.java.ftl",
            "boot/pom.xml.ftl",
            "boot/src/main/java/__packagePath__/boot/GeneratedApplication.java.ftl",
            "boot/src/main/resources/application.yml.ftl",
            "boot/src/test/java/__packagePath__/boot/GeneratedApplicationTest.java.ftl");

    private final Configuration configuration;

    public TemplateRenderer() {
        configuration = new Configuration(FREEMARKER_VERSION);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateLoader(new ClassTemplateLoader(TemplateRenderer.class, "/templates"));
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setFallbackOnNullLoopVariable(false);
    }

    /**
     * Render to a temporary root and return a sorted map of relative paths to
     * bytes. The temporary files make path-boundary checks explicit and are
     * removed before this method returns.
     */
    public Map<String, byte[]> render(ScaffoldRequest request) throws IOException {
        Path root = Files.createTempDirectory("icarus-scaffold-");
        try {
            renderTo(root, request);
            Map<String, byte[]> files = new java.util.TreeMap<>();
            try (var paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .sorted()
                        .forEach(path -> {
                            String relative = root.relativize(path).toString().replace('\\', '/');
                            ZipSafety.validateEntryName(relative);
                            try {
                                files.put(relative, Files.readAllBytes(path));
                            } catch (IOException exception) {
                                throw new TemplateRenderingRuntimeException(exception);
                            }
                        });
            } catch (TemplateRenderingRuntimeException exception) {
                throw exception.ioException;
            }
            return files;
        } finally {
            deleteTemporaryRoot(root);
        }
    }

    private void renderTo(Path root, ScaffoldRequest request) throws IOException {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("artifact", request.artifact());
        model.put("group", request.group());
        model.put("packageName", request.packageName());
        model.put("packagePath", request.packageName().replace('.', '/'));
        model.put("port", Integer.toString(request.port()));
        model.put("description", request.description());

        for (String templatePath : TEMPLATE_PATHS) {
            String relative = templatePath.replace("__packagePath__", (String) model.get("packagePath"));
            if (relative.endsWith(".ftl")) {
                relative = relative.substring(0, relative.length() - ".ftl".length());
            }
            Path target = ZipSafety.resolveInside(root, relative);
            Files.createDirectories(target.getParent());
            try (StringWriter rendered = new StringWriter()) {
                Template template = configuration.getTemplate("project/" + templatePath);
                template.process(model, rendered);
                Files.writeString(target, rendered.toString(), StandardCharsets.UTF_8);
            } catch (TemplateException exception) {
                throw new IOException("template rendering failed for " + templatePath, exception);
            }
        }
    }

    private static void deleteTemporaryRoot(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new TemplateRenderingRuntimeException(exception);
                }
            });
        } catch (TemplateRenderingRuntimeException exception) {
            throw exception.ioException;
        }
    }

    private static final class TemplateRenderingRuntimeException extends RuntimeException {
        private final IOException ioException;

        private TemplateRenderingRuntimeException(IOException ioException) {
            super(ioException);
            this.ioException = ioException;
        }
    }
}
