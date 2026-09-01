package io.github.boomkalakasha.icarus.scaffold.core.rendering;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import io.github.boomkalakasha.icarus.scaffold.core.security.ZipSafety;
import io.github.boomkalakasha.icarus.scaffold.core.template.DefaultTemplatePack;
import io.github.boomkalakasha.icarus.scaffold.core.template.TemplateDefinition;
import io.github.boomkalakasha.icarus.scaffold.core.template.TemplatePack;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Renders a trusted template-pack manifest into a temporary root. */
public final class TemplateRenderer {

    private static final Version FREEMARKER_VERSION = Configuration.VERSION_2_3_34;
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
        return render(request, new DefaultTemplatePack());
    }

    public Map<String, byte[]> render(ScaffoldRequest request, TemplatePack templatePack) throws IOException {
        Path root = Files.createTempDirectory("icarus-scaffold-");
        try {
            renderTo(root, request, templatePack);
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

    private void renderTo(Path root, ScaffoldRequest request, TemplatePack templatePack) throws IOException {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("artifact", request.artifact());
        model.put("group", request.group());
        model.put("packageName", request.packageName());
        model.put("packagePath", request.packageName().replace('.', '/'));
        model.put("port", Integer.toString(request.port()));
        model.put("description", request.description());
        model.put("licenseDeclared", request.license() != null);
        model.put("license", request.license());
        model.put("copyrightHolder", request.copyrightHolder());
        model.put("copyrightYear", request.copyrightYear() == null
                ? null
                : Integer.toString(request.copyrightYear()));

        for (TemplateDefinition definition : templatePack.templatesFor(request.profile())) {
            renderTemplate(root, definition, model);
        }
        if (request.license() != null) {
            String licenseTemplate = switch (request.license()) {
                case "Apache-2.0" -> "LICENSE.ftl";
                case "MIT" -> "LICENSE.MIT.ftl";
                default -> throw new IllegalArgumentException("unsupported license: " + request.license());
            };
            renderTemplate(root, new TemplateDefinition(licenseTemplate, "LICENSE"), model);
        }
    }

    private void renderTemplate(Path root, TemplateDefinition definition,
                                Map<String, Object> model) throws IOException {
        String relative = definition.outputPath().replace("__packagePath__", (String) model.get("packagePath"));
        if (relative.endsWith(".ftl")) {
            relative = relative.substring(0, relative.length() - ".ftl".length());
        }
        Path target = ZipSafety.resolveInside(root, relative);
        Files.createDirectories(target.getParent());
        try (StringWriter rendered = new StringWriter()) {
            Template template = configuration.getTemplate("project/" + definition.templatePath());
            template.process(model, rendered);
            Files.writeString(target, rendered.toString(), StandardCharsets.UTF_8);
        } catch (TemplateException exception) {
            throw new IOException("template rendering failed for " + definition.templatePath(), exception);
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
