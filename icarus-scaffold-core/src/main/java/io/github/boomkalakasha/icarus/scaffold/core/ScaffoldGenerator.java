package io.github.boomkalakasha.icarus.scaffold.core;

import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import io.github.boomkalakasha.icarus.scaffold.core.rendering.TemplateRenderer;
import io.github.boomkalakasha.icarus.scaffold.core.security.ZipSafety;
import io.github.boomkalakasha.icarus.scaffold.core.template.TemplatePack;
import io.github.boomkalakasha.icarus.scaffold.core.template.TemplatePackRegistry;
import io.github.boomkalakasha.icarus.scaffold.core.validation.ScaffoldRequestValidator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Pure in-memory project generator. It never accepts or writes a caller path. */
public final class ScaffoldGenerator {

    private final ScaffoldRequestValidator validator;
    private final TemplateRenderer renderer;
    private final TemplatePackRegistry templatePacks;

    public ScaffoldGenerator() {
        this(new ScaffoldRequestValidator(), new TemplateRenderer(), TemplatePackRegistry.defaults());
    }

    ScaffoldGenerator(ScaffoldRequestValidator validator, TemplateRenderer renderer) {
        this(validator, renderer, TemplatePackRegistry.defaults());
    }

    public ScaffoldGenerator(TemplatePackRegistry templatePacks) {
        this(new ScaffoldRequestValidator(), new TemplateRenderer(), templatePacks);
    }

    public ScaffoldGenerator(ScaffoldRequestValidator validator, TemplateRenderer renderer,
                             TemplatePackRegistry templatePacks) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.templatePacks = Objects.requireNonNull(templatePacks, "templatePacks");
    }

    public byte[] generate(ScaffoldRequest request) {
        validator.validate(request);
        TemplatePack templatePack = templatePacks.require(request.templatePack());
        try {
            Map<String, byte[]> files = renderer.render(request, templatePack);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
                for (Map.Entry<String, byte[]> file : files.entrySet()) {
                    ZipSafety.validateEntryName(file.getKey());
                    ZipEntry entry = new ZipEntry(file.getKey());
                    entry.setTime(0L);
                    zip.putNextEntry(entry);
                    zip.write(file.getValue());
                    zip.closeEntry();
                }
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("could not generate scaffold ZIP", exception);
        }
    }

    public byte[] generateZip(ScaffoldRequest request) {
        return generate(request);
    }

    public boolean supportsTemplatePack(String id) {
        return templatePacks.contains(id);
    }

    public Set<String> templatePackIds() {
        return templatePacks.ids();
    }
}
