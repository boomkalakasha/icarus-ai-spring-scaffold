package io.github.boomkalakasha.icarus.scaffold.core.model;

/**
 * User-controlled values used to render one generated project.
 *
 * <p>The record intentionally contains no filesystem or process options. A
 * caller receives ZIP bytes and decides how (or whether) to persist them.</p>
 */
public record ScaffoldRequest(
        String artifact,
        String group,
        String packageName,
        int port,
        String description,
        String license,
        String copyrightHolder,
        Integer copyrightYear,
        String templatePack,
        String profile) {

    public static final String SIMPLE_PROFILE = "simple";
    public static final String MODULAR_PROFILE = "modular";

    public ScaffoldRequest(String artifact, String group, String packageName, int port, String description) {
        this(artifact, group, packageName, port, description, null, null, null,
                "default", MODULAR_PROFILE);
    }

    public ScaffoldRequest(String artifact, String group, String packageName, int port, String description,
                           String templatePack) {
        this(artifact, group, packageName, port, description, null, null, null,
                templatePack, MODULAR_PROFILE);
    }

    public ScaffoldRequest(String artifact, String group, String packageName, int port, String description,
                           String license, String copyrightHolder, Integer copyrightYear) {
        this(artifact, group, packageName, port, description,
                license, copyrightHolder, copyrightYear, "default", MODULAR_PROFILE);
    }

    public ScaffoldRequest(String artifact, String group, String packageName, int port, String description,
                           String license, String copyrightHolder, Integer copyrightYear,
                           String templatePack) {
        this(artifact, group, packageName, port, description,
                license, copyrightHolder, copyrightYear, templatePack, MODULAR_PROFILE);
    }

    public static ScaffoldRequest defaults() {
        return new ScaffoldRequest(
                "generated-service",
                "com.example",
                "com.example.generated",
                8080,
                "A generated Spring service",
                null,
                null,
                null,
                "default",
                MODULAR_PROFILE);
    }

    public ScaffoldRequest {
        templatePack = templatePack == null ? "default" : templatePack;
        profile = profile == null ? MODULAR_PROFILE : profile;
    }
}
