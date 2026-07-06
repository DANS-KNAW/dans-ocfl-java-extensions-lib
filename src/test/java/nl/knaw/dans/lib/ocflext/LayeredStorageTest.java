/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.lib.ocflext;

import io.ocfl.api.DigestAlgorithmRegistry;
import io.ocfl.api.exception.OcflFileAlreadyExistsException;
import io.ocfl.api.exception.OcflNoSuchFileException;
import nl.knaw.dans.layerstore.DirectLayerArchiver;
import nl.knaw.dans.layerstore.LayerManagerImpl;
import nl.knaw.dans.layerstore.ZipArchiveProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LayeredStorageTest extends LayerDatabaseFixture {

    private LayeredStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        var layerManager = new LayerManagerImpl(stagingDir, new ZipArchiveProvider(archiveDir), new DirectLayerArchiver());
        storage = createLayeredStorage(layerManager);
        storage.createDirectories("a/b");
    }

    // --- write ---

    @Test
    public void write_throws_OcflFileAlreadyExistsException_when_file_already_exists() {
        storage.write("a/b/file.txt", "hello".getBytes(StandardCharsets.UTF_8), null);

        assertThatThrownBy(() -> storage.write("a/b/file.txt", "world".getBytes(StandardCharsets.UTF_8), null))
            .isInstanceOf(OcflFileAlreadyExistsException.class);
    }

    @Test
    public void write_succeeds_when_file_does_not_exist() {
        storage.write("a/b/file.txt", "hello".getBytes(StandardCharsets.UTF_8), null);

        assertThat(storage.fileExists("a/b/file.txt")).isTrue();
    }

    // --- read ---

    @Test
    public void read_throws_OcflNoSuchFileException_when_file_does_not_exist() {
        assertThatThrownBy(() -> storage.read("a/b/nonexistent.txt"))
            .isInstanceOf(OcflNoSuchFileException.class);
    }

    @Test
    public void readToString_throws_OcflNoSuchFileException_when_file_does_not_exist() {
        assertThatThrownBy(() -> storage.readToString("a/b/nonexistent.txt"))
            .isInstanceOf(OcflNoSuchFileException.class);
    }

    @Test
    public void read_returns_content_when_file_exists() throws Exception {
        storage.write("a/b/file.txt", "hello".getBytes(StandardCharsets.UTF_8), null);

        try (var is = storage.read("a/b/file.txt")) {
            assertThat(is.readAllBytes()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
        }
    }

    // --- readLazy ---

    @Test
    public void readLazy_returns_content_via_LayeredStorageOcflFileRetriever() throws Exception {
        storage.write("a/b/file.txt", "lazy content".getBytes(StandardCharsets.UTF_8), null);

        var retriever = storage.readLazy("a/b/file.txt", DigestAlgorithmRegistry.sha512, computeSha512("lazy content"));

        assertThat(retriever).isInstanceOf(LayeredStorageOcflFileRetriever.class);
        try (var stream = retriever.retrieveFile()) {
            assertThat(stream.readAllBytes()).isEqualTo("lazy content".getBytes(StandardCharsets.UTF_8));
        }
    }

    // --- moveDirectoryInto ---

    @Test
    public void moveDirectoryInto_throws_OcflFileAlreadyExistsException_when_destination_exists() throws Exception {
        var externalDir = Files.createDirectories(testDir.resolve("external"));
        Files.writeString(externalDir.resolve("x.txt"), "x");

        // destination "a/b" already exists
        assertThatThrownBy(() -> storage.moveDirectoryInto(externalDir, "a/b"))
            .isInstanceOf(OcflFileAlreadyExistsException.class);
    }

    @Test
    public void moveDirectoryInto_succeeds_when_destination_does_not_exist() throws Exception {
        var externalDir = Files.createDirectories(testDir.resolve("external"));
        Files.writeString(externalDir.resolve("x.txt"), "x");

        storage.moveDirectoryInto(externalDir, "a/newdir");

        assertThat(storage.fileExists("a/newdir/x.txt")).isTrue();
    }

    // --- moveDirectoryInternal ---

    @Test
    public void moveDirectoryInternal_throws_OcflNoSuchFileException_when_source_does_not_exist() {
        assertThatThrownBy(() -> storage.moveDirectoryInternal("a/nonexistent", "a/dest"))
            .isInstanceOf(OcflNoSuchFileException.class);
    }

    @Test
    public void moveDirectoryInternal_throws_OcflFileAlreadyExistsException_when_destination_exists() {
        storage.createDirectories("a/src");
        // "a/b" already exists from setUp
        assertThatThrownBy(() -> storage.moveDirectoryInternal("a/src", "a/b"))
            .isInstanceOf(OcflFileAlreadyExistsException.class);
    }

    @Test
    public void moveDirectoryInternal_does_not_throw_when_preconditions_are_met() {
        storage.createDirectories("a/src");
        storage.write("a/src/file.txt", "data".getBytes(StandardCharsets.UTF_8), null);

        // Verify no precondition exception is thrown
        storage.moveDirectoryInternal("a/src", "a/moved");
    }

    // --- deleteEmptyDirsDown ---

    @Test
    public void deleteEmptyDirsDown_does_not_delete_starting_path_when_it_has_files() {
        storage.write("a/b/file.txt", "data".getBytes(StandardCharsets.UTF_8), null);

        storage.deleteEmptyDirsDown("a/b");

        assertThat(storage.fileExists("a/b/file.txt")).isTrue();
    }

    // --- helper ---

    private static String computeSha512(String content) {
        try {
            var digest = MessageDigest.getInstance("SHA-512");
            var bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (var b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
