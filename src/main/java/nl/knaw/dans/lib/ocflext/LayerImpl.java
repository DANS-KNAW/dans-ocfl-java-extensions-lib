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

import io.ocfl.core.storage.common.Listing;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RequiredArgsConstructor
class LayerImpl implements Layer {

    @Getter
    private final long id;

    @NonNull
    private final Path stagingDir;

    @NonNull
    private final Archive archive;

    @Getter
    private boolean closed = false;

    @Override
    public void createDirectories(String path) throws IOException {
        checkOpen();
        validatePath(path);
        ensureStagingDirExists();
        Files.createDirectories(stagingDir.resolve(path));
    }

    private void ensureStagingDirExists() throws IOException {
        if (!Files.exists(stagingDir))
            Files.createDirectories(stagingDir);
    }

    private void checkOpen() {
        if (closed)
            throw new IllegalStateException("Layer is closed, but must be open for this operation");
    }

    private void checkClosed() {
        if (!closed)
            throw new IllegalStateException("Layer is open, but must be closed for this operation");
    }

    @Override
    public void deleteFiles(List<String> paths) throws IOException {
        checkOpen();
        ensureStagingDirExists();
        if (paths == null)
            throw new IllegalArgumentException("Paths cannot be null");
        for (String path : paths) {
            validatePath(path);
            Files.delete(stagingDir.resolve(path));
        }
    }

    /*
     * This method is synchronized, because the layer might otherwise be closed just after the check. Note, that after the file handle is returned, the layer may be closed, but
     * that is not a problem, because the file handle is still valid until it is closed, even if the directory containing the file is deleted.
     */
    @Override
    public synchronized InputStream read(String path) throws IOException {
        if (archive.isArchived()) {
            return archive.getInputStreamFor(path);
        }
        else {
            return readFromStaging(path);
        }
    }

    private InputStream readFromStaging(String path) throws IOException {
        return Files.newInputStream(stagingDir.resolve(path));
    }

    @Override
    public synchronized void close() {
        closed = true;
    }

    @Override
    @SneakyThrows
    public synchronized void archive() {
        checkClosed();
        checkNotArchived();
        ensureStagingDirExists();
        archive.archiveFrom(stagingDir);
        FileUtils.deleteDirectory(stagingDir.toFile());
    }

    private void checkNotArchived() {
        if (archive.isArchived())
            throw new IllegalStateException("Layer is already archived");
    }

    @Override
    public void write(String filePath, InputStream content) throws IOException {
        checkOpen();
        validatePath(filePath);
        ensureStagingDirExists(); // TODO: not needed? Is taken care of by initialization of storage
        Files.copy(content, stagingDir.resolve(filePath), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public List<Listing> moveDirectoryInto(Path source, String destination) throws IOException {
        checkOpen();
        ensureStagingDirExists();
        validatePath(destination);
        var destinationPath = stagingDir.resolve(destination);
        Files.move(source, destinationPath);
        try (var s = Files.walk(destinationPath)) {
            return s.map(path -> getListing(path, destination + "/" + source.relativize(path))).toList();
        }
    }

    private Listing getListing(Path path, String relativePath) {
        if (Files.isDirectory(path)) {
            return Listing.directory(path.getFileName().toString());
        }
        else if (Files.isRegularFile(path)) {
            return Listing.file(path.getFileName().toString());
        }
        else {
            return Listing.other(path.getFileName().toString());
        }
    }


    @Override
    public boolean fileExists(String path) throws IOException {
        validatePath(path);
        if (archive.isArchived()) {
            return archive.fileExists(path);
        }
        else {
            return fileExistsInStaging(path);
        }
    }

    private boolean fileExistsInStaging(String path) {
        return Files.exists(stagingDir.resolve(path));
    }

    @Override
    public void moveDirectoryInternal(String source, String destination) throws IOException {
        checkOpen();
        validatePath(source);
        validatePath(destination);
        Files.move(stagingDir.resolve(source), stagingDir.resolve(destination));
    }

    @Override
    public void deleteDirectory(String path) throws IOException {
        checkOpen();
        validatePath(path);
        FileUtils.deleteDirectory(stagingDir.resolve(path).toFile());
    }

    private void validatePath(String path) {
        if (path == null)
            throw new IllegalArgumentException("Path cannot be null");
        if (path.isBlank() && !path.isEmpty())
            throw new IllegalArgumentException("Path cannot be blank");
        var pathInStagingDir = stagingDir.resolve(path).normalize();
        // Check if the path is outside the staging dir
        if (!pathInStagingDir.startsWith(stagingDir))
            throw new IllegalArgumentException("Path is outside staging directory");
    }

}
