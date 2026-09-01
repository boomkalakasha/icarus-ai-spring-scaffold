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
    private static final Set<String> SUPPORTED_LICENSES = Set.of("Apache-2.0", "MIT");
    private static final Pattern TEMPLATE_PACK = Pattern.compile("[a-z][a-z0-9-]{0,63}");
    private static final Set<String> SUPPORTED_PROFILES = Set.of(
            ScaffoldRequest.SIMPLE_PROFILE, ScaffoldRequest.MODULAR_PROFILE);

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
        validateLicenseDeclaration(request);
        validateTemplatePack(request.templatePack());
        validateProfile(request.profile());
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

    private static void validateLicenseDeclaration(ScaffoldRequest request) {
        boolean anyValue = request.license() != null
                || request.copyrightHolder() != null
                || request.copyrightYear() != null;
        if (!anyValue) {
            return;
        }
        if (request.license() == null
                || request.copyrightHolder() == null
                || request.copyrightHolder().isBlank()
                || request.copyrightYear() == null) {
            throw new InvalidScaffoldRequestException(
                    "license, copyright holder and copyright year must be provided together");
        }
        if (!SUPPORTED_LICENSES.contains(request.license())) {
            throw new InvalidScaffoldRequestException("license must be Apache-2.0 or MIT");
        }
        if (request.copyrightHolder().length() > 160) {
            throw new InvalidScaffoldRequestException("copyright holder must contain 1 to 160 characters");
        }
        for (int offset = 0; offset < request.copyrightHolder().length(); ) {
            int codePoint = request.copyrightHolder().codePointAt(offset);
            if (Character.isISOControl(codePoint)
                    || (!Character.isLetterOrDigit(codePoint)
                    && SAFE_DESCRIPTION_PUNCTUATION.indexOf(codePoint) < 0)) {
                throw new InvalidScaffoldRequestException("copyright holder contains an unsupported character");
            }
            offset += Character.charCount(codePoint);
        }
        if (request.copyrightYear() < 1900 || request.copyrightYear() > 9999) {
            throw new InvalidScaffoldRequestException("copyright year must be between 1900 and 9999");
        }
    }

    private static void validateTemplatePack(String templatePack) {
        if (templatePack == null || !TEMPLATE_PACK.matcher(templatePack).matches()) {
            throw new InvalidScaffoldRequestException(
                    "templatePack must match [a-z][a-z0-9-]{0,63}");
        }
    }

    private static void validateProfile(String profile) {
        if (!SUPPORTED_PROFILES.contains(profile)) {
            throw new InvalidScaffoldRequestException("profile must be simple or modular");
        }
    }
}
