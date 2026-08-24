package io.github.boomkalakasha.icarus.scaffold.core.security;

import java.nio.file.Path;
import java.util.Objects;

/** Centralized ZIP entry and temporary-render path checks. */
public final class ZipSafety {

    private ZipSafety() {
    }

    public static void validateEntryName(String entryName) {
        if (entryName == null || entryName.isBlank() || entryName.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("ZIP entry must be a non-empty relative path");
        }
        if (entryName.startsWith("/") || entryName.matches("[A-Za-z]:.*")) {
            throw new IllegalArgumentException("ZIP entry must not be absolute");
        }
        for (String segment : entryName.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("ZIP entry contains an unsafe path segment");
            }
        }
    }

    public static Path resolveInside(Path root, String relativePath) {
        Objects.requireNonNull(root, "root");
        validateEntryName(relativePath);
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path candidate = normalizedRoot.resolve(relativePath).normalize();
        if (!candidate.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("path escapes the temporary render root");
        }
        return candidate;
    }
}
