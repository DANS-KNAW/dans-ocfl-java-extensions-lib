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
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class LayerManagerImpl implements LayerManager {
    private final LayerDatabase layerDatabase;

    private final Path stagingRoot;

    private final Path archiveRoot;

    private LayerImpl topLayer;

    @Override
    public synchronized void openNewTopLayer() {
        if (topLayer != null) {
            topLayer.close();
        }
        var id = System.currentTimeMillis();
        topLayer = new LayerImpl(id, layerDatabase, stagingRoot.resolve(Long.toString(id)), new ZipArchive(archiveRoot.resolve(id + ".zip")));
    }


    @Override
    public synchronized void createDirectories(String path) throws LayerNotWritableException, IOException {
        topLayer.createDirectories(path);
    }

    @Override
    public synchronized void write(String filePath, InputStream content) throws LayerNotWritableException, IOException {
        topLayer.write(filePath, content);
    }

    @Override
    public InputStream read(String filePath) throws IOException {
        return null;
    }

    @Override
    public List<Layer> findLayersContaining(String path) {
        return null;
    }

    @Override
    public List<Listing> listDirectory(String directoryPath) {
        return null;
    }

    @Override
    public List<Listing> listRecursive(String directoryPath) {
        return null;
    }

    @Override
    public boolean fileExists(String filePath) {
        return false;
    }
}
