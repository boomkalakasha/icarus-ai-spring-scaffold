package io.github.boomkalakasha.icarus.scaffold.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SafeOutputFileTest {

    @Test
    void resolvesOnlyOneNewZipFilename(@TempDir Path workingDirectory) {
        assertEquals(workingDirectory.resolve("service.zip"),
                SafeOutputFile.resolve(workingDirectory, "service.zip"));
    }

    @Test
    void rejectsPathSyntaxAndNonZipNames(@TempDir Path workingDirectory) {
        for (String name : new String[]{
                "nested/service.zip", "..\\escape.zip", "../escape.zip", "/tmp/service.zip",
                "C:\\service.zip", "service.tar", ".zip", "service"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> SafeOutputFile.resolve(workingDirectory, name), name);
        }
    }

    @Test
    void rejectsExistingTargetBeforeTheCreateNewWrite(@TempDir Path workingDirectory) throws IOException {
        Path target = workingDirectory.resolve("service.zip");
        Files.writeString(target, "sentinel");

        assertThrows(IllegalArgumentException.class,
                () -> SafeOutputFile.resolve(workingDirectory, "service.zip"));
        assertEquals("sentinel", Files.readString(target));
    }

    @Test
    void writeNewRetainsCreateNewRaceSafety(@TempDir Path workingDirectory) throws IOException {
        Path target = workingDirectory.resolve("service.zip");
        byte[] bytes = new byte[]{'P', 'K', 3, 4};

        SafeOutputFile.writeNew(target, bytes);
        assertArrayEquals(bytes, Files.readAllBytes(target));
        assertThrows(FileAlreadyExistsException.class,
                () -> SafeOutputFile.writeNew(target, new byte[]{0}));
        assertArrayEquals(bytes, Files.readAllBytes(target));
    }
}
