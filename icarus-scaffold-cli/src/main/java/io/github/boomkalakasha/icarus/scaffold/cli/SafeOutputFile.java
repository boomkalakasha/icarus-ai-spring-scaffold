package io.github.boomkalakasha.icarus.scaffold.cli;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/** Resolves the deliberately narrow CLI output-file contract. */
final class SafeOutputFile {

    private SafeOutputFile() {
    }

    /**
     * Resolve one new ZIP filename directly below {@code workingDirectory}.
     *
     * <p>The existence check improves the diagnostic for the common case. The
     * caller must still open the returned path with {@code CREATE_NEW}; a
     * check followed by a normal write would be race-prone.</p>
     */
    static Path resolve(Path workingDirectory, String requestedName) {
        Objects.requireNonNull(workingDirectory, "workingDirectory");
        if (requestedName == null || requestedName.isBlank()) {
            throw new IllegalArgumentException("output must be one *.zip filename");
        }
        if (requestedName.indexOf('\0') >= 0
                || requestedName.indexOf('/') >= 0
                || requestedName.indexOf('\\') >= 0
                || requestedName.matches("[A-Za-z]:.*")) {
            throw new IllegalArgumentException(
                    "output must be one *.zip filename directly under the working directory");
        }

        final Path filename;
        try {
            filename = Path.of(requestedName);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("output must be one valid *.zip filename", exception);
        }
        if (filename.isAbsolute()
                || filename.getNameCount() != 1
                || !filename.getFileName().toString().equals(requestedName)) {
            throw new IllegalArgumentException(
                    "output must be one *.zip filename directly under the working directory");
        }

        String name = filename.getFileName().toString();
        if (name.length() <= ".zip".length()
                || !name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new IllegalArgumentException("output filename must end with .zip");
        }

        Path root = workingDirectory.toAbsolutePath().normalize();
        Path candidate = root.resolve(filename).normalize();
        if (!root.equals(candidate.getParent())) {
            throw new IllegalArgumentException(
                    "output must remain directly under the working directory");
        }
        if (Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("output file already exists: " + name);
        }
        return candidate;
    }

    /**
     * Write bytes without following a validation-to-write race into an
     * overwrite. This method is package-private for the CLI contract tests.
     */
    static void writeNew(Path outputFile, byte[] bytes) throws IOException {
        Objects.requireNonNull(outputFile, "outputFile");
        Objects.requireNonNull(bytes, "bytes");
        try (var stream = Files.newOutputStream(outputFile,
                java.nio.file.StandardOpenOption.CREATE_NEW,
                java.nio.file.StandardOpenOption.WRITE)) {
            stream.write(bytes);
        } catch (FileAlreadyExistsException exception) {
            throw exception;
        }
    }
}
