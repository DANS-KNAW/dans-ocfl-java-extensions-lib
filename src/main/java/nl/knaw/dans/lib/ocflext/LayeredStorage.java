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
import io.ocfl.api.exception.OcflIOException;
import io.ocfl.api.io.FixityCheckInputStream;
import io.ocfl.api.model.DigestAlgorithm;
import io.ocfl.core.storage.common.Listing;
import io.ocfl.core.storage.common.OcflObjectRootDirIterator;
import io.ocfl.core.storage.common.Storage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.layerstore.Item;
import nl.knaw.dans.layerstore.ItemStore;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class LayeredStorage implements Storage {
    private final ItemStore itemStore;

    @Override
    public List<Listing> listDirectory(String directoryPath)  {
        try {
            return itemStore
                .listDirectory(directoryPath)
                .stream()
                .map(item -> Util.fromItem(item, directoryPath))
                .collect(Collectors.toList());
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public List<Listing> listRecursive(String directoryPath) {
        try {
            return itemStore.listRecursive(directoryPath)
                .stream()
                .map(item -> Util.fromItem(item, directoryPath))
                .collect(Collectors.toList());
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public boolean directoryIsEmpty(String directoryPath) {
        return listDirectory(directoryPath).isEmpty();
    }

    @Override
    public OcflObjectRootDirIterator iterateObjects() {
        return new LayeredStorageOcflObjectRootDirIterator(itemStore);
    }

    @Override
    public boolean fileExists(String filePath) {
        return itemStore.existsPathLike(filePath);
    }

    @Override
    public InputStream read(String filePath) {
        try {
            return itemStore.readFile(filePath);
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public String readToString(String filePath) {
        try {
            StringWriter writer = new StringWriter();
            try (InputStream is = read(filePath)) {
                IOUtils.copy(is, writer, StandardCharsets.UTF_8);
            }
            return writer.toString();
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public OcflFileRetriever readLazy(String filePath, DigestAlgorithm algorithm, String digest) {
        return () -> new FixityCheckInputStream(read(filePath), algorithm, digest);
    }

    @Override
    public void write(String filePath, byte[] content, String mediaType) {
        try {
            itemStore.writeFile(filePath, new ByteArrayInputStream(content));
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void createDirectories(String path) {
        try {
            itemStore.createDirectory(path);
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void copyDirectoryOutOf(String source, Path destination) {
        try {
            itemStore.copyDirectoryOutOf(source, destination);
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void copyFileInto(Path source, String destination, String mediaType) {
        try {
            itemStore.writeFile(destination, Files.newInputStream(source));
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void copyFileInternal(String sourceFile, String destinationFile) {
        try {
            itemStore.writeFile(destinationFile, itemStore.readFile(sourceFile));
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void moveDirectoryInto(Path source, String destination) {
        try {
            itemStore.moveDirectoryInto(source, destination);
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void moveDirectoryInternal(String source, String destination) {
        try {
            itemStore.moveDirectoryInternal(source, destination);
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void deleteDirectory(String path) {
        try {
            itemStore.deleteDirectory(path);
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void deleteFile(String path) {
        deleteFiles(Collections.singletonList(path));
    }

    @Override
    public void deleteFiles(Collection<String> paths) {
        try {
            itemStore.deleteFiles(new ArrayList<>(paths));
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void deleteEmptyDirsDown(String path) {
        try {
            List<Item> containedItems;
            containedItems = itemStore.listRecursive(path);
            // Sort by descending path length, so that we start with the deepest directories
            containedItems.sort((i1, i2) -> Integer.compare(i2.getPath().length(), i1.getPath().length()));
            for (Item item : containedItems) {
                if (item.getType().equals(Item.Type.Directory)) {
                    if (directoryIsEmpty(item.getPath())) {
                        itemStore.deleteDirectory(item.getPath());
                    }
                }
            }
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    @Override
    public void deleteEmptyDirsUp(String path) {
        var pathParts = path.split("/");
        for (int i = pathParts.length - 1; i >= 0; i--) {
            var parentPath = String.join("/", pathParts).substring(0, String.join("/", pathParts).lastIndexOf("/"));
            if (directoryIsEmpty(parentPath)) {
                deleteDirectory(parentPath);
            }
        }
    }

    @Override
    public void close() {
        // nothing to close
    }
}
