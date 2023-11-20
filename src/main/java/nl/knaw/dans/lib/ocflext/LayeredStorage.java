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

import io.ocfl.api.OcflFileRetriever;
import io.ocfl.api.model.DigestAlgorithm;
import io.ocfl.core.storage.common.Listing;
import io.ocfl.core.storage.common.OcflObjectRootDirIterator;
import io.ocfl.core.storage.common.Storage;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LayeredStorage implements Storage {
    private LayerManager layerManager;


    public synchronized void openNewTopLayer() {
        layerManager.newTopLayer();
    }


    @Override
    public List<Listing> listDirectory(String directoryPath) {
        return layerManager.listDirectory(directoryPath);
    }

    @Override
    public List<Listing> listRecursive(String directoryPath) {
        return layerManager.listRecursive(directoryPath);
    }

    @Override
    public boolean directoryIsEmpty(String directoryPath) {
        return listDirectory(directoryPath).isEmpty();
    }

    @Override
    public OcflObjectRootDirIterator iterateObjects() {
        return null;
    }

    @Override
    public boolean fileExists(String filePath) {
        return layerManager.fileExists(filePath);
    }

    @Override
    public InputStream read(String filePath) {
        return layerManager.findHighestLayerContaining(filePath).read(filePath);
    }

    @SneakyThrows
    @Override
    public String readToString(String filePath) {
        StringWriter writer = new StringWriter();
        IOUtils.copy(read(filePath), writer, StandardCharsets.UTF_8);
        return writer.toString();
    }

    @Override
    public OcflFileRetriever readLazy(String filePath, DigestAlgorithm algorithm, String digest) {
        return null;
    }

    @Override
    public synchronized void write(String filePath, byte[] content, String mediaType) {

    }

    @SneakyThrows
    @Override
    public synchronized void createDirectories(String path) {
        layerManager.getTopLayer().createDirectories(path);
    }

    @Override
    public void copyDirectoryOutOf(String source, Path destination) {

    }

    @Override
    public void copyFileInto(Path source, String destination, String mediaType) {

    }

    @Override
    public void copyFileInternal(String sourceFile, String destinationFile) {

    }

    @Override
    public void moveDirectoryInto(Path source, String destination) {

    }

    @Override
    public void moveDirectoryInternal(String source, String destination) {

    }

    @Override
    public void deleteDirectory(String path) {

    }

    @Override
    public void deleteFile(String path) {
        List<Layer> layers = layerManager.findLayersContaining(path);
        for (Layer layer : layers) {
            layer.deleteFiles(List.of(path));
        }
    }

    @Override
    public void deleteFiles(Collection<String> paths) {
        Map<Layer, Set<String>> layerPaths = new HashMap<>();
        for (String path : paths) {
            var layers = layerManager.findLayersContaining(path);
            for (Layer layer : layers) {
                layerPaths.computeIfAbsent(layer, k -> new HashSet<>()).add(path);
            }
        }
        for (Map.Entry<Layer, Set<String>> entry : layerPaths.entrySet()) {
            entry.getKey().deleteFiles(new ArrayList<>(entry.getValue()));
        }
    }

    @Override
    public void deleteEmptyDirsDown(String path) {

    }

    @Override
    public void deleteEmptyDirsUp(String path) {

    }
}
