package io.github.boomkalakasha.icarus.scaffold.core.template;

import io.github.boomkalakasha.icarus.scaffold.core.security.ZipSafety;

/**
 * Maps one trusted classpath FreeMarker resource to a generated ZIP path.
 * Both values are logical relative paths; no path is resolved from caller
 * input or interpreted as a URL.
 */
public record TemplateDefinition(String templatePath, String outputPath) {

    public TemplateDefinition {
        validateLogicalPath("templatePath", templatePath);
        validateLogicalPath("outputPath", outputPath);
    }

    private static void validateLogicalPath(String field, String path) {
        try {
            ZipSafety.validateEntryName(path);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(field + " must be a safe relative logical path", exception);
        }
        if (path.indexOf(':') >= 0) {
            throw new IllegalArgumentException(field + " must not contain a URI or drive prefix");
        }
        for (int offset = 0; offset < path.length(); ) {
            int codePoint = path.codePointAt(offset);
            if (Character.isISOControl(codePoint)) {
                throw new IllegalArgumentException(field + " must not contain control characters");
            }
            offset += Character.charCount(codePoint);
        }
    }
}
