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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
class LayerImpl implements Layer {

    @NonNull
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
        Files.createDirectories(stagingDir.resolve(path));
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void deleteFiles(List<String> paths) throws IOException {
        for (String path : paths) {
            Files.delete(stagingDir.resolve(path));
        }
    }

    @Override
    public InputStream read(String path) throws IOException {
        return Files.newInputStream(stagingDir.resolve(path));
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public void archive() {
        archive.archiveFrom(stagingDir);
    }

    @Override
    public void write(String filePath, InputStream content) throws IOException {
        Files.copy(content, stagingDir.resolve(filePath));
    }

    @Override
    public void moveDirectoryInto(Path source, String destination) throws IOException {
        Files.move(source, stagingDir.resolve(destination));
    }

    @Override
    public boolean fileExists(String path) throws IOException {
        return Files.exists(stagingDir.resolve(path));
    }

    @Override
    public void moveDirectoryInternal(String source, String destination) throws IOException {
        Files.move(stagingDir.resolve(source), stagingDir.resolve(destination));
    }

    @Override
    public void deleteDirectory(String path) throws IOException {
        FileUtils.deleteDirectory(stagingDir.resolve(path).toFile());
    }
}
