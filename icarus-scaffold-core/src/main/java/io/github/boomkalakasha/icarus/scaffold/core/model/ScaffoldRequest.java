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
        String description) {

    public static ScaffoldRequest defaults() {
        return new ScaffoldRequest(
                "generated-service",
                "com.example",
                "com.example.generated",
                8080,
                "A generated Spring service");
    }
}
