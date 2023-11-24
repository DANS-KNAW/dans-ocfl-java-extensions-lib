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

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

class LayerImpl implements Layer {
    private final long id;
    private final LayerDatabase layerDatabase;
    private final Path workDir;

    private final Path stagingDir;

    private final Path mutationTokenDir;
    private final Archive archive;

    @SneakyThrows
    LayerImpl(long id, LayerDatabase layerDatabase, Path workDir, Archive archive) {
        this.id = id;
        this.layerDatabase = layerDatabase;
        this.archive = archive;
        this.workDir = Files.createDirectories(workDir);
        this.stagingDir = Files.createDirectory(workDir.resolve("ocfl-staging"));
        this.mutationTokenDir = Files.createDirectory(workDir.resolve("mutation-tokens"));
    }

    private boolean isArchived() {
        return archive.isArchived();
    }

    private boolean isClosed() {
        return isArchived() || Files.exists(workDir.resolve("closed"));
    }

    @SneakyThrows
    private boolean isMutating() {
        return !isArchived() && Files.exists(mutationTokenDir) && !FileUtils.listFiles(mutationTokenDir.toFile(), null, false).isEmpty();
    }

    @SneakyThrows(value = IOException.class)
    private String startMutation() throws LayerNotWritableException {
        if (isClosed()) {
            throw new LayerNotWritableException("Layer is closed");
        }
        String mutationToken = UUID.randomUUID().toString();
        Files.createFile(mutationTokenDir.resolve(mutationToken));
        return mutationToken;
    }

    @SneakyThrows
    private void endMutation(String mutationToken) throws LayerNotWritableException {
        Files.delete(mutationTokenDir.resolve(mutationToken));
    }

    @Override
    public void createDirectories(String path) throws LayerNotWritableException, IOException {
        var mutationToken = startMutation();
        try {
            Files.createDirectories(stagingDir.resolve(path));
            layerDatabase.addDirectory(id, path);
        }
        finally {
            endMutation(mutationToken);
        }
    }

    @Override
    public void write(String filePath, InputStream content) throws LayerNotWritableException, IOException {
        var mutationToken = startMutation();
        try {
            Files.copy(content, stagingDir.resolve(filePath));
            layerDatabase.addFile(id, filePath);
        }
        finally {
            endMutation(mutationToken);
        }
    }

    @Override
    public void deleteFiles(List<String> paths) {

    }

    @Override
    public InputStream read(String path) throws IOException {
        if (isArchived()) {
            return archive.read(path);
        }
        else {
            return Files.newInputStream(stagingDir.resolve(path));
        }
    }

    @Override
    @SneakyThrows
    public void close() {
        Files.createFile(workDir.resolve("closed"));
    }

    @Override
    @SneakyThrows
    public void archive() {
        if (isArchived()) {
            throw new IllegalStateException("Layer is already archived");
        }
        if (!isClosed()) {
            throw new IllegalStateException("Layer is not closed");
        }
        if (isMutating()) {
            throw new IllegalStateException("Layer has pending mutations");
        }
        archive.archiveFrom(stagingDir);
        FileUtils.deleteDirectory(stagingDir.toFile());
    }
}
