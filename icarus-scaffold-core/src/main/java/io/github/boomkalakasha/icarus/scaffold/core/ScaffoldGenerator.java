package io.github.boomkalakasha.icarus.scaffold.core;

import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import io.github.boomkalakasha.icarus.scaffold.core.rendering.TemplateRenderer;
import io.github.boomkalakasha.icarus.scaffold.core.security.ZipSafety;
import io.github.boomkalakasha.icarus.scaffold.core.validation.ScaffoldRequestValidator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Pure in-memory project generator. It never accepts or writes a caller path. */
public final class ScaffoldGenerator {

    private final ScaffoldRequestValidator validator;
    private final TemplateRenderer renderer;

    public ScaffoldGenerator() {
        this(new ScaffoldRequestValidator(), new TemplateRenderer());
    }

    ScaffoldGenerator(ScaffoldRequestValidator validator, TemplateRenderer renderer) {
        this.validator = validator;
        this.renderer = renderer;
    }

    public byte[] generate(ScaffoldRequest request) {
        validator.validate(request);
        try {
            Map<String, byte[]> files = renderer.render(request);
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
}
