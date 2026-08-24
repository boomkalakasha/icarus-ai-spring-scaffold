package io.github.boomkalakasha.icarus.scaffold.core.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZipSafetyTest {

    @Test
    void acceptsNormalRelativeEntryNames() {
        assertDoesNotThrow(() -> ZipSafety.validateEntryName("src/main/java/App.java"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../escape.txt", "/absolute.txt", "C:\\absolute.txt", "a/../../escape.txt", "a\\..\\escape.txt", ""})
    void rejectsAbsoluteAndTraversalEntries(String entryName) {
        assertThrows(IllegalArgumentException.class, () -> ZipSafety.validateEntryName(entryName));
    }
}
