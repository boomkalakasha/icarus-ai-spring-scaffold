package io.github.boomkalakasha.icarus.scaffold.core.template;

import java.util.List;

/** The public, bundled Spring project template pack. */
public final class DefaultTemplatePack implements TemplatePack {

    public static final String ID = "default";

    private static final List<TemplateDefinition> TEMPLATES = List.of(
            definition("AGENTS.md.ftl"),
            definition("README.md.ftl"),
            definition("README.zh-CN.md.ftl"),
            definition(".gitignore.ftl"),
            definition("Dockerfile.ftl"),
            definition("compose.yaml.ftl"),
            definition("pom.xml.ftl"),
            definition(".github/workflows/ci.yml.ftl"),
            definition(".github/workflows/codeql.yml.ftl"),
            definition(".github/dependabot.yml.ftl"),
            definition(".github/ISSUE_TEMPLATE/bug_report.md.ftl"),
            definition(".github/ISSUE_TEMPLATE/feature_request.md.ftl"),
            definition(".github/PULL_REQUEST_TEMPLATE.md.ftl"),
            definition("SECURITY.md.ftl"),
            definition("SUPPORT.md.ftl"),
            definition("docker/HealthCheck.java.ftl"),
            definition("domain/pom.xml.ftl"),
            definition("domain/src/main/java/__packagePath__/domain/Greeting.java.ftl"),
            definition("domain/src/test/java/__packagePath__/domain/GreetingTest.java.ftl"),
            definition("application/pom.xml.ftl"),
            definition("application/src/main/java/__packagePath__/application/GreetingUseCase.java.ftl"),
            definition("application/src/main/java/__packagePath__/application/GreetingService.java.ftl"),
            definition("application/src/test/java/__packagePath__/application/GreetingServiceTest.java.ftl"),
            definition("infrastructure/pom.xml.ftl"),
            definition("infrastructure/src/main/java/__packagePath__/infrastructure/InMemoryGreetingRepository.java.ftl"),
            definition("infrastructure/src/test/java/__packagePath__/infrastructure/InMemoryGreetingRepositoryTest.java.ftl"),
            definition("api/pom.xml.ftl"),
            definition("api/src/main/java/__packagePath__/api/GreetingController.java.ftl"),
            definition("boot/pom.xml.ftl"),
            definition("boot/src/main/java/__packagePath__/boot/GeneratedApplication.java.ftl"),
            definition("boot/src/main/resources/application.yml.ftl"),
            definition("boot/src/test/java/__packagePath__/boot/GeneratedApplicationTest.java.ftl"));

    private static final List<TemplateDefinition> SIMPLE_TEMPLATES = List.of(
            definition("simple/AGENTS.md.ftl", "AGENTS.md"),
            definition("simple/README.md.ftl", "README.md"),
            definition("simple/README.zh-CN.md.ftl", "README.zh-CN.md"),
            definition(".gitignore.ftl"),
            definition("simple/Dockerfile.ftl", "Dockerfile"),
            definition("compose.yaml.ftl"),
            definition("simple/pom.xml.ftl", "pom.xml"),
            definition(".github/workflows/ci.yml.ftl"),
            definition(".github/workflows/codeql.yml.ftl"),
            definition(".github/dependabot.yml.ftl"),
            definition(".github/ISSUE_TEMPLATE/bug_report.md.ftl"),
            definition(".github/ISSUE_TEMPLATE/feature_request.md.ftl"),
            definition(".github/PULL_REQUEST_TEMPLATE.md.ftl"),
            definition("SECURITY.md.ftl"),
            definition("SUPPORT.md.ftl"),
            definition("docker/HealthCheck.java.ftl"),
            definition("domain/src/main/java/__packagePath__/domain/Greeting.java.ftl",
                    "src/main/java/__packagePath__/domain/Greeting.java"),
            definition("domain/src/test/java/__packagePath__/domain/GreetingTest.java.ftl",
                    "src/test/java/__packagePath__/domain/GreetingTest.java"),
            definition("application/src/main/java/__packagePath__/application/GreetingUseCase.java.ftl",
                    "src/main/java/__packagePath__/application/GreetingUseCase.java"),
            definition("application/src/main/java/__packagePath__/application/GreetingService.java.ftl",
                    "src/main/java/__packagePath__/application/GreetingService.java"),
            definition("application/src/test/java/__packagePath__/application/GreetingServiceTest.java.ftl",
                    "src/test/java/__packagePath__/application/GreetingServiceTest.java"),
            definition("infrastructure/src/main/java/__packagePath__/infrastructure/InMemoryGreetingRepository.java.ftl",
                    "src/main/java/__packagePath__/infrastructure/InMemoryGreetingRepository.java"),
            definition("infrastructure/src/test/java/__packagePath__/infrastructure/InMemoryGreetingRepositoryTest.java.ftl",
                    "src/test/java/__packagePath__/infrastructure/InMemoryGreetingRepositoryTest.java"),
            definition("api/src/main/java/__packagePath__/api/GreetingController.java.ftl",
                    "src/main/java/__packagePath__/api/GreetingController.java"),
            definition("boot/src/main/java/__packagePath__/boot/GeneratedApplication.java.ftl",
                    "src/main/java/__packagePath__/boot/GeneratedApplication.java"),
            definition("boot/src/main/resources/application.yml.ftl", "src/main/resources/application.yml"),
            definition("boot/src/test/java/__packagePath__/boot/GeneratedApplicationTest.java.ftl",
                    "src/test/java/__packagePath__/boot/GeneratedApplicationTest.java"));

    @Override
    public String id() {
        return ID;
    }

    @Override
    public List<TemplateDefinition> templates() {
        return TEMPLATES;
    }

    @Override
    public List<TemplateDefinition> templatesFor(String profile) {
        return switch (profile) {
            case "simple" -> SIMPLE_TEMPLATES;
            case "modular" -> TEMPLATES;
            default -> throw new IllegalArgumentException("unsupported profile: " + profile);
        };
    }

    private static TemplateDefinition definition(String path) {
        String output = path.endsWith(".ftl")
                ? path.substring(0, path.length() - ".ftl".length())
                : path;
        return new TemplateDefinition(path, output);
    }

    private static TemplateDefinition definition(String templatePath, String outputPath) {
        return new TemplateDefinition(templatePath, outputPath);
    }
}
