package io.github.boomkalakasha.icarus.scaffold.core.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Immutable registry for trusted template-pack implementations.
 *
 * <p>The default registry is deliberately just the bundled pack. A trusted
 * application may create a registry with additional classpath providers; no
 * request can add a provider or supply a resource directory.</p>
 */
public final class TemplatePackRegistry {

    private static final Pattern PACK_ID = Pattern.compile("[a-z][a-z0-9-]{0,63}");

    private final Map<String, TemplatePack> packs;

    public TemplatePackRegistry(Collection<? extends TemplatePack> templatePacks) {
        if (templatePacks == null) {
            throw new IllegalArgumentException("template packs must not be null");
        }
        Map<String, TemplatePack> registered = new LinkedHashMap<>();
        for (TemplatePack pack : templatePacks) {
            if (pack == null) {
                throw new IllegalArgumentException("template pack must not be null");
            }
            String id = pack.id();
            validateId(id);
            if (registered.containsKey(id)) {
                throw new IllegalArgumentException("duplicate template pack id: " + id);
            }
            List<TemplateDefinition> definitions = pack.templates();
            if (definitions == null) {
                throw new IllegalArgumentException("template pack templates must not be null: " + id);
            }
            List<TemplateDefinition> snapshot = snapshotDefinitions(id, definitions);
            List<TemplateDefinition> simpleSnapshot = snapshotDefinitions(
                    id, pack.templatesFor("simple"));
            List<TemplateDefinition> modularSnapshot = snapshotDefinitions(
                    id, pack.templatesFor("modular"));
            registered.put(id, new RegisteredTemplatePack(
                    id, snapshot, simpleSnapshot, modularSnapshot));
        }
        if (registered.isEmpty()) {
            throw new IllegalArgumentException("at least one template pack is required");
        }
        packs = Collections.unmodifiableMap(new LinkedHashMap<>(registered));
    }

    /** Returns the stock registry; it intentionally contains only {@code default}. */
    public static TemplatePackRegistry defaults() {
        return new TemplatePackRegistry(List.of(new DefaultTemplatePack()));
    }

    /**
     * Returns the bundled pack plus implementations advertised by the trusted
     * process classpath through Java's {@link ServiceLoader}.
     */
    public static TemplatePackRegistry fromClasspath() {
        List<TemplatePack> providers = new ArrayList<>();
        providers.add(new DefaultTemplatePack());
        ServiceLoader.load(TemplatePack.class).forEach(providers::add);
        return new TemplatePackRegistry(providers);
    }

    /** Alias emphasizing that providers are loaded only from the classpath. */
    public static TemplatePackRegistry classpath() {
        return fromClasspath();
    }

    public Set<String> ids() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(packs.keySet()));
    }

    public List<TemplatePack> packs() {
        return List.copyOf(packs.values());
    }

    public boolean contains(String id) {
        return id != null && packs.containsKey(id);
    }

    public TemplatePack require(String id) {
        TemplatePack pack = id == null ? null : packs.get(id);
        if (pack == null) {
            throw new IllegalArgumentException("unknown template pack: " + id);
        }
        return pack;
    }

    /** Map-style alias for callers that prefer lookup terminology. */
    public TemplatePack get(String id) {
        return require(id);
    }

    private static void validateId(String id) {
        if (id == null || !PACK_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("template pack id must match [a-z][a-z0-9-]{0,63}");
        }
    }

    private static String renderedOutputPath(String path) {
        return path.endsWith(".ftl") ? path.substring(0, path.length() - 4) : path;
    }

    private static List<TemplateDefinition> snapshotDefinitions(
            String packId, List<TemplateDefinition> definitions) {
        if (definitions == null) {
            throw new IllegalArgumentException("template pack templates must not be null: " + packId);
        }
        Set<String> outputPaths = new LinkedHashSet<>();
        List<TemplateDefinition> snapshot = new ArrayList<>(definitions.size());
        for (TemplateDefinition definition : definitions) {
            if (definition == null) {
                throw new IllegalArgumentException("template definition must not be null: " + packId);
            }
            String outputPath = renderedOutputPath(definition.outputPath());
            if (!outputPaths.add(outputPath)) {
                throw new IllegalArgumentException(
                        "duplicate template output path in pack " + packId + ": " + outputPath);
            }
            snapshot.add(definition);
        }
        return List.copyOf(snapshot);
    }

    private record RegisteredTemplatePack(
            String id,
            List<TemplateDefinition> templates,
            List<TemplateDefinition> simpleTemplates,
            List<TemplateDefinition> modularTemplates)
            implements TemplatePack {

        @Override
        public List<TemplateDefinition> templatesFor(String profile) {
            return switch (profile) {
                case "simple" -> simpleTemplates;
                case "modular" -> modularTemplates;
                default -> templates;
            };
        }
    }
}
