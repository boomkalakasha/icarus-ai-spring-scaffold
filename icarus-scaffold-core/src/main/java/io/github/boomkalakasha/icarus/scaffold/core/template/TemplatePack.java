package io.github.boomkalakasha.icarus.scaffold.core.template;

import java.util.List;

/**
 * A trusted-classpath collection of FreeMarker templates.
 *
 * <p>Implementations are supplied by the application classpath (or directly
 * by a trusted caller). This contract intentionally exposes logical resource
 * names only; it has no filesystem, URL or process-execution input.</p>
 */
public interface TemplatePack {

    /** Stable identifier used by a {@code ScaffoldRequest}. */
    String id();

    /** Logical classpath templates and their generated relative paths. */
    List<TemplateDefinition> templates();

    /**
     * Returns the manifest for one validated architecture profile. Existing
     * packs remain profile-neutral unless they opt into profile-specific
     * templates.
     */
    default List<TemplateDefinition> templatesFor(String profile) {
        return templates();
    }
}
