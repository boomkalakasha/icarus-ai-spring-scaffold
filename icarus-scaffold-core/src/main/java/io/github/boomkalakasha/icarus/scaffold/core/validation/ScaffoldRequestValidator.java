package io.github.boomkalakasha.icarus.scaffold.core.validation;

import io.github.boomkalakasha.icarus.scaffold.core.exception.InvalidScaffoldRequestException;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;

import java.util.Set;
import java.util.regex.Pattern;

/** Validates all values that can reach a template or ZIP entry. */
public final class ScaffoldRequestValidator {

    private static final Pattern ARTIFACT = Pattern.compile("[a-z][a-z0-9-]{0,63}");
    private static final Pattern JAVA_PACKAGE = Pattern.compile("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*");
    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
            "true", "false", "null", "record", "sealed", "permits", "non-sealed", "var", "yield");
    private static final String SAFE_DESCRIPTION_PUNCTUATION = " .,;:_()[]/'\"!?&+-=#@%*";

    public void validate(ScaffoldRequest request) {
        if (request == null) {
            throw new InvalidScaffoldRequestException("request must not be null");
        }
        validateArtifact(request.artifact());
        validatePackage("group", request.group());
        validatePackage("package", request.packageName());
        if (request.port() < 1024 || request.port() > 65535) {
            throw new InvalidScaffoldRequestException("port must be between 1024 and 65535");
        }
        validateDescription(request.description());
    }

    public static void validateRequest(ScaffoldRequest request) {
        new ScaffoldRequestValidator().validate(request);
    }

    private static void validateArtifact(String artifact) {
        if (artifact == null || artifact.length() > 64 || !ARTIFACT.matcher(artifact).matches()) {
            throw new InvalidScaffoldRequestException("artifact must match [a-z][a-z0-9-]{0,63}");
        }
    }

    private static void validatePackage(String field, String value) {
        if (value == null || value.length() > 128 || !JAVA_PACKAGE.matcher(value).matches()) {
            throw new InvalidScaffoldRequestException(field + " must be a lower-case Java package");
        }
        for (String segment : value.split("\\.")) {
            if (JAVA_KEYWORDS.contains(segment)) {
                throw new InvalidScaffoldRequestException(field + " contains a Java keyword");
            }
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank() || description.length() > 240) {
            throw new InvalidScaffoldRequestException("description must contain 1 to 240 characters");
        }
        for (int offset = 0; offset < description.length(); ) {
            int codePoint = description.codePointAt(offset);
            if (Character.isISOControl(codePoint)
                    || (!Character.isLetterOrDigit(codePoint)
                    && SAFE_DESCRIPTION_PUNCTUATION.indexOf(codePoint) < 0)) {
                throw new InvalidScaffoldRequestException("description contains an unsupported character");
            }
            offset += Character.charCount(codePoint);
        }
    }
}
