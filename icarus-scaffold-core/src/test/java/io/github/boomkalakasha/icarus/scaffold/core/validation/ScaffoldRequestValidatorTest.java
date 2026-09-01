package io.github.boomkalakasha.icarus.scaffold.core.validation;

import io.github.boomkalakasha.icarus.scaffold.core.exception.InvalidScaffoldRequestException;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScaffoldRequestValidatorTest {

    private final ScaffoldRequestValidator validator = new ScaffoldRequestValidator();

    @Test
    void acceptsAWellFormedRequest() {
        assertDoesNotThrow(() -> validator.validate(new ScaffoldRequest(
                "order-service",
                "com.example",
                "com.example.order",
                8080,
                "A small order service")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"simple", "modular"})
    void acceptsSupportedProfiles(String profile) {
        assertDoesNotThrow(() -> validator.validate(new ScaffoldRequest(
                "orders", "com.example", "com.example.orders", 8080, "safe",
                null, null, null, "default", profile)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Simple", "../escape", "simple/profile"})
    void rejectsUnsupportedProfiles(String profile) {
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest(
                        "orders", "com.example", "com.example.orders", 8080, "safe",
                        null, null, null, "default", profile)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../evil", "../", "Alpha", "has space", "a/b", "a\\b", ""})
    void rejectsUnsafeArtifactNames(String artifact) {
        ScaffoldRequest request = new ScaffoldRequest(artifact, "com.example", "com.example.order", 8080, "safe");
        assertThrows(InvalidScaffoldRequestException.class, () -> validator.validate(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"com..example", "Com.example", "com/example", "com.example.$bad", ""})
    void rejectsUnsafeGroupAndPackageNames(String value) {
        ScaffoldRequest groupRequest = new ScaffoldRequest("orders", value, "com.example.order", 8080, "safe");
        ScaffoldRequest packageRequest = new ScaffoldRequest("orders", "com.example", value, 8080, "safe");
        assertThrows(InvalidScaffoldRequestException.class, () -> validator.validate(groupRequest));
        assertThrows(InvalidScaffoldRequestException.class, () -> validator.validate(packageRequest));
    }

    @Test
    void rejectsPrivilegedAndOutOfRangePorts() {
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest("orders", "com.example", "com.example.order", 80, "safe")));
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest("orders", "com.example", "com.example.order", 65536, "safe")));
    }

    @Test
    void rejectsMarkupAndOverlongDescriptions() {
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest("orders", "com.example", "com.example.order", 8080, "<script>alert(1)</script>")));
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest("orders", "com.example", "com.example.order", 8080, "a".repeat(241))));
    }

    @Test
    void acceptsACompleteSupportedLicenseDeclaration() {
        assertDoesNotThrow(() -> validator.validate(new ScaffoldRequest(
                "orders", "com.example", "com.example.order", 8080, "safe",
                "MIT", "Example Authors", 2026)));
    }

    @Test
    void rejectsPartialAndUnsupportedLicenseDeclarations() {
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest(
                        "orders", "com.example", "com.example.order", 8080, "safe",
                        "MIT", null, 2026)));
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest(
                        "orders", "com.example", "com.example.order", 8080, "safe",
                        "GPL-3.0", "Example Authors", 2026)));
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest(
                        "orders", "com.example", "com.example.order", 8080, "safe",
                        "Apache-2.0", "Example\nAuthors", 2026)));
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest(
                        "orders", "com.example", "com.example.order", 8080, "safe",
                        "Apache-2.0", "<Example Authors>", 2026)));
        assertThrows(InvalidScaffoldRequestException.class,
                () -> validator.validate(new ScaffoldRequest(
                        "orders", "com.example", "com.example.order", 8080, "safe",
                        "Apache-2.0", "Example Authors", 1899)));
    }
}
